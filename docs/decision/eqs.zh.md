# 环境查询（EQS）

**环境查询**（即虚幻引擎的 *Environment Query System*）用来回答诸如
“我该躲在哪里？”“我该在哪里找掩护？”“从哪边包抄它？”之类的问题。它会：

1. 在目标周围**生成**候选点（落在地面上，生物可以站立的位置）；
2. 用测试对这些点进行**过滤**和**打分**；
3. 返回最佳的点，并在需要时检查它是否可达。

## 内置查询

**威胁**是确定看到的目标，否则是关注点的位置。

| 标识符 | 查找 |
|---|---|
| `sentience:hide` | 附近一个威胁看不到你的地方，尽量远离它 |
| `sentience:cover` | 非常近的掩体，你和威胁之间有一个实心方块 |
| `sentience:flank` | 威胁侧面的一个点，用于包抄 |
| `sentience:flee` | 远离威胁的一个点，尽可能在其视线之外 |

在行为树中：

```json
{ "type": "run_query", "query": "sentience:hide", "result": "mymod:hiding_spot" },
{ "type": "move_to", "target": "mymod:hiding_spot", "speed": 1.4 }
```

如果没有任何有效的点，`run_query` 会失败：序列停止，行为树转而执行其他分支。

## 用 JSON 编写查询

文件 `data/mymod/sentience/env_query/dark_corner.json` → 标识符 `mymod:dark_corner`。

```json
{
  "generator": { "type": "ring", "center": "querier", "radius": [4, 14], "rings": 3, "points": 10 },
  "tests": [
    { "type": "light", "max": 9, "weight": 2, "prefer": "low" },
    { "type": "visible_from", "target": "threat", "max": 0 },
    { "type": "distance", "target": "querier", "weight": 0.5, "prefer": "high" }
  ],
  "pathable": true
}
```

### 生成器

| 类型 | 参数 |
|---|---|
| `ring` | `center`、`radius`（数字或 `[min, max]`）、`rings`（3）、每圈 `points`（12） |
| `grid` | `center`、`radius`（10）、`spacing`（2） |

### 目标

`querier`（智能体自身）、`threat`（威胁）、`focus`（关注点位置）或黑板键的标识符。

### 测试

| 类型 | 测量值 |
|---|---|
| `distance` | 点与 `target` 之间的距离 |
| `visible_from` | 如果 `target` 能看到该点（头部高度，可透过玻璃）则为 1，否则为 0 |
| `cover` | 如果在 `target` 一侧有实心方块保护该点则为 1 |
| `dot` | 从 `from` 看，点与 `to` 之间夹角的余弦（1 = 同一方向，0 = 侧面） |
| `light` | 该点的光照等级（0-15） |
| `random` | 随机值，用于打破平局 |

每个测试可以：

- **过滤**：`min` 和/或 `max` 会剔除区间之外的点；
- **打分**：`weight`（0 = 仅过滤）和 `prefer`（`high` 或 `low`）。数值会在剩余的点上
  归一化到 0 到 1 之间，然后相加。

`"pathable": true` 只保留可达的点：只会对最好的几个点计算路径，直到找到一个可达的点。

## Java 写法

```java
public static final EnvQuery HIDING_SPOT = EnvQueries.register(Identifier.of("mymod", "hiding_spot"),
        EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 4, 16, 3, 12))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))    // 过滤：隐藏
                .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(1))   // 远离威胁
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1.5)) // 离自己不要太远
                .pathable()
                .build());

Vec3d spot = HIDING_SPOT.run(agent).best();   // 如果没有合适的点则为 null
```

自定义测量是一个函数，它接收智能体和世界，并返回每个点的测量值：

```java
QueryTest.of((agent, world) -> point -> world.getBlockState(BlockPos.ofFloored(point).down()).isOf(Blocks.GRASS_BLOCK) ? 1 : 0)
        .preferHigh(1);
```

## 调试

开启 `/sentience debug` 后，每次查询都会显示它的点：**灰色** = 被剔除，**绿色** = 保留，
最佳点上会有闪光粒子。

## 性能

一次查询需要几十次方块或视线测试，开启 `pathable` 时还要计算几条路径。
请在进入分支时（`run_query`）或用间隔较长的服务（20 tick 或更多）执行，不要每个 tick 都执行。
