# Vanilla and other mods' mobs

`AgentTemplates` gives an agent to every mob of an existing type, without touching its code.
The template is applied each time the mob spawns or is loaded.

```java
@Override
public void onInitialize() {
    AgentTemplates.register(EntityType.ZOMBIE, agent -> agent
            .rules(MyMod.RULES)
            .hearing(HearingProfile.DEFAULT)
            .sight(SightConfig.humanoid()
                    .withTargets(e -> SightConfig.PLAYERS.test(e) || e instanceof VillagerEntity))
            .damage()
            .defaultGoals(3, 1.0, 1.2));
}
```

Sentience's behaviours are added to the mob's own: a zombie keeps its vanilla attack
(priority 2) and uses "investigate" (priority 3) when it has no target.

## Several templates for the same type

Several mods can target the same type: their templates are **combined** on the same agent,
by increasing priority (then in registration order).

```java
// Another mod adds smell to zombies, without removing hearing and sight
AgentTemplates.register(EntityType.ZOMBIE, 10, agent -> agent
        .scent(ScentSense.hound().only(p -> p.kind().equals(ScentTrails.BLOOD))));
```

- senses and components **add up**;
- a sense or component **of the same class** is replaced by the one from the highest-priority template;
- so are the rules, the alert, the default behaviours and the Behavior Tree.

For your own mobs, build the agent directly in the mob instead.

## Entities without an agent

An entity that isn't a mob (or that only wants to hear) can implement `HearingListener`
and register by hand:

```java
HearingSystem.register(world, myListener);   // on load
HearingSystem.unregister(myListener);        // on unload
```
