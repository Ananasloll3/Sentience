# Installation

Sentience targets **Minecraft 1.21.10**, **Fabric Loader 0.17.3+** and **Fabric API**. Java 21.

## In your mod project

Once Sentience is published on CurseForge or Modrinth, those platforms serve it as a Maven dependency.

=== "CurseForge (Cursemaven)"

    ```groovy
    repositories {
        maven { url = 'https://cursemaven.com' }
    }

    dependencies {
        // curse.maven:<slug>-<project ID>:<file ID>
        modImplementation "curse.maven:sentience-PROJECT:FILE"
    }
    ```

    The project ID is in the "About Project" box of the CurseForge page; the file ID is at the end
    of the file page's address.

=== "Modrinth"

    ```groovy
    repositories {
        maven { url = 'https://api.modrinth.com/maven' }
    }

    dependencies {
        modImplementation "maven.modrinth:sentience:0.1.0"
    }
    ```

Then declare the dependency in your `fabric.mod.json`:

```json
"depends": {
  "sentience": ">=0.1.0"
}
```

!!! info "Bundle or depend?"
    Also add `include` with the same coordinates to ship Sentience inside your jar (jar-in-jar):
    players then have nothing to install. Otherwise, they must add the Sentience jar to their `mods` folder.

## From source

```bash
git clone https://github.com/Ananasloll3/Sentience.git
cd Sentience
./gradlew publishToMavenLocal
```

Then, in your project: `repositories { mavenLocal() }` and
`modImplementation "com.ananasloll3:sentience:0.1.0"`.

## Simple Voice Chat (optional)

If [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) is installed, Sentience automatically
listens to microphones: players' voices become noises like any other. Nothing to configure.
Without it, everything else works normally.
