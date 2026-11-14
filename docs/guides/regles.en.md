# Your mod's rules

The "physical" rules of perception (rain, materials, darkness, camouflage…) have realistic defaults,
but **each mod can have its own**.

!!! success "No conflict between mods"
    Rules apply **on the perceiver's side**: the same noise is heard by your mod's mobs
    with your rules, and by another mod's mobs with theirs. Nobody changes global rules.

## Creating your rules

```java
public static final SentienceRules RULES = SentienceRules.create("mymod")
        .hearing(h -> h
                .ignoreRain()
                .muffled(25f)
                .addAmbientSource((world, pos) -> isBlizzard(world, pos) ? 55f : Float.NaN)
                .addNoiseModifier((event, db) -> isMyMachine(event.soundId()) ? Float.NaN : db))
        .sight(s -> s
                .sneak(0.3f)
                .addStealthModifier((observer, target) -> wearsCamo(target) ? 0.2f : 1f))
        .scent(s -> s.ignoreRain().lifetime(6000).blood(3f))
        .build();

Agent.builder(this).rules(RULES)...build();
```

Without `.rules(...)`, an agent uses `SentienceRules.DEFAULT`, which never changes.

### Variants

A rule set can be derived for a specific mob:

```java
public static final SentienceRules BOSS_RULES = RULES.derive(Identifier.of("mymod", "boss"))
        .sight(s -> s.invisibility(1f))      // sees invisible players
        .build();
```

## Hearing

| Setting | Default | Method |
|---|---|---|
| Air absorption | 0.35 dB/block | `airAbsorption(db)` |
| Maximum occlusion | 60 dB | `maxOcclusion(db)` |
| Deafening threshold | 85 dB | `deafening(db)` |
| Background noise cave / surface / rain / thunderstorm | 12 / 18 / 42 / 50 dB | `ambient(...)`, `ignoreRain()` |
| Flowing water / lava | 38 / 30 dB | `fluidAmbient(water, lava)` |
| Wool, carpet | 18 dB | `muffled(db)` |
| Closed / open door | 7 / 0.5 dB | `doors(closed, open)` |
| Leaves | 1.5 dB | `leaves(db)` |
| Solid block | 9 + hardness × 0.2 (unbreakable 25) | `solids(base, factor, unbreakable)` |
| Thin block (glass, slab…) | 3.5 dB | `thinSolids(db)` |
| Water / lava crossed | 4 / 6 dB | `fluids(water, lava)` |

**Extension points:**

- `addNoiseModifier((event, db) -> newDb)` — changes a noise's intensity, or `Float.NaN` to ignore it.
  `event.soundId()` gives the original Minecraft sound (e.g. `minecraft:block.wool.step`).
- `addBlockAcoustics((world, pos, state) -> dB or null)` — a block's attenuation, takes priority over the built-in rules.
- `addAmbientSource((world, pos) -> dB or Float.NaN)` — extra background noise.

## Sight

| Setting | Default | Method |
|---|---|---|
| Visibility in total darkness | 0.05 | `darknessFloor(v)` |
| Peripheral vision | ×0.3 | `peripheral(f)` |
| Sneaking | ×0.5 | `sneak(f)` |
| Invisibility | ×0.08 | `invisibility(f)` |
| Movement | ×1.3 | `movement(f)` |
| Torch in hand / fire / Glowing reveal | yes | `lightInHandReveals`, `fireReveals`, `glowingReveals` |
| Instant detection | < 2.5 blocks | `instantDetection(distance)` |
| See-through blocks | glass, panes, bars | `seeThrough(predicate)` |

**Extension point:** `addStealthModifier((observer, target) -> factor)` — camouflage, cloaks, paint…

## Smell

| Setting | Default | Method |
|---|---|---|
| Trace lifetime | 2400 ticks (max 12000) | `lifetime(ticks)` |
| Rain effect | lifetime ×0.5 | `rainLifetime(f)`, `ignoreRain()` |
| Water breaks the trail | yes | `waterBreaksTrail(bool)` |
| Trace while sneaking | ×1 | `sneakStrength(f)` |
| Blood attraction | ×1 | `blood(f)` |
| Walls scent can cross | 1 solid block | `maxWallBlocks(n)` (-1 = unlimited) |

**Extension point:** `addPointModifier((agent, point) -> factor)` — anti-scent potion, preferred prey…
