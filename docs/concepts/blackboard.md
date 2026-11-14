# Blackboard

Le Blackboard est la **mémoire de travail** de l'agent, comme dans Unreal Engine : les composants y écrivent,
les comportements y lisent. Les clés sont typées.

## Clés standard

| Clé | Type | Écrite par |
|---|---|---|
| `BlackboardKeys.AWARENESS` | `Awareness` | alerte |
| `BlackboardKeys.FOCUS` | `Stimulus` | alerte — le souvenir le plus prioritaire |
| `BlackboardKeys.FOCUS_POSITION` | `Vec3d` | alerte — où l'agent **pense** que c'est |
| `BlackboardKeys.FOCUS_SOURCE` | `UUID` | alerte — qui, si identifié |
| `BlackboardKeys.VISIBLE_TARGET` | `UUID` | vue — cible vue avec certitude en ce moment |

## Tes propres clés

```java
public static final BlackboardKey<BlockPos> NID =
        BlackboardKey.of(Identifier.of("monmod", "nid"), BlockPos.class);

agent.blackboard().set(NID, pos);
BlockPos nid = agent.blackboard().get(NID);                  // null si absent
BlockPos ou = agent.blackboard().getOrDefault(NID, BlockPos.ORIGIN);
agent.blackboard().set(NID, null);                           // efface
```

## Écouter les changements

```java
agent.blackboard().addListener((key, ancien, nouveau) -> {
    if (key.equals(BlackboardKeys.VISIBLE_TARGET) && nouveau != null) {
        // une cible vient d'apparaître dans son champ de vision
    }
});
```

Les écouteurs ne sont appelés que si la valeur change réellement.
