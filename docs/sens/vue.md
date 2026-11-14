# Vue

```java
.sight(SightConfig.humanoid())
```

## Fonctionnement

Toutes les quelques ticks, l'agent calcule la **visibilité** (0 à 1) de chaque cible à portée :

```
visibilité = angle × ligne de vue × lumière × discrétion × distance
```

| Facteur | Détail |
|---|---|
| Angle | 1 dans le champ net, 0,3 en vision périphérique, 0 derrière |
| Ligne de vue | rayons vers les yeux et le corps de la cible ; verre, vitres et barreaux laissent passer le regard |
| Lumière | plancher de 0,05 dans le noir total (sauf `darkVision`) ; une torche en main ou le feu rendent visible |
| Discrétion | accroupi ×0,5, invisibilité ×0,08, mouvement ×1,3, Glowing révèle, camouflages des règles |
| Distance | jusqu'à −70 % en limite de portée |

Chaque cible remplit une **jauge de détection**, comme dans un jeu d'infiltration :

- à partir de 35 % : un *coup d'œil* — position et identité floues (`kind = "glimpse"`) ;
- à 100 % : la cible est *vue* — position exacte, `BlackboardKeys.VISIBLE_TARGET` renseigné (`kind = "seen"`) ;
- à moins de 2,5 blocs : repérage instantané.

La jauge redescend quand la cible n'est plus visible.

## Configurations

| Préréglage | Portée | Champ net / périph. | Repérage | Vision nocturne |
|---|---|---|---|---|
| `SightConfig.humanoid()` | 32 | 110° / 170° | 20 ticks | 0,15 |
| `SightConfig.nocturnal()` | 40 | 100° / 160° | 12 ticks | 0,8 |

```java
SightConfig.humanoid()
        .withRange(40)
        .withFov(90f, 150f)
        .withDetectTicks(30)
        .withDarkVision(0.5f)
        .withSeesInvisible(true)
        .withTargets(e -> e instanceof PlayerEntity || e instanceof VillagerEntity)
```

Par défaut, les cibles sont les joueurs (hors spectateurs) : `SightConfig.PLAYERS`.

## Lire ce qu'il voit

```java
UUID cible = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);   // vue avec certitude

SightSense vue = agent.perception().sense(SightSense.class);
float jauge = vue.getDetection(player.getUuid());                    // 0..1
```
