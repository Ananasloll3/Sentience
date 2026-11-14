# 创建感官

感官需要实现 `SenseModule`，并通过 `perception.report(...)` 上报它感知到的内容。
之后，记忆、警觉、行为和调试都会自动生效。

## 示例：热感应

一种能隔墙感知生物体温的生物，但只能在近距离生效。

```java
public final class HeatSense implements SenseModule {

    public static final SenseType HEAT = new SenseType(Identifier.of("mymod", "heat"));
    private final double range;

    public HeatSense(double range) {
        this.range = range;
    }

    @Override
    public SenseType type() {
        return HEAT;
    }

    @Override
    public void tick(Agent agent, PerceptionComponent perception, ServerWorld world, int deltaTicks) {
        if (world.getTime() % 10 != 0) {
            return;
        }
        MobEntity mob = agent.mob();
        for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class,
                mob.getBoundingBox().expand(range), p -> !p.isSpectator())) {
            double distance = mob.distanceTo(player);
            float score = (float) (15 * (1 - distance / range));
            perception.report(new StimulusInput(HEAT, player.getEntityPos(),
                    1.0 + distance * 0.2,   // 不确定性
                    score,                  // 重要性
                    37f,                    // 强度（用于调试）
                    "heat",                 // 自由描述
                    player.getUuid()));
        }
    }
}
```

```java
Agent.builder(this).sense(new HeatSense(10)).awareness(AwarenessProfile.DEFAULT).build();
```

## 生命周期

| 方法 | 时机 |
|---|---|
| `onLoad(agent, perception, world)` | 生物生成：注册到某个系统、准备缓存 |
| `tick(agent, perception, world, deltaTicks)` | 智能体每次思考时（参见 [LOD](../concepts/agent.md#level-of-detail-lod)） |
| `onUnload(agent, perception)` | 生物消失 |

!!! tip "读取智能体的规则"
    如果你的感官有依赖于模组的设置，请读取 `agent.rules()` 而不是使用常量，
    或者在感官的构造函数中提供参数。
