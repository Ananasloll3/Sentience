# Mobs vanilla et d'autres mods

`AgentTemplates` donne un agent à tous les mobs d'un type existant, sans toucher à leur code.
Le modèle est appliqué à chaque apparition ou chargement du mob.

```java
@Override
public void onInitialize() {
    AgentTemplates.register(EntityType.ZOMBIE, agent -> agent
            .rules(MonMod.RULES)
            .hearing(HearingProfile.DEFAULT)
            .sight(SightConfig.humanoid()
                    .withTargets(e -> SightConfig.PLAYERS.test(e) || e instanceof VillagerEntity))
            .damage()
            .defaultGoals(3, 1.0, 1.2));
}
```

Les comportements de Sentience s'ajoutent à ceux du mob : un zombie garde son attaque vanilla
(priorité 2) et utilise « aller voir » (priorité 3) quand il n'a pas de cible.

## Plusieurs modèles pour un même type

Plusieurs mods peuvent viser le même type : leurs modèles sont **combinés** sur le même agent,
par priorité croissante (puis dans l'ordre d'enregistrement).

```java
// Un autre mod ajoute l'odorat aux zombies, sans retirer l'ouïe et la vue
AgentTemplates.register(EntityType.ZOMBIE, 10, agent -> agent
        .scent(ScentSense.hound().only(p -> p.kind().equals(ScentTrails.BLOOD))));
```

- les sens et composants **s'additionnent** ;
- un sens ou un composant **de même classe** est remplacé par celui du modèle le plus prioritaire ;
- les règles, l'alerte, les comportements par défaut et le Behavior Tree aussi.

Pour tes propres mobs, construis plutôt l'agent directement dans le mob.

## Entités sans agent

Une entité qui n'est pas un mob (ou qui ne veut qu'entendre) peut implémenter `HearingListener`
et s'enregistrer à la main :

```java
HearingSystem.register(world, monEcouteur);   // au chargement
HearingSystem.unregister(monEcouteur);        // au déchargement
```
