# Créer un sens

Un sens implémente `SenseModule` et signale ce qu'il perçoit avec `perception.report(...)`.
La mémoire, l'alerte, les comportements et le debug fonctionnent alors automatiquement.

## Exemple : détection de chaleur

Une créature qui sent la chaleur des êtres vivants à travers les murs, mais seulement de près.

```java
public final class HeatSense implements SenseModule {

    public static final SenseType HEAT = new SenseType(Identifier.of("monmod", "heat"));
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
                    1.0 + distance * 0.2,   // incertitude
                    score,                  // importance
                    37f,                    // intensité (pour le debug)
                    "heat",                 // détail libre
                    player.getUuid()));
        }
    }
}
```

```java
Agent.builder(this).sense(new HeatSense(10)).awareness(AwarenessProfile.DEFAULT).build();
```

## Cycle de vie

| Méthode | Quand |
|---|---|
| `onLoad(agent, perception, world)` | le mob apparaît : s'enregistrer auprès d'un système, préparer des caches |
| `tick(agent, perception, world, deltaTicks)` | à chaque réflexion de l'agent (voir [LOD](../concepts/agent.md#niveau-de-detail-lod)) |
| `onUnload(agent, perception)` | le mob disparaît |

!!! tip "Lis les règles de l'agent"
    Si ton sens a des réglages qui dépendent du mod, lis `agent.rules()` plutôt que des constantes,
    ou propose un paramètre dans le constructeur du sens.
