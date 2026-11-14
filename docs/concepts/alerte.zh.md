# 警觉等级与行为

`AwarenessComponent` 会把最佳刺激转换为**警觉等级**。等级会立即上升，
并在一段延迟后逐级下降。

| 等级 | 默认行为 | 降级前的延迟 |
|---|---|---|
| `IDLE` | 正常游荡 | — |
| `SUSPICIOUS` | 停下，把头转向声音来源（`ListenGoal`） | 40 tick |
| `INVESTIGATE` | 走过去查看，然后搜索该区域（`InvestigateGoal`） | 80 tick |
| `HUNT` | 跑向最后已知的位置 | 140 tick |

## 阈值

```java
.awareness(new AwarenessProfile(0.5f, 6f, 18f))  // 怀疑、调查、追猎
```

| 配置 | 怀疑 | 调查 | 追猎 |
|---|---|---|---|
| `AwarenessProfile.DEFAULT` | 0.5 | 6 | 18 |
| `AwarenessProfile.PARANOID` | 0.3 | 4 | 13 |
| `AwarenessProfile.CALM` | 2 | 10 | 25 |

参见[分数参考](memoire.md#score)。

## 开箱即用的行为

`.defaultGoals(优先级, 步行, 奔跑)` 会添加：

- **`InvestigateGoal`**（给定优先级）：前往估计的位置，然后在不确定范围内的随机点进行搜索。
  如果在 `searchDuration` tick 内没有新的信息，就会放弃这个刺激。
  每次新的感知都会让它重新前往更新后的位置。
- **`ListenGoal`**（优先级 + 1）：处于 `SUSPICIOUS` 时，停下来朝推测的方向四处张望。

你也可以自己实例化它们：

```java
goalSelector.add(2, new InvestigateGoal(agent, 0.9, 1.4, 240));
goalSelector.add(3, new ListenGoal(agent));
```

## 控制警觉

```java
AwarenessComponent alert = agent.awareness();
alert.escalate(Awareness.HUNT);   // 强制设定一个最低等级
alert.giveUp(alert.focus());      // 忘记当前的刺激
```

## 编写你自己的行为

在你的 goal 中读取等级和黑板：

```java
@Override
public boolean canStart() {
    return agent.awarenessState() == Awareness.HUNT
            && agent.blackboard().has(BlackboardKeys.VISIBLE_TARGET);
}
```
