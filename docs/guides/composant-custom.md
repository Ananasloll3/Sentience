# Créer un composant

Un `AgentComponent` est une brique de logique attachée à l'agent : il réfléchit à chaque tick,
lit et écrit le Blackboard, et peut agir sur le mob. C'est l'endroit idéal pour un trait de caractère.

## Exemple : la créature craint la lumière

Tiré du mod de démo (l'Abomination). Le composant **mesure** et écrit dans le Blackboard ;
un goal séparé **décide** de fuir. Les deux ne se connaissent que par le Blackboard.

```java
public class PhotophobiaComponent implements AgentComponent {

    public static final BlackboardKey<Boolean> BLINDED =
            BlackboardKey.of(Identifier.of("monmod", "blinded"), Boolean.class);

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
                && agent.awarenessState() != Awareness.HUNT;   // la proie compte plus que la douleur
    }
    // ... cherche l'endroit le plus sombre et y court
}
```

```java
Agent.builder(this)
        .component(new PhotophobiaComponent(10))
        ...
        .build();
goalSelector.add(2, new FleeLightGoal(this, agent, 1.3));
```

## Méthodes

| Méthode | Quand |
|---|---|
| `onAttach(agent)` | une fois, à la construction de l'agent |
| `onLoad(agent, world)` | le mob apparaît / son chunk se charge |
| `tick(agent, world, deltaTicks)` | chaque réflexion (après la perception et l'alerte) |
| `onUnload(agent)` | le mob disparaît |

Récupère un composant depuis n'importe où avec `agent.find(PhotophobiaComponent.class)`.
