# Douleur et toucher

## Douleur

```java
.damage()
```

Quand l'agent est blessé, il apprend d'où vient le coup :

- **corps à corps** : position quasi exacte de l'attaquant (score 40, `kind = "melee"`) ;
- **tir lointain** : position de l'attaquant avec une incertitude qui grandit avec la distance (`"ranged"`) ;
- **projectile sans tireur** : l'agent remonte la trajectoire.

Avec les seuils par défaut, un coup déclenche directement la chasse.

## Toucher

```java
.touch()                                   // joueurs
.touch(e -> e instanceof PlayerEntity || e instanceof VillagerEntity)
```

Une cible qui frôle l'agent se trahit (position exacte, score 25), même dans le noir complet
et même si l'agent est aveugle et sourd.
