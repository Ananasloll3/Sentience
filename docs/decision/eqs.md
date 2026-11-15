# Requêtes d'environnement (EQS)

Une **requête d'environnement** (l'*Environment Query System* d'Unreal) répond à des questions comme
« où me cacher ? », « où m'abriter ? », « par où la contourner ? ». Elle :

1. **génère** des points candidats autour d'une cible (posés au sol, là où un mob tient debout) ;
2. les **filtre** et les **note** avec des tests ;
3. renvoie le meilleur, en vérifiant au besoin qu'on peut l'atteindre.

## Requêtes intégrées

La **menace** est la cible vue avec certitude, sinon la position du focus.

| Identifiant | Trouve |
|---|---|
| `sentience:hide` | un endroit proche où la menace ne te voit pas, plutôt loin d'elle |
| `sentience:cover` | un abri tout proche, avec un bloc solide entre toi et la menace |
| `sentience:flank` | un point sur le côté de la menace, pour la contourner |
| `sentience:flee` | un point loin de la menace, hors de sa vue si possible |

Dans un Behavior Tree :

```json
{ "type": "run_query", "query": "sentience:hide", "result": "monmod:cachette" },
{ "type": "move_to", "target": "monmod:cachette", "speed": 1.4 }
```

`run_query` échoue s'il n'y a aucun point valable : la séquence s'arrête et l'arbre passe à autre chose.

## Écrire une requête en JSON

Fichier `data/monmod/sentience/env_query/coin_sombre.json` → identifiant `monmod:coin_sombre`.

```json
{
  "generator": { "type": "ring", "center": "querier", "radius": [4, 14], "rings": 3, "points": 10 },
  "tests": [
    { "type": "light", "max": 9, "weight": 2, "prefer": "low" },
    { "type": "visible_from", "target": "threat", "max": 0 },
    { "type": "distance", "target": "querier", "weight": 0.5, "prefer": "high" }
  ],
  "pathable": true
}
```

### Générateurs

| Type | Paramètres |
|---|---|
| `ring` | `center`, `radius` (nombre ou `[min, max]`), `rings` (3), `points` par anneau (12) |
| `grid` | `center`, `radius` (10), `spacing` (2) |

### Cibles

`querier` (l'agent), `threat` (la menace), `focus` (position du focus) ou l'identifiant d'une clé
du Blackboard.

### Tests

| Type | Mesure |
|---|---|
| `distance` | distance entre le point et `target` |
| `visible_from` | 1 si `target` voit le point (à hauteur de tête, à travers le verre), 0 sinon |
| `cover` | 1 si un bloc solide protège le point du côté de `target` |
| `dot` | cosinus de l'angle, vu de `from`, entre le point et `to` (1 = même direction, 0 = de côté) |
| `light` | lumière au point (0-15) |
| `random` | valeur aléatoire, pour départager |

Chaque test peut :

- **filtrer** : `min` et/ou `max` éliminent les points hors de l'intervalle ;
- **noter** : `weight` (0 = filtre seul) et `prefer` (`high` ou `low`). Les valeurs sont normalisées
  entre 0 et 1 sur les points restants, puis additionnées.

`"pathable": true` garde seulement un point atteignable : le chemin n'est calculé que pour les
meilleurs points, jusqu'à en trouver un.

## En Java

```java
public static final EnvQuery CACHETTE = EnvQueries.register(Identifier.of("monmod", "cachette"),
        EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 4, 16, 3, 12))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))   // filtre : caché
                .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(1))  // loin de la menace
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1.5)) // pas trop loin de moi
                .pathable()
                .build());

Vec3d spot = CACHETTE.run(agent).best();   // null si rien ne convient
```

Une mesure personnalisée est une fonction qui reçoit l'agent et le monde, et renvoie la mesure de chaque point :

```java
QueryTest.of((agent, world) -> point -> world.getBlockState(BlockPos.ofFloored(point).down()).isOf(Blocks.GRASS_BLOCK) ? 1 : 0)
        .preferHigh(1);
```

## Debug

Avec `/sentience debug`, chaque requête affiche ses points : **gris** = éliminés, **verts** = retenus,
et des étincelles sur le meilleur.

## Performances

Une requête coûte quelques dizaines de tests de bloc ou de ligne de vue, et quelques calculs de chemin
avec `pathable`. Lance-la à l'entrée d'une branche (`run_query`) ou avec un service espacé (20 ticks ou plus),
pas à chaque tick.
