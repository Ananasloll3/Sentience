# Pain and touch

## Pain

```java
.damage()
```

When the agent is hurt, it learns where the hit came from:

- **melee**: near-exact position of the attacker (score 40, `kind = "melee"`);
- **long-range shot**: attacker's position with an uncertainty that grows with distance (`"ranged"`);
- **projectile with no shooter**: the agent traces back the trajectory.

With the default thresholds, a hit triggers the hunt directly.

## Touch

```java
.touch()                                   // players
.touch(e -> e instanceof PlayerEntity || e instanceof VillagerEntity)
```

A target that brushes against the agent gives itself away (exact position, score 25), even in total
darkness and even if the agent is blind and deaf.
