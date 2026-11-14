# Odorat

```java
.scent(ScentSense.hound())
```

## Les pistes

Les joueurs laissent un **point d'odeur** toutes les 10 ticks (0,5 s). Chaque point note s'il a été laissé
**dans l'eau** ou **accroupi**. Une créature blessée laisse du **sang** — une trace 2,5× plus forte — et
saigne en marchant sous 40 % de vie.

Par défaut :

- une trace dure **2 minutes** ;
- la **pluie** l'efface deux fois plus vite ;
- l'**eau** ne garde pas l'odeur : traverser une rivière coupe la piste ;
- l'odeur traverse **au plus un bloc plein** entre la trace et le nez.

Tout est réglable par mod via les [règles](../guides/regles.md#odorat).

## Le flair

Toutes les 10 ticks, l'agent cherche la trace la plus forte à portée (la plus fraîche à force égale,
c'est-à-dire celle *devant*, dans le sens de la marche) et la signale. En la suivant, il remonte la piste
pas à pas. Arrivé au bout, la dernière trace ne le relance plus : il fouille autour, puis abandonne.

```java
ScentSense.hound()                         // 8 blocs, toutes les pistes
ScentSense.of(12, 15f)                     // 12 blocs, plus sensible
ScentSense.hound().playersOnly()           // seulement les joueurs
ScentSense.hound().only(p -> p.kind().equals(ScentTrails.BLOOD))   // seulement le sang
```

## Faire laisser une piste à d'autres entités

```java
ScentTrails.addEmitter(EntityType.VILLAGER);
```

L'ajout est additif : chaque mod ajoute ses types sans retirer ceux des autres.

## Déposer des odeurs

```java
ScentTrails.deposit(world, pos, 3f, appatUuid);            // odeur custom (appât, cadavre…)
ScentTrails.deposit(world, entity, 1f, ScentTrails.TRAIL); // trace d'une entité à sa position
```

## Voir les pistes

`/sentience debug` affiche les pistes récentes autour de toi :
**vert** = trace, **rouge** = sang, **gris** = dans l'eau.
`/sentiencetest piste <de> <a>` (mod de démo) fabrique une piste instantanément.
