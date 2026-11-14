# Règles de ton mod

Les règles « physiques » de la perception (pluie, matériaux, obscurité, camouflage…) ont des valeurs par
défaut réalistes, mais **chaque mod peut avoir les siennes**.

!!! success "Aucun conflit entre mods"
    Les règles s'appliquent **côté perceveur** : un même bruit est entendu par les mobs de ton mod
    avec tes règles, et par ceux d'un autre mod avec les leurs. Personne ne modifie les règles globales.

## Créer ses règles

```java
public static final SentienceRules RULES = SentienceRules.create("monmod")
        .hearing(h -> h
                .ignoreRain()
                .muffled(25f)
                .addAmbientSource((world, pos) -> isBlizzard(world, pos) ? 55f : Float.NaN)
                .addNoiseModifier((event, db) -> isMyMachine(event.soundId()) ? Float.NaN : db))
        .sight(s -> s
                .sneak(0.3f)
                .addStealthModifier((observer, target) -> wearsCamo(target) ? 0.2f : 1f))
        .scent(s -> s.ignoreRain().lifetime(6000).blood(3f))
        .build();

Agent.builder(this).rules(RULES)...build();
```

Sans `.rules(...)`, un agent utilise `SentienceRules.DEFAULT`, qui ne change jamais.

### Variantes

Un jeu de règles peut être dérivé pour un mob particulier :

```java
public static final SentienceRules BOSS_RULES = RULES.derive(Identifier.of("monmod", "boss"))
        .sight(s -> s.invisibility(1f))      // voit les invisibles
        .build();
```

## Ouïe

| Réglage | Défaut | Méthode |
|---|---|---|
| Absorption de l'air | 0,35 dB/bloc | `airAbsorption(db)` |
| Occlusion maximale | 60 dB | `maxOcclusion(db)` |
| Seuil d'assourdissement | 85 dB | `deafening(db)` |
| Bruit de fond grotte / surface / pluie / orage | 12 / 18 / 42 / 50 dB | `ambient(...)`, `ignoreRain()` |
| Eau qui coule / lave | 38 / 30 dB | `fluidAmbient(eau, lave)` |
| Laine, tapis | 18 dB | `muffled(db)` |
| Porte fermée / ouverte | 7 / 0,5 dB | `doors(fermée, ouverte)` |
| Feuilles | 1,5 dB | `leaves(db)` |
| Bloc plein | 9 + dureté × 0,2 (incassable 25) | `solids(base, facteur, incassable)` |
| Bloc fin (verre, dalle…) | 3,5 dB | `thinSolids(db)` |
| Eau / lave traversée | 4 / 6 dB | `fluids(eau, lave)` |

**Points d'extension :**

- `addNoiseModifier((event, db) -> nouveauDb)` — change l'intensité d'un bruit, ou `Float.NaN` pour l'ignorer.
  `event.soundId()` donne le son Minecraft d'origine (ex. `minecraft:block.wool.step`).
- `addBlockAcoustics((world, pos, state) -> dB ou null)` — atténuation d'un bloc, prioritaire sur les règles intégrées.
- `addAmbientSource((world, pos) -> dB ou Float.NaN)` — bruit de fond supplémentaire.

## Vue

| Réglage | Défaut | Méthode |
|---|---|---|
| Visibilité dans le noir total | 0,05 | `darknessFloor(v)` |
| Vision périphérique | ×0,3 | `peripheral(f)` |
| Accroupi | ×0,5 | `sneak(f)` |
| Invisibilité | ×0,08 | `invisibility(f)` |
| Mouvement | ×1,3 | `movement(f)` |
| Torche en main / feu / Glowing révèlent | oui | `lightInHandReveals`, `fireReveals`, `glowingReveals` |
| Repérage instantané | < 2,5 blocs | `instantDetection(distance)` |
| Blocs transparents | verre, vitres, barreaux | `seeThrough(predicate)` |

**Point d'extension :** `addStealthModifier((observer, target) -> facteur)` — camouflage, capes, peintures…

## Odorat

| Réglage | Défaut | Méthode |
|---|---|---|
| Durée d'une trace | 2400 ticks (max 12000) | `lifetime(ticks)` |
| Effet de la pluie | durée ×0,5 | `rainLifetime(f)`, `ignoreRain()` |
| L'eau coupe la piste | oui | `waterBreaksTrail(bool)` |
| Trace accroupi | ×1 | `sneakStrength(f)` |
| Attrait du sang | ×1 | `blood(f)` |
| Murs traversables | 1 bloc plein | `maxWallBlocks(n)` (-1 = illimité) |

**Point d'extension :** `addPointModifier((agent, point) -> facteur)` — potion anti-odeur, proie préférée…
