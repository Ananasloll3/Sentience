# Hearing and voice

```java
.hearing(HearingProfile.KEEN)
```

## What is heard

**Every sound played on the server** is captured automatically and turned into a physical noise
(type + intensity in dB): footsteps, falls, swimming, blocks broken/placed/hit, doors, chests, pistons,
mechanisms, combat, projectiles, explosions, creature calls… Music, weather and UI are ignored.

| Benchmark | dB at the source |
|---|---|
| Whisper (microphone) | ≈ 35 |
| Footstep on wool, moss, snow | 42 |
| Footstep | 50 (sprint +6, **sneaking: no footstep sound**) |
| Normal voice (microphone) | 60 |
| Block broken | 62 |
| Shout (microphone) | ≈ 80 |
| Bell | 95 |
| Explosion | 125 |

## Propagation

For each listener:

```
perceived dB = source dB − 20·log10(distance) − 0.35 × distance − occlusion
```

- **Occlusion**: block-by-block traversal between the source and the ear. Wool and carpet −18 dB,
  stone ≈ −9, closed door −7, open door −0.5, leaves −1.5, glass −3.5…
- **Masking**: a noise is only heard if it exceeds the background noise — cave 12 dB, surface 18,
  rain 42, thunderstorm 50, flowing water, lava. A noise above 85 dB **deafens** temporarily.
- **Localisation**: good for direction, poor for distance; blurrier the fainter or more muffled the sound.

All these values can be tuned **per mod**: see [Your mod's rules](../guides/regles.md).

## Profiles

| Profile | Threshold | Range | Localisation |
|---|---|---|---|
| `HearingProfile.DEFAULT` | 15 dB | 64 | ×1 |
| `HearingProfile.KEEN` | 10 dB | 80 | ×1.4 |
| `HearingProfile.DULL` | 25 dB | 40 | ×0.7 |

```java
HearingProfile.KEEN.withThreshold(12f).withMaxRange(72).withLocalization(1.3)
```

## Filtering what interests it

By default, interest depends on the noise type (a voice is more interesting than a sheep), and the agent
ignores spectators and its own kind. You can change that:

```java
.hearing(HearingProfile.DEFAULT, (agent, event) -> {
    if (event.type() == NoiseType.CREATURE) return 0;          // ignores animals
    if (event.type() == NoiseType.VOICE) return 1.5;           // loves voices
    return event.type().defaultInterest();
})
```

## Voice (Simple Voice Chat)

If Simple Voice Chat is installed, each player's microphone level is measured server-side,
calibrated automatically (every microphone has its own gain) and turned into a `VOICE` noise.
Whisper mode counts 15 dB less.

Another voice mod can feed Sentience:

```java
VoiceInput.submit(player.getUuid(), levelDbfs, whispering);   // from any thread
```

## Emitting your own noises

```java
HearingSystem.report(world, pos, 90f, NoiseType.MECHANISM, sourceEntity);
```

`NoiseType`: `VOICE`, `FOOTSTEP`, `COMBAT`, `BLOCK`, `MECHANISM`, `PROJECTILE`, `EXPLOSION`,
`ITEM`, `CREATURE`, `GENERIC`.
