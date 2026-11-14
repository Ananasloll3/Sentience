# Communication

Un agent peut partager son focus avec ses alliés. L'information arrive **dégradée** : incertitude +3 blocs,
score ×0,8 (`SenseType.REPORT`, `kind = "report"`).

```java
// Prévenir les mobs du même type dans un rayon de 48 blocs
int prevenus = AgentComms.shareFocus(agent, 48, AgentComms.sameType(agent));

// Prévenir tous les agents proches
AgentComms.shareFocus(agent, 40, other -> true);
```

Un usage typique : dans `onAwarenessChanged`, quand l'agent passe en chasse.

## Alarme sonore

Pour une alerte que **toutes** les créatures dotées d'ouïe peuvent entendre (à travers les murs, avec
la physique du son), émets simplement un bruit fort :

```java
HearingSystem.report(world, mob.getEyePos(), 105f, NoiseType.MECHANISM, mob);
```

La Sentinelle du [mod de démo](../demo.md) fait les deux : radio pour ses alliées, cloche pour tout le monde.
