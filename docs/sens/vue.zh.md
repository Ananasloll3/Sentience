# 视觉

```java
.sight(SightConfig.humanoid())
```

## 工作原理

每隔几个 tick，智能体会计算范围内每个目标的**可见度**（0 到 1）：

```
可见度 = 角度 × 视线 × 光照 × 隐蔽 × 距离
```

| 因素 | 说明 |
|---|---|
| 角度 | 清晰视野内为 1，周边视觉为 0.3，背后为 0 |
| 视线 | 射线指向目标的眼睛和身体；玻璃、玻璃板和铁栏杆可以透视 |
| 光照 | 完全黑暗时最低为 0.05（除非有 `darkVision`）；手持火把或着火会让目标可见 |
| 隐蔽 | 潜行 ×0.5，隐身 ×0.08，移动 ×1.3，发光效果会暴露，规则中的伪装 |
| 距离 | 在范围边缘最多 −70% |

每个目标都会填充一个**发现进度条**，就像潜行游戏一样：

- 从 35% 开始：*瞥见*——位置和身份都比较模糊（`kind = "glimpse"`）；
- 达到 100%：目标被*看到*——精确位置，并设置 `BlackboardKeys.VISIBLE_TARGET`（`kind = "seen"`）；
- 距离小于 2.5 格：立即发现。

当目标不再可见时，进度条会下降。

## 预设配置

| 预设 | 范围 | 清晰 / 周边 | 发现时间 | 夜视 |
|---|---|---|---|---|
| `SightConfig.humanoid()` | 32 | 110° / 170° | 20 tick | 0.15 |
| `SightConfig.nocturnal()` | 40 | 100° / 160° | 12 tick | 0.8 |

```java
SightConfig.humanoid()
        .withRange(40)
        .withFov(90f, 150f)
        .withDetectTicks(30)
        .withDarkVision(0.5f)
        .withSeesInvisible(true)
        .withTargets(e -> e instanceof PlayerEntity || e instanceof VillagerEntity)
```

默认目标是玩家（不包括旁观者）：`SightConfig.PLAYERS`。

## 读取它看到的内容

```java
UUID target = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);   // 确定看到

SightSense sight = agent.perception().sense(SightSense.class);
float meter = sight.getDetection(player.getUuid());                    // 0..1
```
