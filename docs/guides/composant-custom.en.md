# Create a component

An `AgentComponent` is a piece of logic attached to the agent: it thinks every tick,
reads and writes the Blackboard, and can act on the mob. It's the ideal place for a character trait.

## Example: the creature fears light

Taken from the demo mod (the Abomination). The component **measures** and writes to the Blackboard;
a separate goal **decides** to flee. They only know each other through the Blackboard.

```java
public class PhotophobiaComponent implements AgentComponent {

    public static final BlackboardKey<Boolean> BLINDED =
            BlackboardKey.of(Identifier.of("mymod", "blinded"), Boolean.class);

    private final int threshold;
    private int cooldown;

    public PhotophobiaComponent(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void tick(Agent agent, ServerWorld world, int deltaTicks) {
        cooldown -= deltaTicks;
        if (cooldown > 0) return;
        cooldown = 5;

        int light = world.getLightLevel(agent.mob().getBlockPos().up());
        boolean blinded = light >= threshold;
        agent.blackboard().set(BLINDED, blinded);
        if (blinded) {
            agent.mob().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false));
        }
    }
}
```

```java
public class FleeLightGoal extends Goal {
    @Override
    public boolean canStart() {
        return Boolean.TRUE.equals(agent.blackboard().get(PhotophobiaComponent.BLINDED))
                && agent.awarenessState() != Awareness.HUNT;   // prey matters more than pain
    }
    // ... looks for the darkest spot and runs there
}
```

```java
Agent.builder(this)
        .component(new PhotophobiaComponent(10))
        ...
        .build();
goalSelector.add(2, new FleeLightGoal(this, agent, 1.3));
```

## Methods

| Method | When |
|---|---|
| `onAttach(agent)` | once, when the agent is built |
| `onLoad(agent, world)` | the mob spawns / its chunk loads |
| `tick(agent, world, deltaTicks)` | every think (after perception and alert) |
| `onUnload(agent)` | the mob disappears |

Get a component from anywhere with `agent.find(PhotophobiaComponent.class)`.
