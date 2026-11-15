# 行为树

**行为树（Behavior Tree）**以树的形式描述生物的全部行为，与虚幻引擎相同：
**选择器**挑选优先级最高的分支，**序列**依次执行各个步骤，
**装饰器**添加条件，**服务**在后台运行。

可以在数据包中**用 JSON 编写**（通过 `/reload` 热重载），也可以**用 Java 编写**。

```java
Agent.builder(this)
        .hearing(HearingProfile.DEFAULT)
        .sight(SightConfig.humanoid())
        .damage()
        .awareness(AwarenessProfile.DEFAULT)
        .behaviorTree(Identifier.of("mymod", "guard"), 1)   // (1)!
        .build();
```

1. 行为树由一个优先级为 1 的 goal 执行，它会接管生物的**移动**和**视线**控制。
   优先级更高的 goal（游泳……）先执行；之后行为树从根节点重新开始。
   不要同时使用 `defaultGoals`：行为树会取代它们。

## JSON 写法

文件 `data/mymod/sentience/behavior_tree/guard.json` → 标识符 `mymod:guard`。

```json
{
  "root": {
    "type": "selector",
    "children": [
      {
        "name": "战斗",
        "type": "attack",
        "target": "visible_target",
        "decorators": [ { "type": "sees_target", "abort": "both" } ],
        "services": [ { "type": "share_focus", "range": 32, "interval": 40 } ]
      },
      {
        "name": "调查",
        "type": "sequence",
        "decorators": [ { "type": "awareness", "at_least": "investigate", "abort": "lower" } ],
        "children": [
          { "type": "move_to", "target": "focus_position", "speed": 1.1 },
          { "type": "search", "radius": 6, "ticks": 160 },
          { "type": "give_up" }
        ]
      },
      { "type": "wander", "radius": 10, "speed": 0.7 }
    ]
  }
}
```

每个节点都有一个 `type`，此外还接受：

| 字段 | 作用 |
|---|---|
| `name` | 在 `/sentience debug` 中显示的名称 |
| `decorators` | 装饰器列表（第一个在最外层） |
| `services` | 服务列表，`interval` 以 tick 为单位（默认 20） |

不带命名空间的标识符（`sequence`、`focus_position`）指 Sentience 自带的。
黑板的键用其标识符书写：`focus_position`、`visible_target`、`mymod:hiding_spot`……

!!! tip "热修改"
    修改文件后输入 `/reload`：所有智能体会立即重建它们的行为树。
    错误会连同文件名一起写入服务器日志。
    数据包也可以通过使用相同的标识符来**替换**某个模组的行为树。

## 执行

每个 tick，行为树都会从根节点开始遍历。每个节点返回 **SUCCESS**、**FAILURE**
或 **RUNNING**（尚未完成，下个 tick 继续）。

| 组合节点 | 行为 |
|---|---|
| `sequence` | 按顺序执行子节点；任一子节点失败即失败 |
| `selector` | 按优先级执行子节点；任一子节点成功即成功 |
| `random_selector` | 与 `selector` 相同，但顺序随机 |
| `parallel` | 同时执行所有子节点；`"require": "all"` 或 `"one"` |
| `subtree` | 插入另一棵树：`"tree": "mymod:combat"` |

### 中断（`abort`）

作为装饰器使用的条件可以带 `abort`，即虚幻引擎中的 *observer aborts*：

| `abort` | 效果 |
|---|---|
| `none` | 只在进入分支时检查 |
| `self` | 条件一旦变为假，分支立即停止 |
| `lower` | 条件一旦变为真，就中断正在运行的**更低优先级**分支 |
| `both` | 两者兼有 |

这让行为树具有反应能力：一旦声音提升了警觉，巡逻就会被打断；
一旦看不见目标，战斗就会停止。

## 参考

### 任务

| 类型 | 参数（默认值） |
|---|---|
| `wait` | `ticks`（20）、`deviation`（0） |
| `move_to` | `target`（focus_position）、`speed`（1）、`acceptance`（1.5）、`timeout`（0 = 无） |
| `wander` | `radius`（8）、`speed`（0.8） |
| `search` | `target`（focus_position）、`radius`（6）、`ticks`（200）、`speed`（0.8） |
| `flee` | `from`（focus_position）、`distance`（12）、`speed`（1.3） |
| `look_at` | `target`（focus_position）、`ticks`（0 = 一个 tick） |
| `attack` | `target`（visible_target）、`speed`（1.3）、`cooldown`（20）——近战 |
| `run_query` | `query`、`result`（query_result）——参见[环境查询](eqs.md) |
| `set` | `key`、`value` 或 `from`（复制另一个键） |
| `clear` | `key` |
| `play_sound` | `sound`、`volume`（1）、`pitch`（1） |
| `emit_noise` | `db`（80）、`noise`（generic）——会被智能体听到 |
| `share_focus` | `range`（32）、`same_type`（true） |
| `give_up` | 忘记当前的刺激 |
| `alert` | `level`：将警觉至少提升到该等级 |
| `set_attack_target` | `target`：原版目标，供原版 goal 使用 |
| `succeed`、`fail` | 成功 / 失败 |
| `check` | `condition`：条件为真时成功 |

