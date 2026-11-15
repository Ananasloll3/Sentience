# Changelog

## 0.2.0

### Added
- **Behavior Trees**: sequences, selectors, random selectors, parallels, subtrees; decorators
  (conditions with `abort: none | self | lower | both`, cooldown, time limit, repeat, invert…);
  services; built-in tasks (move, wander, search, flee, look, melee attack, environment queries,
  Blackboard, sounds, noises, alerts, focus sharing).
  - Written in **JSON** in datapacks (`data/<ns>/sentience/behavior_tree/`), hot-reloaded with `/reload`,
    or in **Java** with `Bt` and `BehaviorTrees.register`.
  - `AgentBuilder.behaviorTree(id | tree, priority)`.
  - Custom tasks, conditions, services and decorators for JSON through `BtRegistry`.
- **Environment queries (EQS)**: ring and grid generators; distance, visibility, cover, angle, light
  and random tests (filter and score); reachability check.
  - Built-in queries `sentience:hide`, `sentience:cover`, `sentience:flank`, `sentience:flee`.
  - JSON queries in `data/<ns>/sentience/env_query/`, or Java with `EnvQuery.builder`.
- **Multiple templates per mob type**: `AgentTemplates` now combines the templates of every mod, by priority.
- Debug: the running task is shown above each agent; queries display their points.
- Demo mod: the **Hunter**, driven by a JSON Behavior Tree; zombies also smell blood (second template).
- `BlackboardKey.byId`, `BlackboardKeys.QUERY_RESULT`, `SightSense.hasLineOfSight`, `Ground`.

### Changed
- `AgentBuilder`: adding a sense or component of a class already present replaces it.

## 0.1.0

First release: agents, Blackboard, physical hearing and voice, sight, smell, pain, touch,
communication, memory, alert levels, per-mod rules, debug tools, demo mod.
