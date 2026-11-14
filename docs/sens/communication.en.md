# Communication

An agent can share its focus with its allies. The information arrives **degraded**: uncertainty +3 blocks,
score ×0.8 (`SenseType.REPORT`, `kind = "report"`).

```java
// Warn mobs of the same type within 48 blocks
int warned = AgentComms.shareFocus(agent, 48, AgentComms.sameType(agent));

// Warn every nearby agent
AgentComms.shareFocus(agent, 40, other -> true);
```

A typical use: in `onAwarenessChanged`, when the agent starts hunting.

## Sound alarm

For an alert that **every** creature with hearing can pick up (through walls, with sound physics),
simply emit a loud noise:

```java
HearingSystem.report(world, mob.getEyePos(), 105f, NoiseType.MECHANISM, mob);
```

The Sentinel in the [demo mod](../demo.md) does both: radio for its allies, a bell for everyone.
