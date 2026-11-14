# Niveaux d'alerte et comportements

L'`AwarenessComponent` transforme le meilleur stimulus en **niveau d'alerte**. Il monte immédiatement,
et redescend d'un cran à la fois après un délai.

| Niveau | Comportement par défaut | Délai avant de redescendre |
|---|---|---|
| `IDLE` | erre normalement | — |
| `SUSPICIOUS` | se fige, tourne la tête vers le bruit (`ListenGoal`) | 40 ticks |
| `INVESTIGATE` | va voir en marchant, puis fouille la zone (`InvestigateGoal`) | 80 ticks |
| `HUNT` | court vers la dernière position connue | 140 ticks |

## Seuils

```java
.awareness(new AwarenessProfile(0.5f, 6f, 18f))  // méfiant, va voir, chasse
```

| Profil | Méfiant | Va voir | Chasse |
|---|---|---|---|
| `AwarenessProfile.DEFAULT` | 0,5 | 6 | 18 |
| `AwarenessProfile.PARANOID` | 0,3 | 4 | 13 |
| `AwarenessProfile.CALM` | 2 | 10 | 25 |

Voir les [repères de score](memoire.md#reperes-de-score).

## Comportements prêts à l'emploi

`.defaultGoals(priorité, marche, course)` ajoute :

- **`InvestigateGoal`** (priorité donnée) : va à la position estimée, puis fouille la zone d'incertitude
  à des points aléatoires. Sans nouvelle information pendant `searchDuration` ticks, il abandonne ce stimulus.
  Chaque nouvelle perception le relance vers la position mise à jour.
- **`ListenGoal`** (priorité + 1) : en `SUSPICIOUS`, s'arrête et regarde autour de la direction supposée.

Tu peux aussi les instancier toi-même :

```java
goalSelector.add(2, new InvestigateGoal(agent, 0.9, 1.4, 240));
goalSelector.add(3, new ListenGoal(agent));
```

## Piloter l'alerte

```java
AwarenessComponent alerte = agent.awareness();
alerte.escalate(Awareness.HUNT);   // force un niveau minimal
alerte.giveUp(alerte.focus());     // oublie le stimulus courant
```

## Écrire tes propres comportements

Lis le niveau et le Blackboard dans tes goals :

```java
@Override
public boolean canStart() {
    return agent.awarenessState() == Awareness.HUNT
            && agent.blackboard().has(BlackboardKeys.VISIBLE_TARGET);
}
```
