# Sight

```java
.sight(SightConfig.humanoid())
```

## How it works

Every few ticks, the agent computes the **visibility** (0 to 1) of each target in range:

```
visibility = angle × line of sight × light × stealth × distance
```

| Factor | Detail |
|---|---|
| Angle | 1 in the sharp field of view, 0.3 in peripheral vision, 0 behind |
| Line of sight | rays toward the target's eyes and body; glass, panes and bars let the gaze through |
| Light | floor of 0.05 in total darkness (unless `darkVision`); a torch in hand or fire makes the target visible |
| Stealth | sneaking ×0.5, invisibility ×0.08, movement ×1.3, Glowing reveals, camouflage from the rules |
| Distance | down to −70% at the edge of range |

Each target fills a **detection meter**, like in a stealth game:

- from 35%: a *glimpse* — blurry position and identity (`kind = "glimpse"`);
- at 100%: the target is *seen* — exact position, `BlackboardKeys.VISIBLE_TARGET` set (`kind = "seen"`);
- under 2.5 blocks: instant detection.

The meter goes down when the target is no longer visible.

## Configurations

| Preset | Range | Sharp / peripheral | Detection | Night vision |
|---|---|---|---|---|
| `SightConfig.humanoid()` | 32 | 110° / 170° | 20 ticks | 0.15 |
| `SightConfig.nocturnal()` | 40 | 100° / 160° | 12 ticks | 0.8 |

```java
SightConfig.humanoid()
        .withRange(40)
        .withFov(90f, 150f)
        .withDetectTicks(30)
        .withDarkVision(0.5f)
        .withSeesInvisible(true)
        .withTargets(e -> e instanceof PlayerEntity || e instanceof VillagerEntity)
```

By default, targets are players (spectators excluded): `SightConfig.PLAYERS`.

## Reading what it sees

```java
UUID target = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);   // seen with certainty

SightSense sight = agent.perception().sense(SightSense.class);
float meter = sight.getDetection(player.getUuid());                    // 0..1
```
