# 安装

Sentience 适用于 **Minecraft 1.21.10**、**Fabric Loader 0.17.3+** 和 **Fabric API**，需要 Java 21。

## 在你的模组项目中使用

Sentience 在 CurseForge 或 Modrinth 上发布后，这些平台会将其作为 Maven 依赖提供。

=== "CurseForge（Cursemaven）"

    ```groovy
    repositories {
        maven { url = 'https://cursemaven.com' }
    }

    dependencies {
        // curse.maven:<slug>-<项目 ID>:<文件 ID>
        modImplementation "curse.maven:sentience-PROJECT:FILE"
    }
    ```

    项目 ID 位于 CurseForge 页面的 “About Project” 栏中；文件 ID 位于文件页面网址的末尾。

=== "Modrinth"

    ```groovy
    repositories {
        maven { url = 'https://api.modrinth.com/maven' }
    }

    dependencies {
        modImplementation "maven.modrinth:sentience:0.1.0"
    }
    ```

然后在你的 `fabric.mod.json` 中声明依赖：

```json
"depends": {
  "sentience": ">=0.1.0"
}
```

!!! info "内置还是依赖？"
    使用相同坐标再添加一行 `include`，即可把 Sentience 打包进你的 jar（jar-in-jar）：
    玩家无需额外安装。否则，玩家需要把 Sentience 的 jar 放进 `mods` 文件夹。

## 从源码构建

```bash
git clone https://github.com/Ananasloll3/Sentience.git
cd Sentience
./gradlew publishToMavenLocal
```

然后在你的项目中使用：`repositories { mavenLocal() }` 和
`modImplementation "com.ananasloll3:sentience:0.1.0"`。

## Simple Voice Chat（可选）

如果安装了 [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)，Sentience 会自动监听麦克风：
玩家的语音会像其他声音一样成为噪音。无需任何配置。
没有它，其余功能依然正常运行。
