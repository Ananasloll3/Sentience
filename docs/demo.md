# Mod de démo

Le dépôt contient un mod de démo séparé (`src/testmod`), jamais publié avec la librairie.
Il montre comment utiliser chaque fonctionnalité, et fournit une map d'arènes de test.

```bash
./gradlew runTestmodClient
```

![Les mobs de démo](assets/mobs.png)

## Les créatures

| Créature | Ce qu'elle teste |
|---|---|
| **Rôdeur** | aveugle : ouïe seule (+ micro), toucher, douleur |
| **Sentinelle** | sourde et immobile : vue seule. Tire sur ce qu'elle voit vraiment, sonne une alarme entendue par toutes les créatures, prévient ses alliées par radio |
| **Flaireur** | odorat : remonte ta piste pas à pas. Ses règles : la pluie n'efface pas la piste pour lui |
| **Abomination** | tout à la fois + un composant personnalisé (craint la lumière) + un goal qui lit le Blackboard ; prévient les autres créatures quand elle chasse |
| **Chasseur** | tout son comportement est un [Behavior Tree JSON](decision/behavior-trees.md) : patrouille dans les coins sombres, contourne sa proie, se cache quand il est blessé ([requêtes d'environnement](decision/eqs.md)), prévient ses semblables |
| **Zombies** (vanilla) | ouïe et vue des joueurs et villageois, plus un second modèle qui leur fait sentir le sang (deux `AgentTemplates` combinés) |

Le mod de démo a **ses propres règles** : une citrouille sculptée sur la tête sert de camouflage pour ses
créatures. Les zombies, eux, gardent les règles par défaut — la citrouille ne les trompe pas.

## La map « Sentience - Arènes de test »

Disponible dans les [versions publiées](https://github.com/Ananasloll3/Sentience/releases). Dézippe-la dans
`run/saves`. Tu apparais de nuit, au hub ; les arènes sont le long du chemin.

| Arène | Contenu |
|---|---|
| **Rôdeur** | 4 salles : gravier, laine (pas étouffés), cascade (masque les bruits), mécanismes et cachette en laine |
| **Sentinelle** | cour moitié éclairée, moitié noire ; couvertures, vitre, barreaux ; coffre (invisibilité, torches, citrouille) ; un Rôdeur dans le chenil qui accourt à l'alarme |
| **Flaireur** | chemin en serpentin, enclos à ouvrir, rivière qui coupe la piste, cabane au bout |
| **Abomination** | maison sombre, une lampe et un levier par pièce, placard, tapis |

`/sentiencetest arene <rodeur|sentinelle|flaireur|abomination|toutes>` reconstruit une arène n'importe où.

!!! tip "Modifier le Chasseur à chaud"
    Son arbre est dans `src/testmod/resources/data/sentience_testmod/sentience/behavior_tree/hunter.json`.
    Modifie-le, puis `/reload` en jeu.
