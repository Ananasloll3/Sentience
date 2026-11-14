# Blackboard

The Blackboard is the agent's **working memory**, as in Unreal Engine: components write to it,
behaviours read from it. Keys are typed.

## Standard keys

| Key | Type | Written by |
|---|---|---|
| `BlackboardKeys.AWARENESS` | `Awareness` | alert |
| `BlackboardKeys.FOCUS` | `Stimulus` | alert — the highest-priority memory |
| `BlackboardKeys.FOCUS_POSITION` | `Vec3d` | alert — where the agent **thinks** it is |
| `BlackboardKeys.FOCUS_SOURCE` | `UUID` | alert — who, if identified |
| `BlackboardKeys.VISIBLE_TARGET` | `UUID` | sight — target seen with certainty right now |

## Your own keys

```java
public static final BlackboardKey<BlockPos> NEST =
        BlackboardKey.of(Identifier.of("mymod", "nest"), BlockPos.class);

agent.blackboard().set(NEST, pos);
BlockPos nest = agent.blackboard().get(NEST);                 // null if absent
BlockPos where = agent.blackboard().getOrDefault(NEST, BlockPos.ORIGIN);
agent.blackboard().set(NEST, null);                           // clears it
```

## Listening for changes

```java
agent.blackboard().addListener((key, oldValue, newValue) -> {
    if (key.equals(BlackboardKeys.VISIBLE_TARGET) && newValue != null) {
        // a target just appeared in its field of view
    }
});
```

Listeners are only called when the value actually changes.
