# 智能体

`Agent` 由一个[黑板](blackboard.md)和一组**组件**（`AgentComponent`）组成。
内置组件包括**感知**（各种感官 + 它们的记忆）和**警觉**；你也可以添加自己的组件。

```mermaid
flowchart LR
    S[感官<br/>听觉、视觉、嗅觉……] -->|StimulusInput| M[记忆]
    M -->|最佳刺激| A[警觉]
    A -->|写入| B[(黑板)]
    M -->|写入| B
    B -->|被读取| G[目标 Goals<br/>倾听、调查、你的行为]
    C[你的组件] <-->|读取 / 写入| B
```

## 构建器

| 方法 | 作用 |
|---|---|
| `.rules(SentienceRules)` | 你的模组的感知规则（[指南](../guides/regles.md)） |
| `.hearing(HearingProfile[, 兴趣])` | [听觉](../sens/ouie.md) |
| `.sight(SightConfig)` | [视觉](../sens/vue.md) |
| `.scent(ScentSense)` | [嗅觉](../sens/odorat.md) |
| `.damage()` / `.touch([目标])` | [痛觉、触觉](../sens/douleur-toucher.md) |
| `.sense(SenseModule)` | [自定义感官](../guides/sens-custom.md) |
| `.memory(容量, ticks)` | 记忆容量与时长（8 条记忆，600 tick） |
| `.awareness(AwarenessProfile)` / `.noAwareness()` | [警觉等级](alerte.md) |
| `.defaultGoals(优先级, 步行, 奔跑)` | 开箱即用的行为 |
| `.searchDuration(ticks)` | 放弃前的搜索时长（240） |
| `.component(AgentComponent)` | [自定义组件](../guides/composant-custom.md) |
| `.build()` | 创建智能体并附加到生物上 |

## 生命周期

| 时机 | 发生的事情 |
|---|---|
| `build()` | 对每个组件调用 `onAttach` |
| 生物生成 / 所在区块加载 | `onLoad`：各感官进行注册（例如注册到听觉系统） |
| 每个服务器 tick | `tick(agent, world, deltaTicks)`，在原版 AI 之前执行 |
| 死亡 / 卸载 | `onUnload` |

## 细节层次（LOD） {#level-of-detail-lod}

远离所有玩家的智能体思考得更少：

| 与最近玩家的距离 | 思考频率 |
|---|---|
| ≤ 32 格 | 每 tick |
| ≤ 64 格 | 每 2 tick 一次 |
| ≤ 128 格 | 每 5 tick 一次 |
| 更远 | 每 20 tick 一次 |

`deltaTicks` 表示距离上次思考已经过去了多少 tick。
任何新的感知都会迫使它在下一个 tick 立即做出反应：远处的生物听到爆炸会马上行动。

## 事件

```java
agent.onStimulus((agent, stimulus) -> { ... });               // 每次感知
agent.onAwarenessChanged((agent, from, to) -> { ... });       // 警觉等级变化
```

## 访问

```java
Agent agent = Agent.of(entity);                 // 如果实体没有智能体则为 null
agent.mob(); agent.rules(); agent.blackboard();
agent.perception();                             // 感官 + 记忆
agent.awareness();                              // 警觉等级
agent.find(MyComponent.class);                  // 某个特定组件
agent.thinkInterval();                          // 当前 LOD
```
