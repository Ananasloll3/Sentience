# Ouïe et voix

```java
.hearing(HearingProfile.KEEN)
```

## Ce qui est entendu

**Tous les sons joués côté serveur** sont captés automatiquement et convertis en bruits physiques
(type + intensité en dB) : pas, chutes, nage, blocs cassés/posés/frappés, portes, coffres, pistons,
mécanismes, combat, projectiles, explosions, cris de créatures… La musique, la météo et l'interface sont ignorées.

| Repère | dB à la source |
|---|---|
| Chuchotement (micro) | ≈ 35 |
| Pas sur la laine, la mousse, la neige | 42 |
| Pas | 50 (sprint +6, **accroupi : aucun bruit de pas**) |
| Voix normale (micro) | 60 |
| Bloc cassé | 62 |
| Cri (micro) | ≈ 80 |
| Cloche | 95 |
| Explosion | 125 |

## Propagation

Pour chaque auditeur :

```
dB perçu = dB source − 20·log10(distance) − 0,35 × distance − occlusion
```

- **Occlusion** : parcours bloc par bloc entre la source et l'oreille. Laine et tapis −18 dB,
  pierre ≈ −9, porte fermée −7, porte ouverte −0,5, feuilles −1,5, verre −3,5…
- **Masquage** : le bruit n'est entendu que s'il dépasse le bruit de fond — grotte 12 dB, surface 18,
  pluie 42, orage 50, eau qui coule, lave. Un bruit de plus de 85 dB **assourdit** temporairement.
- **Localisation** : bonne en direction, mauvaise en distance ; d'autant plus floue que le son est faible
  ou étouffé.

Toutes ces valeurs sont réglables **par mod** : voir [Règles de ton mod](../guides/regles.md).

## Profils

| Profil | Seuil | Portée | Localisation |
|---|---|---|---|
| `HearingProfile.DEFAULT` | 15 dB | 64 | ×1 |
| `HearingProfile.KEEN` | 10 dB | 80 | ×1,4 |
| `HearingProfile.DULL` | 25 dB | 40 | ×0,7 |

```java
HearingProfile.KEEN.withThreshold(12f).withMaxRange(72).withLocalization(1.3)
```

## Filtrer ce qui l'intéresse

Par défaut, l'intérêt dépend du type de bruit (une voix intéresse plus qu'un mouton), et l'agent ignore
les spectateurs et ses congénères. Tu peux changer ça :

```java
.hearing(HearingProfile.DEFAULT, (agent, event) -> {
    if (event.type() == NoiseType.CREATURE) return 0;          // ignore les animaux
    if (event.type() == NoiseType.VOICE) return 1.5;           // adore les voix
    return event.type().defaultInterest();
})
```

## La voix (Simple Voice Chat)

Si Simple Voice Chat est installé, le niveau du micro de chaque joueur est mesuré côté serveur,
calibré automatiquement (chaque micro a son gain) et converti en bruit de type `VOICE`.
Le mode murmure compte pour 15 dB de moins.

Un autre mod de voix peut alimenter Sentience :

```java
VoiceInput.submit(player.getUuid(), niveauDbfs, chuchote);   // depuis n'importe quel thread
```

## Émettre tes propres bruits

```java
HearingSystem.report(world, pos, 90f, NoiseType.MECHANISM, sourceEntity);
```

`NoiseType` : `VOICE`, `FOOTSTEP`, `COMBAT`, `BLOCK`, `MECHANISM`, `PROJECTILE`, `EXPLOSION`,
`ITEM`, `CREATURE`, `GENERIC`.
