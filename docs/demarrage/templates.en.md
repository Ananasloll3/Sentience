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

!!! warning "One template per type"
    If two mods register a template for the same type, the last one registered wins.
    For your own mobs, build the agent inside the mob itself instead.

## Entities without an agent

An entity that isn't a mob (or that only wants to hear) can implement `HearingListener`
and register by hand:

```java
HearingSystem.register(world, myListener);   // on load
HearingSystem.unregister(myListener);        // on unload
```
