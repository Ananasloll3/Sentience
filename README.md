<div align="center">

# 🧠 Sentience

**Des IA qui perçoivent vraiment le monde, pour tes mods Minecraft.**

Librairie Fabric inspirée des outils d'IA d'Unreal Engine : perception multi-sens réaliste,
mémoire, Blackboard, niveaux d'alerte et comportements prêts à l'emploi.

[![Build](https://github.com/Ananasloll3/Sentience/actions/workflows/build.yml/badge.svg)](https://github.com/Ananasloll3/Sentience/actions/workflows/build.yml)
[![Docs](https://img.shields.io/badge/docs-en%20ligne-7e57c2)](https://ananasloll3.github.io/Sentience/)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-62b47a)
![Fabric](https://img.shields.io/badge/loader-Fabric-dbd0b4)
[![Licence MIT](https://img.shields.io/badge/licence-MIT-blue)](LICENSE)

[**📖 Documentation**](https://ananasloll3.github.io/Sentience/) ([English](https://ananasloll3.github.io/Sentience/en/) · [中文](https://ananasloll3.github.io/Sentience/zh/)) ·
[Démarrage rapide](https://ananasloll3.github.io/Sentience/demarrage/premier-agent/) ·
[Mod de démo](https://ananasloll3.github.io/Sentience/demo/) ·
[Versions](https://github.com/Ananasloll3/Sentience/releases)

![Les créatures du mod de démo](docs/assets/mobs.png)

</div>

## Pourquoi Sentience ?

Les mobs vanilla te repèrent à travers les murs et foncent droit sur toi. Avec Sentience, une créature
**ne connaît jamais ta vraie position** : elle entend un bruit, devine d'où il vient, va voir, fouille,
te piste… et peut se tromper. Tu peux te cacher, te taire, marcher accroupi, profiter de la pluie,
couper ta piste en traversant une rivière.

| | |
|---|---|
| 👂 **Ouïe physique** | Tous les sons du jeu, propagés avec atténuation, murs, matériaux et bruit de fond. La **voix** des joueurs via Simple Voice Chat. |
| 👁 **Vue crédible** | Cône de vision, périphérie floue, obscurité, discrétion, camouflage, jauge de détection progressive. |
| 👃 **Odorat** | Pistes que l'eau coupe et que la pluie efface ; le sang attire ; l'odeur ne traverse pas les murs. |
| ⚔ **Douleur & toucher** | Être frappé ou frôlé révèle la position. |
| 📣 **Communication** | Les agents se transmettent ce qu'ils savent. |
| 🧠 **Mémoire & alerte** | Fusion des perceptions, incertitude, oubli ; méfiant → va voir → chasse. |
| 🎛 **Règles par mod** | Chaque mod règle la physique de la perception pour ses propres mobs, sans conflit. |
| 🧩 **Extensible** | Sens, composants, bruits, odeurs et camouflages personnalisés. |
| ⚡ **Performant** | Niveau de détail automatique, calculs coûteux seulement quand c'est utile. |

## En dix lignes

```java
@Override
protected void initGoals() {
    Agent agent = Agent.builder(this)
            .hearing(HearingProfile.KEEN)        // ouïe + micro
            .sight(SightConfig.nocturnal())      // vue
            .scent(ScentSense.hound())           // odorat
            .damage().touch()                    // douleur, toucher
            .awareness(AwarenessProfile.DEFAULT) // méfiant → va voir → chasse
            .defaultGoals(2, 0.9, 1.4)           // écouter, aller voir, fouiller, chasser
            .build();
}
```

Un mob vanilla ou d'un autre mod, sans toucher à son code :

```java
AgentTemplates.register(EntityType.ZOMBIE, agent -> agent
        .hearing(HearingProfile.DEFAULT)
        .sight(SightConfig.humanoid())
        .defaultGoals(3, 1.0, 1.2));
```

## Installation

Via [Cursemaven](https://cursemaven.com) ou le [Maven de Modrinth](https://support.modrinth.com/en/articles/8801191-modrinth-maven)
une fois Sentience publié sur ces plateformes, ou depuis les sources :

```bash
git clone https://github.com/Ananasloll3/Sentience.git && cd Sentience
./gradlew publishToMavenLocal
```

```groovy
repositories { mavenLocal() }
dependencies { modImplementation "com.ananasloll3:sentience:0.1.0" }
```

Détails : [installation](https://ananasloll3.github.io/Sentience/demarrage/installation/).

Minecraft **1.21.10**, Fabric Loader **0.17.3+**, Fabric API, Java 21.
[Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) est optionnel.

➡️ Tout le reste est dans la [**documentation**](https://ananasloll3.github.io/Sentience/).

## Mod de démo

```bash
./gradlew runTestmodClient
```

Quatre créatures, chacune centrée sur un sens — le **Rôdeur** (aveugle), la **Sentinelle** (sourde),
le **Flaireur** (odorat) et l'**Abomination** (tout à la fois) — plus des zombies vanilla améliorés,
et une map d'arènes de test disponible dans les [versions](https://github.com/Ananasloll3/Sentience/releases).
`/sentience debug` montre ce que pense chaque créature.

## Développement

| Commande | Effet |
|---|---|
| `./gradlew build` | compile la librairie et le mod de démo |
| `./gradlew runTestmodClient` | lance le jeu avec le mod de démo |
| `./gradlew publishToMavenLocal` | publie dans ton Maven local |
| `pip install mkdocs-material && mkdocs serve` | documentation en local |

```
src/main      la librairie (api/ public et stable, impl/ interne)
src/testmod   le mod de démo, jamais publié
docs/         la documentation (MkDocs Material)
```

## Feuille de route

- [x] Agent, Blackboard, niveau de détail, debug
- [x] Ouïe, voix, vue, odorat, douleur, toucher, communication
- [x] Règles par mod
- [ ] Behavior Trees en JSON
- [ ] Navigation v2 (A* asynchrone, portes, échelles, sauts)
- [ ] EQS, escouades, Director

## Licence

[MIT](LICENSE) — utilise Sentience dans tes mods, libres ou non.
