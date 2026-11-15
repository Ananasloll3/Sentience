---
hide:
  - navigation
---

# Sentience

**为你的 Minecraft 模组打造真正能感知世界的 AI。**

Sentience 是一个受虚幻引擎 AI 工具启发的 Fabric 库（Minecraft 1.21.10）。
它可以为任意生物——你自己的、原版的、或者其他模组的——附加一个**智能体（Agent）**：
逼真的感官、记忆、黑板（Blackboard）和警觉等级，并附带开箱即用的行为。

![演示生物](assets/mobs.png)

<div class="grid cards" markdown>

-   :material-ear-hearing: **真实的听觉**

    ---

    游戏中的每一个声音都会被捕捉并按物理规律传播：距离衰减、墙体、材质，
    以及能掩盖你脚步声的背景噪音。**玩家的语音**（Simple Voice Chat）同样可以被听到。

    [:octicons-arrow-right-24: 听觉与语音](sens/ouie.md)

-   :material-eye: **可信的视觉**

    ---

    视野锥、周边视觉、黑暗、潜行、伪装，以及像潜行游戏一样逐步填充的发现进度条。

    [:octicons-arrow-right-24: 视觉](sens/vue.md)

-   :material-dog-side: **嗅觉与踪迹**

    ---

    玩家会留下气味踪迹，水会切断它，雨会冲淡它。血腥味会吸引追踪者。
    追踪者会一步步沿着踪迹追来。

    [:octicons-arrow-right-24: 嗅觉](sens/odorat.md)

-   :material-brain: **记忆与警觉**

    ---

    智能体永远不知道你的真实位置：它会融合自己的感知、推测、前去查看、
    搜索、追猎，最后放弃。

    [:octicons-arrow-right-24: 警觉](concepts/alerte.md)

-   :material-tune-variant: **按模组区分的规则**

    ---

    每个模组都有自己的规则（雨、材质、伪装……），永远不会影响其他模组的规则。

    [:octicons-arrow-right-24: 规则](guides/regles.md)

-   :material-file-tree: **行为树与 EQS**

    ---

    用 JSON 描述行为，用 `/reload` 重新加载。环境查询可以找到藏身处、掩体或包抄目标的位置。

    [:octicons-arrow-right-24: 行为树](decision/behavior-trees.md)

-   :material-puzzle: **可扩展**

    ---

    创建你自己的感官、组件、声音、气味和隐蔽修正。

    [:octicons-arrow-right-24: 创建感官](guides/sens-custom.md)

</div>

## 十行代码上手

```java
@Override
protected void initGoals() {
    Agent agent = Agent.builder(this)
            .rules(MyMod.RULES)                  // 你的感知规则（可选）
            .hearing(HearingProfile.KEEN)        // 听觉 + 麦克风
            .sight(SightConfig.nocturnal())      // 视觉
            .scent(ScentSense.hound())           // 嗅觉
            .damage().touch()                    // 痛觉、触觉
            .awareness(AwarenessProfile.DEFAULT) // 怀疑 → 调查 → 追猎
            .defaultGoals(2, 0.9, 1.4)           // 倾听、调查、搜索、追猎
            .build();

    agent.onAwarenessChanged((a, from, to) -> { /* 低吼、动画…… */ });
}
```

!!! tip "想先看看效果再写代码？"
    [演示模组](demo.md)包含四种生物和一张现成可玩的测试竞技场地图。
