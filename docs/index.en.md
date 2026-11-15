---
hide:
  - navigation
---

# Sentience

**AI that truly perceives the world, for your Minecraft mods.**

Sentience is a Fabric library (Minecraft 1.21.10) inspired by Unreal Engine's AI tools.
It gives any mob — yours, a vanilla mob, or another mod's — an **agent**:
realistic senses, a memory, a Blackboard and alert levels, with ready-to-use behaviours.

![The demo mobs](assets/mobs.png)

<div class="grid cards" markdown>

-   :material-ear-hearing: **Realistic hearing**

    ---

    Every game sound is captured and propagated physically: falloff, walls, materials,
    background noise that masks your footsteps. **Players' voices** (Simple Voice Chat) too.

    [:octicons-arrow-right-24: Hearing and voice](sens/ouie.md)

-   :material-eye: **Believable sight**

    ---

    Vision cone, peripheral vision, darkness, stealth, camouflage, and a progressive detection
    meter like in a stealth game.

    [:octicons-arrow-right-24: Sight](sens/vue.md)

-   :material-dog-side: **Smell and trails**

    ---

    Players leave a trail that water breaks and rain washes away. Blood attracts.
    Trackers follow it step by step.

    [:octicons-arrow-right-24: Smell](sens/odorat.md)

-   :material-brain: **Memory and alert**

    ---

    The agent never knows your real position: it fuses its perceptions, guesses, goes to check,
    searches, hunts, then gives up.

    [:octicons-arrow-right-24: Alert](concepts/alerte.md)

-   :material-tune-variant: **Per-mod rules**

    ---

    Each mod has its own rules (rain, materials, camouflage…) without ever touching other mods' rules.

    [:octicons-arrow-right-24: Rules](guides/regles.md)

-   :material-file-tree: **Behavior Trees and EQS**

    ---

    Describe behaviour in JSON, reload it with `/reload`. Environment queries find
    a hiding spot, cover or a way to flank the target.

    [:octicons-arrow-right-24: Behavior Trees](decision/behavior-trees.md)

-   :material-puzzle: **Extensible**

    ---

    Create your own senses, components, noises, scents and stealth modifiers.

    [:octicons-arrow-right-24: Create a sense](guides/sens-custom.md)

</div>

## In ten lines

```java
@Override
protected void initGoals() {
    Agent agent = Agent.builder(this)
            .rules(MyMod.RULES)                  // your perception rules (optional)
            .hearing(HearingProfile.KEEN)        // hearing + microphone
            .sight(SightConfig.nocturnal())      // sight
            .scent(ScentSense.hound())           // smell
            .damage().touch()                    // pain, touch
            .awareness(AwarenessProfile.DEFAULT) // suspicious → investigate → hunt
            .defaultGoals(2, 0.9, 1.4)           // listen, investigate, search, hunt
            .build();

    agent.onAwarenessChanged((a, from, to) -> { /* growl, animation… */ });
}
```

!!! tip "Want to see it before coding?"
    The [demo mod](demo.md) contains four creatures and a ready-to-play map of test arenas.
