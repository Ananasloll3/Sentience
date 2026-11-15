---
hide:
  - navigation
---

# Sentience

**Des IA qui perçoivent vraiment le monde, pour tes mods Minecraft.**

Sentience est une librairie Fabric (Minecraft 1.21.10) inspirée des outils d'IA d'Unreal Engine.
Elle donne à n'importe quel mob — le tien, un mob vanilla ou celui d'un autre mod — un **agent** :
des sens réalistes, une mémoire, un Blackboard et des niveaux d'alerte, avec des comportements prêts à l'emploi.

![Les mobs de démo](assets/mobs.png)

<div class="grid cards" markdown>

-   :material-ear-hearing: **Ouïe réaliste**

    ---

    Tous les sons du jeu sont captés et propagés physiquement : atténuation, murs, matériaux,
    bruit de fond qui couvre tes pas. La **voix des joueurs** (Simple Voice Chat) aussi.

    [:octicons-arrow-right-24: Ouïe et voix](sens/ouie.md)

-   :material-eye: **Vue crédible**

    ---

    Cône de vision, vision périphérique, obscurité, discrétion, camouflage, et une jauge de détection
    progressive comme dans un jeu d'infiltration.

    [:octicons-arrow-right-24: Vue](sens/vue.md)

-   :material-dog-side: **Odorat et pistes**

    ---

    Les joueurs laissent une piste que l'eau coupe et que la pluie efface. Le sang attire.
    Les traqueurs la remontent pas à pas.

    [:octicons-arrow-right-24: Odorat](sens/odorat.md)

-   :material-brain: **Mémoire et alerte**

    ---

    L'agent ne connaît jamais ta vraie position : il fusionne ses perceptions, devine, va voir,
    fouille, chasse, puis abandonne.

    [:octicons-arrow-right-24: Alerte](concepts/alerte.md)

-   :material-tune-variant: **Règles par mod**

    ---

    Chaque mod a ses propres règles (pluie, matériaux, camouflage…) sans jamais toucher à celles des autres.

    [:octicons-arrow-right-24: Règles](guides/regles.md)

-   :material-file-tree: **Behavior Trees et EQS**

    ---

    Décris le comportement en JSON, recharge-le avec `/reload`. Les requêtes d'environnement trouvent
    une cachette, un abri ou un point pour contourner la cible.

    [:octicons-arrow-right-24: Behavior Trees](decision/behavior-trees.md)

-   :material-puzzle: **Extensible**

    ---

    Crée tes propres sens, composants, bruits, odeurs et modificateurs de discrétion.

    [:octicons-arrow-right-24: Créer un sens](guides/sens-custom.md)

</div>

## En dix lignes

```java
@Override
protected void initGoals() {
    Agent agent = Agent.builder(this)
            .rules(MonMod.RULES)                 // tes règles de perception (optionnel)
            .hearing(HearingProfile.KEEN)        // ouïe + micro
            .sight(SightConfig.nocturnal())      // vue
            .scent(ScentSense.hound())           // odorat
            .damage().touch()                    // douleur, toucher
            .awareness(AwarenessProfile.DEFAULT) // méfiant → va voir → chasse
            .defaultGoals(2, 0.9, 1.4)           // écouter, aller voir, fouiller, chasser
            .build();

    agent.onAwarenessChanged((a, from, to) -> { /* grognement, animation… */ });
}
```

!!! tip "Envie de voir avant de coder ?"
    Le [mod de démo](demo.md) contient quatre créatures et une map d'arènes de test prête à jouer.
