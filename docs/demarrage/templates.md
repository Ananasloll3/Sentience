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

!!! warning "Un seul modèle par type"
    Si deux mods enregistrent un modèle pour le même type, le dernier enregistré gagne.
    Pour tes propres mobs, construis plutôt l'agent dans le mob lui-même.

## Entités sans agent

Une entité qui n'est pas un mob (ou qui ne veut qu'entendre) peut implémenter `HearingListener`
et s'enregistrer à la main :

```java
HearingSystem.register(world, monEcouteur);   // au chargement
HearingSystem.unregister(monEcouteur);        // au déchargement
```
