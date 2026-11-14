# Premier agent

Un **agent** est le « cerveau » attaché à un mob. Ton mob reste un `HostileEntity`, un `PathAwareEntity`…
tu n'hérites d'aucune classe spéciale.

## 1. Construire l'agent

Le plus simple est de le faire dans `initGoals()`, appelé une fois à la création du mob :

```java
public class GardienEntity extends HostileEntity {

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

1. Entend tous les sons du jeu et la voix des joueurs.
2. Voit les joueurs dans un cône de 110°, gêné par l'obscurité.
3. Quand il est blessé, il sait d'où vient le coup.
4. Seuils : méfiant, va voir, chasse.
5. Ajoute « écouter » (priorité 3) et « aller voir / chasser » (priorité 2), marche à 0,9, course à 1,4.

C'est tout : l'agent est enregistré automatiquement auprès des systèmes de perception quand le mob apparaît,
et réfléchit à chaque tick serveur juste avant l'IA vanilla.

## 2. Réagir

```java
agent.onAwarenessChanged((a, from, to) -> {
    if (to == Awareness.HUNT) {
        playSound(SoundEvents.ENTITY_WARDEN_ROAR, 2f, 1f);
    }
});

agent.onStimulus((a, stimulus) -> {
    if (stimulus.wasSensedBy(SenseType.SIGHT) && "seen".equals(stimulus.getKind())) {
        // il vient de te voir clairement
    }
});
```

## 3. Lire ce qu'il sait

```java
Agent agent = Agent.of(mob);                                   // n'importe quel mob
Awareness niveau = agent.awarenessState();                     // IDLE, SUSPICIOUS, INVESTIGATE, HUNT
Vec3d supposition = agent.blackboard().get(BlackboardKeys.FOCUS_POSITION);
UUID visible = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);
```

!!! warning "Il ne connaît jamais ta vraie position"
    `FOCUS_POSITION` est une **estimation** : floue si le son était faible ou étouffé, exacte si
    l'agent t'a vu clairement. C'est ce qui rend le comportement crédible — et ce que tu dois utiliser
    dans tes propres comportements.

## Ensuite

- [Mobs vanilla et d'autres mods](templates.md) : donner un agent à un zombie sans toucher à son code.
- [Règles de ton mod](../guides/regles.md) : régler la pluie, les matériaux, le camouflage…
- [Debug](../guides/debug.md) : `/sentience debug` pour voir ce qu'il pense.
