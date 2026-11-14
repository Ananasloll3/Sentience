# Installation

Sentience cible **Minecraft 1.21.10**, **Fabric Loader 0.17.3+** et **Fabric API**. Java 21.

## Dans ton projet de mod

Sentience est distribué via [JitPack](https://jitpack.io), directement depuis les versions publiées sur GitHub.

=== "Gradle (Groovy)"

    ```groovy
    repositories {
        maven { url = 'https://jitpack.io' }
    }

    dependencies {
        modImplementation "com.github.Ananasloll3:Sentience:v0.1.0"
        // Optionnel : embarquer Sentience dans ton jar (jar-in-jar)
        // include "com.github.Ananasloll3:Sentience:v0.1.0"
    }
    ```

=== "Gradle (Kotlin)"

    ```kotlin
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        modImplementation("com.github.Ananasloll3:Sentience:v0.1.0")
    }
    ```

Puis déclare la dépendance dans ton `fabric.mod.json` :

```json
"depends": {
  "sentience": ">=0.1.0"
}
```

!!! info "Embarquer ou dépendre ?"
    Avec `include`, Sentience est livré dans ton jar : les joueurs n'ont rien à installer.
    Sans `include`, ils doivent ajouter le jar de Sentience à leur dossier `mods`.

## Depuis les sources

```bash
git clone https://github.com/Ananasloll3/Sentience.git
cd Sentience
./gradlew publishToMavenLocal
```

Puis, dans ton projet : `repositories { mavenLocal() }` et
`modImplementation "com.ananasloll3:sentience:0.1.0"`.

## Simple Voice Chat (optionnel)

Si [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) est installé, Sentience écoute
automatiquement les micros : la voix des joueurs devient un bruit comme un autre. Rien à configurer.
Sans lui, tout le reste fonctionne normalement.
