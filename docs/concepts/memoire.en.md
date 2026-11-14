# Memory and stimuli

Each sense produces **raw perceptions** (`StimulusInput`). The memory (`StimulusMemory`) fuses them
into **stimuli**: memories like "there's something, roughly over there".

## What a stimulus contains

| Accessor | Meaning |
|---|---|
| `getPosition()` | **estimated** position |
| `getErrorRadius()` | uncertainty, in blocks |
| `getScore(now)` | current priority (decays) |
| `getLastSense()`, `getSenses()`, `wasSensedBy(sense)` | which senses confirmed it |
| `getKind()` | detail: `footstep`, `voice`, `seen`, `glimpse`, `scent`, `blood`, `melee`… |
| `getSourceId()` | identified entity, if known |
| `getPeakIntensity()` | physical measurement (dB for hearing…) |
| `getCount()`, `getFirstTick()`, `getLastTick()` | history |

## Fusion

Two perceptions close together, or from the same source, are fused:

- **Same place**: positions are averaged, weighted by precision (1/σ²).
  Uncertainty shrinks — like triangulation.
- **Same source that moved**: the new position **replaces** the old one, so the agent never aims
  at a stale point.
- Each repetition adds a small priority bonus: repeated footsteps end up drawing attention.

## Decay

The score decays with a half-life of 160 ticks. A stimulus is forgotten when its score becomes too low
or it exceeds the memory duration (600 ticks by default, `.memory(capacity, ticks)`).

## Direct use

```java
StimulusMemory memory = agent.perception().memory();
Stimulus best = memory.best(world.getTime());
Stimulus player = memory.findBySource(player.getUuid());
memory.forgetNear(pos, 5.0);

// Inject information (e.g. an ally warns you, a trap triggers)
agent.perception().report(new StimulusInput(SenseType.REPORT, pos, 3.0, 20f, 0f, "trap", null));
```

## Score benchmarks

| Situation | Typical score |
|---|---|
| Footstep at 8 blocks | ≈ 6 |
| Normal voice at 10 blocks | ≈ 18 |
| Glimpse (sight meter at 35%) | ≈ 4 |
| Target clearly seen | 30 |
| Touch | 25 |
| Hit taken | 40 |
