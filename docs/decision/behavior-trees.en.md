# Behavior Trees

A **Behavior Tree** describes a mob's whole behaviour as a tree, like in Unreal:
**selectors** pick the highest-priority branch, **sequences** chain steps,
**decorators** add conditions and **services** run in the background.

You can write them **in JSON** in a datapack (hot-reloaded with `/reload`) or **in Java**.

```java
Agent.builder(this)
        .hearing(HearingProfile.DEFAULT)
        .sight(SightConfig.humanoid())
        .damage()
        .awareness(AwarenessProfile.DEFAULT)
        .behaviorTree(Identifier.of("mymod", "guard"), 1)   // (1)!
        .build();
```

1. The tree is run by a priority-1 goal that takes the mob's **movement** and **look** controls.
   Higher-priority goals (swimming…) come first; the tree then restarts from the root.
   Don't use `defaultGoals` at the same time: the tree replaces them.

## In JSON

File `data/mymod/sentience/behavior_tree/guard.json` → identifier `mymod:guard`.

```json
{
  "root": {
    "type": "selector",
    "children": [
      {
        "name": "fight",
        "type": "attack",
        "target": "visible_target",
        "decorators": [ { "type": "sees_target", "abort": "both" } ],
        "services": [ { "type": "share_focus", "range": 32, "interval": 40 } ]
      },
      {
        "name": "investigate",
        "type": "sequence",
        "decorators": [ { "type": "awareness", "at_least": "investigate", "abort": "lower" } ],
        "children": [
          { "type": "move_to", "target": "focus_position", "speed": 1.1 },
          { "type": "search", "radius": 6, "ticks": 160 },
          { "type": "give_up" }
        ]
      },
      { "type": "wander", "radius": 10, "speed": 0.7 }
    ]
  }
}
```

Every node has a `type` and also accepts:

| Field | Role |
|---|---|
| `name` | name shown by `/sentience debug` |
| `decorators` | list of decorators (the first one is the outermost) |
| `services` | list of services, with `interval` in ticks (20 by default) |

An identifier without a namespace (`sequence`, `focus_position`) refers to Sentience's.
Blackboard keys are written by their identifier: `focus_position`, `visible_target`,
`mymod:hiding_spot`…

!!! tip "Hot editing"
    Edit the file, type `/reload`: every agent rebuilds its tree immediately.
    Errors are written to the server log with the file name.
    A datapack can also **replace** a mod's tree by reusing its identifier.

## Execution

Every tick, the tree is walked from the root. Each node returns **SUCCESS**, **FAILURE**
or **RUNNING** (not finished, it will resume next tick).

| Composite | Behaviour |
|---|---|
| `sequence` | children in order; fails as soon as a child fails |
| `selector` | children by priority; succeeds as soon as a child succeeds |
| `random_selector` | like `selector`, in random order |
| `parallel` | all children at once; `"require": "all"` or `"one"` |
| `subtree` | inserts another tree: `"tree": "mymod:combat"` |

### Aborts (`abort`)

A condition used as a decorator accepts `abort`, like Unreal's *observer aborts*:

| `abort` | Effect |
|---|---|
| `none` | checked only when entering the branch |
| `self` | the branch stops as soon as the condition becomes false |
| `lower` | as soon as it becomes true, it interrupts a running **lower-priority** branch |
| `both` | both |

This is what makes the tree reactive: patrolling is interrupted as soon as a noise raises the alert,
fighting stops as soon as the target is no longer visible.

## Reference

### Tasks

| Type | Parameters (default) |
|---|---|
| `wait` | `ticks` (20), `deviation` (0) |
| `move_to` | `target` (focus_position), `speed` (1), `acceptance` (1.5), `timeout` (0 = none) |
| `wander` | `radius` (8), `speed` (0.8) |
| `search` | `target` (focus_position), `radius` (6), `ticks` (200), `speed` (0.8) |
| `flee` | `from` (focus_position), `distance` (12), `speed` (1.3) |
| `look_at` | `target` (focus_position), `ticks` (0 = one tick) |
| `attack` | `target` (visible_target), `speed` (1.3), `cooldown` (20) — melee |
| `run_query` | `query`, `result` (query_result) — see [environment queries](eqs.md) |
| `set` | `key`, `value` or `from` (copies another key) |
| `clear` | `key` |
| `play_sound` | `sound`, `volume` (1), `pitch` (1) |
| `emit_noise` | `db` (80), `noise` (generic) — heard by agents |
| `share_focus` | `range` (32), `same_type` (true) |
| `give_up` | forgets the current stimulus |
| `alert` | `level`: raises the alert to at least this level |
| `set_attack_target` | `target`: vanilla target, for vanilla goals |
| `succeed`, `fail` | succeeds / fails |
| `check` | `condition`: succeeds if the condition is true |

