# Smell

```java
.scent(ScentSense.hound())
```

## Trails

Players leave a **scent point** every 10 ticks (0.5 s). Each point records whether it was left
**in water** or **sneaking**. A wounded creature leaves **blood** — a 2.5× stronger trace — and
bleeds while walking below 40% health.

By default:

- a trace lasts **2 minutes**;
- **rain** washes it away twice as fast;
- **water** doesn't hold scent: crossing a river breaks the trail;
- scent goes through **at most one solid block** between the trace and the nose.

Everything can be tuned per mod through the [rules](../guides/regles.md#smell).

## The nose

Every 10 ticks, the agent looks for the strongest trace in range (the freshest one at equal strength,
i.e. the one *ahead*, in the direction of travel) and reports it. By following it, it tracks the trail
step by step. At the end, the last trace no longer sets it off: it searches around, then gives up.

```java
ScentSense.hound()                         // 8 blocks, all trails
ScentSense.of(12, 15f)                     // 12 blocks, more sensitive
ScentSense.hound().playersOnly()           // players only
ScentSense.hound().only(p -> p.kind().equals(ScentTrails.BLOOD))   // blood only
```

## Making other entities leave a trail

```java
ScentTrails.addEmitter(EntityType.VILLAGER);
```

This is additive: each mod adds its types without removing other mods' ones.

## Depositing scents

```java
ScentTrails.deposit(world, pos, 3f, baitUuid);             // custom scent (bait, corpse…)
ScentTrails.deposit(world, entity, 1f, ScentTrails.TRAIL); // an entity's trace at its position
```

## Seeing trails

`/sentience debug` shows recent trails around you:
**green** = trace, **red** = blood, **grey** = in water.
`/sentiencetest piste <from> <to>` (demo mod) lays a trail instantly.
