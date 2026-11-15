# Environment queries (EQS)

An **environment query** (Unreal's *Environment Query System*) answers questions such as
"where do I hide?", "where do I take cover?", "which way do I flank it?". It:

1. **generates** candidate points around a target (on the ground, where a mob can stand);
2. **filters** and **scores** them with tests;
3. returns the best one, checking if needed that it can be reached.

## Built-in queries

The **threat** is the target seen with certainty, otherwise the focus position.

| Identifier | Finds |
|---|---|
| `sentience:hide` | a nearby spot where the threat can't see you, rather far from it |
| `sentience:cover` | very close cover, with a solid block between you and the threat |
| `sentience:flank` | a point on the threat's side, to go around it |
| `sentience:flee` | a point far from the threat, out of its sight if possible |

In a Behavior Tree:

```json
{ "type": "run_query", "query": "sentience:hide", "result": "mymod:hiding_spot" },
{ "type": "move_to", "target": "mymod:hiding_spot", "speed": 1.4 }
```

`run_query` fails if no point is valid: the sequence stops and the tree moves on.

## Writing a query in JSON

File `data/mymod/sentience/env_query/dark_corner.json` → identifier `mymod:dark_corner`.

```json
{
  "generator": { "type": "ring", "center": "querier", "radius": [4, 14], "rings": 3, "points": 10 },
  "tests": [
    { "type": "light", "max": 9, "weight": 2, "prefer": "low" },
    { "type": "visible_from", "target": "threat", "max": 0 },
    { "type": "distance", "target": "querier", "weight": 0.5, "prefer": "high" }
  ],
  "pathable": true
}
```

### Generators

| Type | Parameters |
|---|---|
| `ring` | `center`, `radius` (number or `[min, max]`), `rings` (3), `points` per ring (12) |
| `grid` | `center`, `radius` (10), `spacing` (2) |

### Targets

`querier` (the agent), `threat`, `focus` (focus position) or the identifier of a Blackboard key.

### Tests

| Type | Measure |
|---|---|
| `distance` | distance between the point and `target` |
| `visible_from` | 1 if `target` sees the point (at head height, through glass), 0 otherwise |
| `cover` | 1 if a solid block protects the point on `target`'s side |
| `dot` | cosine of the angle, seen from `from`, between the point and `to` (1 = same direction, 0 = to the side) |
| `light` | light level at the point (0-15) |
| `random` | random value, as a tie-breaker |

Each test can:

- **filter**: `min` and/or `max` remove points outside the range;
- **score**: `weight` (0 = filter only) and `prefer` (`high` or `low`). Values are normalised
  between 0 and 1 over the remaining points, then added up.

`"pathable": true` keeps only a reachable point: the path is only computed for the
best points, until one is found.

## In Java

```java
public static final EnvQuery HIDING_SPOT = EnvQueries.register(Identifier.of("mymod", "hiding_spot"),
        EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 4, 16, 3, 12))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))    // filter: hidden
                .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(1))   // far from the threat
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1.5)) // not too far from me
                .pathable()
                .build());

Vec3d spot = HIDING_SPOT.run(agent).best();   // null if nothing fits
```

A custom measure is a function that receives the agent and the world, and returns the measure of each point:

```java
QueryTest.of((agent, world) -> point -> world.getBlockState(BlockPos.ofFloored(point).down()).isOf(Blocks.GRASS_BLOCK) ? 1 : 0)
        .preferHigh(1);
```

## Debug

With `/sentience debug`, each query shows its points: **grey** = removed, **green** = kept,
and sparkles on the best one.

## Performance

A query costs a few dozen block or line-of-sight tests, plus a few path computations
with `pathable`. Run it when entering a branch (`run_query`) or with a spaced-out service (20 ticks or more),
not every tick.