条件也可以直接作为节点使用：`{ "type": "sees_target" }`。

`move_to`、`look_at`、`attack`……接受的键可以包含位置（`Vec3d`）、方块（`BlockPos`）、
实体（其 UUID）或刺激。

### 条件

可用作装饰器（配合 `abort`）、用于 `check`，或用于 `not` / `all` / `any`。
都支持 `"negate": true`。

| 类型 | 参数 |
|---|---|
| `awareness` | `at_least`、`is` 或 `below`：`idle`、`suspicious`、`investigate`、`hunt` |
| `sees_target` | 确定看到了某个目标 |
| `is_set` | `key` |
| `equals` | `key`、`value` |
| `alive` | `target`（visible_target） |
| `health` | `below` 或 `above`（最大生命值的比例） |
| `distance` | `target`、`below` 和/或 `above` |
| `light` | `below` 和/或 `above`（0-15） |
| `is_night`、`is_day`、`in_water` | — |
| `chance` | `chance`（0.5） |
| `not` | `condition` |
| `all`、`any` | `conditions` |

### 装饰器

除条件之外：

| 类型 | 参数 |
|---|---|
| `cooldown` | `ticks`（100）：分支结束后不足 `ticks` 时失败 |
| `time_limit` | `ticks`（200）：超时则中断并失败 |
| `repeat` | `count`（-1 = 无限）：子节点成功时重新执行 |
| `invert`、`force_success`、`force_failure` | 修改结果 |

它们也可以作为带 `child` 的节点使用。

### 服务

| 类型 | 参数 |
|---|---|
| `run_query` | `query`、`result`：定期刷新一个点 |
| `share_focus` | `range`、`same_type` |
| `copy` | `from`、`to` |
| `look_at` | `target` |

## Java 写法

```java
public static final BehaviorTree GUARD = BehaviorTrees.register(Identifier.of("mymod", "guard"), () ->
        Bt.selector(
                Bt.attack(BlackboardKeys.VISIBLE_TARGET, 1.3, 20)
                        .guard(Bt.seesTarget(), BtAbort.BOTH)
                        .service(40, ctx -> AgentComms.shareFocus(ctx.agent(), 32, other -> true)),
                Bt.sequence(
                        Bt.moveTo(BlackboardKeys.FOCUS_POSITION, 1.1),
                        Bt.search(BlackboardKeys.FOCUS_POSITION, 6, 160, 0.8),
                        Bt.giveUp())
                        .guard(Bt.awarenessAtLeast(Awareness.INVESTIGATE), BtAbort.LOWER),
                Bt.wander(10, 0.7)));

Agent.builder(this)...behaviorTree(GUARD, 1).build();
```

工厂会**为每个智能体调用一次**：每个生物都有自己的节点。
具有相同标识符的 JSON 树会替换 Java 树。

### 编写任务

```java
public class HowlTask extends BtNode {
    private int remaining;

    @Override
    protected void onStart(BtContext ctx) {
        remaining = 40;
        ctx.mob().playSound(SoundEvents.ENTITY_WOLF_HOWL, 3f, 0.8f);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        remaining -= ctx.deltaTicks();
        return remaining <= 0 ? BtStatus.SUCCESS : BtStatus.RUNNING;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        // 清理（aborted = 被更高优先级的分支中断）
    }
}
```

让它可以在 JSON 中使用：

```java
BtRegistry.registerNode(Identifier.of("mymod", "howl"), json -> HowlTask::new);
BtRegistry.registerCondition(Identifier.of("mymod", "full_moon"),
        json -> ctx -> ctx.world().getMoonPhase() == 0);
BtRegistry.registerService(Identifier.of("mymod", "sniff"),
        json -> ctx -> ctx.mob().playSound(SoundEvents.ENTITY_WOLF_SNIFF, 1f, 1f));
```

JSON 参数通过 `SentienceJson`（`number`、`integer`、`string`、`key`……）读取。
条件和服务必须是**无状态**的：所有智能体共享同一个实例。

## 调试

`/sentience debug` 会在每个智能体头顶显示当前任务：`HUNT 👂 22.2 ±2.9 ▶ attack`。
