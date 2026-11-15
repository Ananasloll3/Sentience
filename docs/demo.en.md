# Demo mod

The repository contains a separate demo mod (`src/testmod`), never published with the library.
It shows how to use each feature, and provides a map of test arenas.

```bash
./gradlew runTestmodClient
```

![The demo mobs](assets/mobs.png)

## The creatures

| Creature | What it tests |
|---|---|
| **Stalker** | blind: hearing only (+ microphone), touch, pain |
| **Sentinel** | deaf and motionless: sight only. Shoots at what it really sees, rings an alarm heard by every creature, warns its allies by radio |
| **Hound** | smell: follows your trail step by step. Its rules: rain doesn't wash the trail away for it |
| **Abomination** | everything at once + a custom component (fears light) + a goal that reads the Blackboard; warns other creatures when it hunts |
| **Hunter** | its whole behaviour is a [JSON Behavior Tree](decision/behavior-trees.md): patrols dark corners, flanks its prey, hides when wounded ([environment queries](decision/eqs.md)), warns its kind |
| **Zombies** (vanilla) | hearing and sight of players and villagers, plus a second template that makes them smell blood (two combined `AgentTemplates`) |

The demo mod has **its own rules**: a carved pumpkin on your head acts as camouflage for its
creatures. Zombies keep the default rules — the pumpkin doesn't fool them.

## The "Sentience - Arènes de test" map

Available in the [releases](https://github.com/Ananasloll3/Sentience/releases). Unzip it into
`run/saves`. You spawn at night, at the hub; the arenas are along the path.

| Arena | Content |
|---|---|
| **Stalker** | 4 rooms: gravel, wool (muffled steps), waterfall (masks noises), mechanisms and a wool hideout |
| **Sentinel** | courtyard half lit, half dark; cover, glass, bars; chest (invisibility, torches, pumpkin); a Stalker in the kennel that rushes in at the alarm |
| **Hound** | winding path, pen to open, river that breaks the trail, cabin at the end |
| **Abomination** | dark house, one lamp and one lever per room, wardrobe, carpets |

`/sentiencetest arene <rodeur|sentinelle|flaireur|abomination|toutes>` rebuilds an arena anywhere.

!!! tip "Editing the Hunter live"
    Its tree is in `src/testmod/resources/data/sentience_testmod/sentience/behavior_tree/hunter.json`.
    Edit it, then `/reload` in game.
