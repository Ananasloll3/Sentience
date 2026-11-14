# Performance

Sentience is designed for dozens of agents per world.

| Mechanism | Effect |
|---|---|
| **Level of detail** | an agent far from players thinks 2 to 20 times less often, without losing reactivity |
| **Batched noises** | noises are dispatched once per tick; duplicates (same source, same type) are merged; 256 noises max per tick |
| **Two-step hearing** | block traversal (occlusion) is only computed if the noise would be audible without obstacles |
| **Cached background noise** | recomputed every second per listener, staggered between listeners |
| **Spaced-out sight** | one check every 3 to 4 ticks per agent |
| **Trails per chunk** | a scent lookup only scans the chunks around the nose |
| **No cost without agents** | if no agent is listening in a world, noises are dropped immediately |

## Tips

- Keep reasonable ranges (`withMaxRange`, `withRange`): they are what bounds the work.
- A purely decorative agent doesn't need `defaultGoals`.
- In your components, use `deltaTicks` and a countdown rather than doing everything every tick.
