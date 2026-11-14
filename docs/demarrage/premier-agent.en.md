# First agent

An **agent** is the "brain" attached to a mob. Your mob stays a `HostileEntity`, a `PathAwareEntity`…
you don't extend any special class.

## 1. Build the agent

The simplest place is `initGoals()`, called once when the mob is created:

```java
public class GuardianEntity extends HostileEntity {

    private Agent agent;

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .hearing(HearingProfile.DEFAULT)      // (1)!
                .sight(SightConfig.humanoid())        // (2)!
                .damage()                             // (3)!
                .awareness(AwarenessProfile.DEFAULT)  // (4)!
                .defaultGoals(2, 0.9, 1.4)            // (5)!
                .build();

        goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        goalSelector.add(6, new LookAroundGoal(this));
    }
}
```

1. Hears every game sound and players' voices.
2. Sees players in a 110° cone, hindered by darkness.
3. When hurt, it knows where the hit came from.
4. Thresholds: suspicious, investigate, hunt.
5. Adds "listen" (priority 3) and "investigate / hunt" (priority 2), walking at 0.9, running at 1.4.

That's it: the agent registers itself with the perception systems when the mob spawns,
and thinks every server tick, right before the vanilla AI.

## 2. React

```java
agent.onAwarenessChanged((a, from, to) -> {
    if (to == Awareness.HUNT) {
        playSound(SoundEvents.ENTITY_WARDEN_ROAR, 2f, 1f);
    }
});

agent.onStimulus((a, stimulus) -> {
    if (stimulus.wasSensedBy(SenseType.SIGHT) && "seen".equals(stimulus.getKind())) {
        // it just clearly saw you
    }
});
```

## 3. Read what it knows

```java
Agent agent = Agent.of(mob);                                   // any mob
Awareness level = agent.awarenessState();                      // IDLE, SUSPICIOUS, INVESTIGATE, HUNT
Vec3d guess = agent.blackboard().get(BlackboardKeys.FOCUS_POSITION);
UUID visible = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);
```

!!! warning "It never knows your real position"
    `FOCUS_POSITION` is an **estimate**: blurry if the sound was faint or muffled, exact if
    the agent clearly saw you. That's what makes the behaviour believable — and what you should use
    in your own behaviours.

## Next

- [Vanilla and other mods' mobs](templates.md): give a zombie an agent without touching its code.
- [Your mod's rules](../guides/regles.md): tune rain, materials, camouflage…
- [Debug](../guides/debug.md): `/sentience debug` to see what it thinks.