A condition can also be used directly as a node: `{ "type": "sees_target" }`.

`move_to`, `look_at`, `attack`… accept a key holding a position (`Vec3d`), a block (`BlockPos`),
an entity (its UUID) or a stimulus.

### Conditions

Usable as decorators (with `abort`), in `check`, or in `not` / `all` / `any`.
All accept `"negate": true`.

| Type | Parameters |
|---|---|
| `awareness` | `at_least`, `is` or `below`: `idle`, `suspicious`, `investigate`, `hunt` |
| `sees_target` | a target is seen with certainty |
| `is_set` | `key` |
| `equals` | `key`, `value` |
| `alive` | `target` (visible_target) |
| `health` | `below` or `above` (fraction of max health) |
| `distance` | `target`, `below` and/or `above` |
| `light` | `below` and/or `above` (0-15) |
| `is_night`, `is_day`, `in_water` | — |
| `chance` | `chance` (0.5) |
| `not` | `condition` |
| `all`, `any` | `conditions` |

### Decorators

In addition to conditions:

| Type | Parameters |
|---|---|
| `cooldown` | `ticks` (100): fails while the branch finished less than `ticks` ago |
| `time_limit` | `ticks` (200): interrupts and fails beyond that |
| `repeat` | `count` (-1 = forever): restarts while the child succeeds |
| `invert`, `force_success`, `force_failure` | change the result |

They can also be used as nodes with a `child`.

### Services

| Type | Parameters |
|---|---|
| `run_query` | `query`, `result`: refreshes a point regularly |
| `share_focus` | `range`, `same_type` |
| `copy` | `from`, `to` |
| `look_at` | `target` |

## In Java

```java
public static final BehaviorTree GUARD = BehaviorTrees.register(Identifier.of("mymod", "guard"), () ->
        Bt.selector(
                Bt.attack(BlackboardKeys.VISIBLE_TARGET, 1.3, 20)
                        .guard(Bt.seesTarget(), BtAbort.BOTH)
                        .service(40, ctx -> AgentComms.shareFocus(ctx.agent(), 32, other -> true)),
                Bt.sequence(
                        Bt.moveTo(BlackboardKeys.FOCUS_POSITION, 1.1),
                        Bt.search(BlackboardKeys.FOCUS_POSITION, 6, 160, 0.8),
                        Bt.giveUp())
                        .guard(Bt.awarenessAtLeast(Awareness.INVESTIGATE), BtAbort.LOWER),
                Bt.wander(10, 0.7)));

Agent.builder(this)...behaviorTree(GUARD, 1).build();
```

The factory is called **once per agent**: each mob has its own nodes.
A JSON tree with the same identifier replaces the Java tree.

### Writing a task

```java
public class HowlTask extends BtNode {
    private int remaining;

    @Override
    protected void onStart(BtContext ctx) {
        remaining = 40;
        ctx.mob().playSound(SoundEvents.ENTITY_WOLF_HOWL, 3f, 0.8f);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        remaining -= ctx.deltaTicks();
        return remaining <= 0 ? BtStatus.SUCCESS : BtStatus.RUNNING;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        // clean-up (aborted = interrupted by a higher-priority branch)
    }
}
```

To make it available in JSON:

```java
BtRegistry.registerNode(Identifier.of("mymod", "howl"), json -> HowlTask::new);
BtRegistry.registerCondition(Identifier.of("mymod", "full_moon"),
        json -> ctx -> ctx.world().getMoonPhase() == 0);
BtRegistry.registerService(Identifier.of("mymod", "sniff"),
        json -> ctx -> ctx.mob().playSound(SoundEvents.ENTITY_WOLF_SNIFF, 1f, 1f));
```

JSON parameters are read with `SentienceJson` (`number`, `integer`, `string`, `key`…).
Conditions and services must be **stateless**: a single instance is shared by every agent.

## Debug

`/sentience debug` shows the current task above each agent: `HUNT 👂 22.2 ±2.9 ▶ attack`.
