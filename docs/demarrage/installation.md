# Installation

Sentience cible **Minecraft 1.21.10**, **Fabric Loader 0.17.3+** et **Fabric API**. Java 21.

## Dans ton projet de mod

Une fois Sentience publié sur CurseForge ou Modrinth, ces plateformes le servent comme une dépendance Maven.

=== "CurseForge (Cursemaven)"

    ```groovy
    repositories {
        maven { url = 'https://cursemaven.com' }
    }

    dependencies {
        // curse.maven:<slug>-<ID du projet>:<ID du fichier>
        modImplementation "curse.maven:sentience-PROJET:FICHIER"
    }
    ```

    L'ID du projet est dans l'encadré « About Project » de la page CurseForge ; l'ID du fichier
    est à la fin de l'adresse de la page du fichier.

=== "Modrinth"

    ```groovy
    repositories {
        maven { url = 'https://api.modrinth.com/maven' }
    }

    dependencies {
        modImplementation "maven.modrinth:sentience:0.2.0"
    }
    ```

Puis déclare la dépendance dans ton `fabric.mod.json` :

```json
"depends": {
  "sentience": ">=0.2.0"
}
```

!!! info "Embarquer ou dépendre ?"
    Ajoute aussi `include` avec les mêmes coordonnées pour livrer Sentience dans ton jar (jar-in-jar) :
    les joueurs n'ont alors rien à installer. Sinon, ils doivent ajouter le jar de Sentience à leur dossier `mods`.

## Depuis les sources

```bash
git clone https://github.com/Ananasloll3/Sentience.git
cd Sentience
./gradlew publishToMavenLocal
```

Puis, dans ton projet : `repositories { mavenLocal() }` et
`modImplementation "com.ananasloll3:sentience:0.2.0"`.

## Simple Voice Chat (optionnel)

Si [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) est installé, Sentience écoute
automatiquement les micros : la voix des joueurs devient un bruit comme un autre. Rien à configurer.
Sans lui, tout le reste fonctionne normalement.
