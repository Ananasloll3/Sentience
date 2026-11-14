# Mémoire et stimuli

Chaque sens produit des **perceptions brutes** (`StimulusInput`). La mémoire (`StimulusMemory`) les fusionne
en **stimuli** : des souvenirs du type « il y a quelque chose, à peu près là ».

## Ce que contient un stimulus

| Accesseur | Sens |
|---|---|
| `getPosition()` | position **estimée** |
| `getErrorRadius()` | incertitude, en blocs |
| `getScore(now)` | priorité actuelle (vieillit) |
| `getLastSense()`, `getSenses()`, `wasSensedBy(sens)` | quels sens l'ont confirmé |
| `getKind()` | détail : `footstep`, `voice`, `seen`, `glimpse`, `scent`, `blood`, `melee`… |
| `getSourceId()` | entité identifiée, si connue |
| `getPeakIntensity()` | mesure physique (dB pour l'ouïe…) |
| `getCount()`, `getFirstTick()`, `getLastTick()` | historique |

## Fusion

Deux perceptions proches, ou de la même source, sont fusionnées :

- **Même endroit** : les positions sont moyennées en pondérant par la précision (1/σ²).
  L'incertitude diminue — comme une triangulation.
- **Même source qui s'est déplacée** : la nouvelle position **remplace** l'ancienne, pour ne jamais
  viser un point périmé.
- Chaque répétition ajoute un petit bonus de priorité : des pas répétés finissent par attirer l'attention.

## Vieillissement

Le score décroît avec une demi-vie de 160 ticks. Un stimulus est oublié quand son score devient trop faible
ou qu'il dépasse la durée de mémoire (600 ticks par défaut, `.memory(capacité, ticks)`).

## Utilisation directe

```java
StimulusMemory memoire = agent.perception().memory();
Stimulus meilleur = memoire.best(world.getTime());
Stimulus joueur = memoire.findBySource(player.getUuid());
memoire.forgetNear(pos, 5.0);

// Injecter une information (ex. : un allié te prévient, un piège se déclenche)
agent.perception().report(new StimulusInput(SenseType.REPORT, pos, 3.0, 20f, 0f, "piege", null));
```

## Repères de score

| Situation | Score typique |
|---|---|
| Pas à 8 blocs | ≈ 6 |
| Voix normale à 10 blocs | ≈ 18 |
| Coup d'œil (jauge de vue à 35 %) | ≈ 4 |
| Cible clairement vue | 30 |
| Toucher | 25 |
| Coup reçu | 40 |
