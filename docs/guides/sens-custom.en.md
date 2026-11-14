# Create a sense

A sense implements `SenseModule` and reports what it perceives with `perception.report(...)`.
Memory, alert, behaviours and debug then work automatically.

## Example: heat detection

A creature that senses the warmth of living beings through walls, but only up close.

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
                    1.0 + distance * 0.2,   // uncertainty
                    score,                  // importance
                    37f,                    // intensity (for debug)
                    "heat",                 // free-form detail
                    player.getUuid()));
        }
    }
}
```

```java
Agent.builder(this).sense(new HeatSense(10)).awareness(AwarenessProfile.DEFAULT).build();
```

## Lifecycle

| Method | When |
|---|---|
| `onLoad(agent, perception, world)` | the mob spawns: register with a system, prepare caches |
| `tick(agent, perception, world, deltaTicks)` | on every think of the agent (see [LOD](../concepts/agent.md#level-of-detail-lod)) |
| `onUnload(agent, perception)` | the mob disappears |

!!! tip "Read the agent's rules"
    If your sense has settings that depend on the mod, read `agent.rules()` rather than constants,
    or offer a parameter in the sense's constructor.
