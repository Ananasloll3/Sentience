# Alert levels and behaviours

The `AwarenessComponent` turns the best stimulus into an **alert level**. It rises immediately,
and comes down one step at a time after a delay.

| Level | Default behaviour | Delay before stepping down |
|---|---|---|
| `IDLE` | wanders normally | — |
| `SUSPICIOUS` | freezes, turns its head toward the noise (`ListenGoal`) | 40 ticks |
| `INVESTIGATE` | walks over to check, then searches the area (`InvestigateGoal`) | 80 ticks |
| `HUNT` | runs to the last known position | 140 ticks |

## Thresholds

```java
.awareness(new AwarenessProfile(0.5f, 6f, 18f))  // suspicious, investigate, hunt
```

| Profile | Suspicious | Investigate | Hunt |
|---|---|---|---|
| `AwarenessProfile.DEFAULT` | 0.5 | 6 | 18 |
| `AwarenessProfile.PARANOID` | 0.3 | 4 | 13 |
| `AwarenessProfile.CALM` | 2 | 10 | 25 |

See the [score benchmarks](memoire.md#score-benchmarks).

## Ready-to-use behaviours

`.defaultGoals(priority, walk, run)` adds:

- **`InvestigateGoal`** (given priority): goes to the estimated position, then searches the uncertainty
  area at random points. With no new information for `searchDuration` ticks, it drops that stimulus.
  Every new perception sends it back toward the updated position.
- **`ListenGoal`** (priority + 1): in `SUSPICIOUS`, stops and looks around the assumed direction.

You can also instantiate them yourself:

```java
goalSelector.add(2, new InvestigateGoal(agent, 0.9, 1.4, 240));
goalSelector.add(3, new ListenGoal(agent));
```

## Driving the alert

```java
AwarenessComponent alert = agent.awareness();
alert.escalate(Awareness.HUNT);   // forces a minimum level
alert.giveUp(alert.focus());      // forgets the current stimulus
```

## Writing your own behaviours

Read the level and the Blackboard in your goals:

```java
@Override
public boolean canStart() {
    return agent.awarenessState() == Awareness.HUNT
            && agent.blackboard().has(BlackboardKeys.VISIBLE_TARGET);
}
```
