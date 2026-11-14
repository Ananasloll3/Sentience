# 你的模组规则

感知的“物理”规则（雨、材质、黑暗、伪装……）都有逼真的默认值，但**每个模组都可以拥有自己的规则**。

!!! success "模组之间不会冲突"
    规则在**感知者一侧**生效：同一个声音，你的模组的生物按你的规则来听，
    另一个模组的生物按它们自己的规则来听。没有人会修改全局规则。

## 创建你的规则

```java
public static final SentienceRules RULES = SentienceRules.create("mymod")
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

如果不调用 `.rules(...)`，智能体会使用 `SentienceRules.DEFAULT`，它永远不会改变。

### 变体

可以为特定生物派生一套规则：

```java
public static final SentienceRules BOSS_RULES = RULES.derive(Identifier.of("mymod", "boss"))
        .sight(s -> s.invisibility(1f))      // 能看见隐身的玩家
        .build();
```

## 听觉 {#hearing}

| 设置 | 默认值 | 方法 |
|---|---|---|
| 空气吸收 | 0.35 dB/格 | `airAbsorption(db)` |
| 最大遮挡 | 60 dB | `maxOcclusion(db)` |
| 失聪阈值 | 85 dB | `deafening(db)` |
| 背景噪音 洞穴 / 地表 / 雨 / 雷暴 | 12 / 18 / 42 / 50 dB | `ambient(...)`、`ignoreRain()` |
| 流水 / 岩浆 | 38 / 30 dB | `fluidAmbient(water, lava)` |
| 羊毛、地毯 | 18 dB | `muffled(db)` |
| 关闭 / 打开的门 | 7 / 0.5 dB | `doors(closed, open)` |
| 树叶 | 1.5 dB | `leaves(db)` |
| 实心方块 | 9 + 硬度 × 0.2（不可破坏 25） | `solids(base, factor, unbreakable)` |
| 薄方块（玻璃、台阶……） | 3.5 dB | `thinSolids(db)` |
| 穿过的水 / 岩浆 | 4 / 6 dB | `fluids(water, lava)` |

**扩展点：**

- `addNoiseModifier((event, db) -> newDb)`——修改声音的强度，或返回 `Float.NaN` 忽略它。
  `event.soundId()` 提供原始的 Minecraft 声音（例如 `minecraft:block.wool.step`）。
- `addBlockAcoustics((world, pos, state) -> 分贝或 null)`——方块的衰减值，优先于内置规则。
- `addAmbientSource((world, pos) -> 分贝或 Float.NaN)`——额外的背景噪音。

## 视觉 {#sight}

| 设置 | 默认值 | 方法 |
|---|---|---|
| 完全黑暗中的可见度 | 0.05 | `darknessFloor(v)` |
| 周边视觉 | ×0.3 | `peripheral(f)` |
| 潜行 | ×0.5 | `sneak(f)` |
| 隐身 | ×0.08 | `invisibility(f)` |
| 移动 | ×1.3 | `movement(f)` |
| 手持火把 / 着火 / 发光效果会暴露 | 是 | `lightInHandReveals`、`fireReveals`、`glowingReveals` |
| 立即发现 | < 2.5 格 | `instantDetection(distance)` |
| 可透视的方块 | 玻璃、玻璃板、铁栏杆 | `seeThrough(predicate)` |

**扩展点：** `addStealthModifier((observer, target) -> 系数)`——伪装、斗篷、涂料……

## 嗅觉 {#smell}

| 设置 | 默认值 | 方法 |
|---|---|---|
| 气味持续时间 | 2400 tick（最大 12000） | `lifetime(ticks)` |
| 雨的影响 | 持续时间 ×0.5 | `rainLifetime(f)`、`ignoreRain()` |
| 水会切断踪迹 | 是 | `waterBreaksTrail(bool)` |
| 潜行时的气味 | ×1 | `sneakStrength(f)` |
| 血迹吸引力 | ×1 | `blood(f)` |
| 气味可穿过的墙 | 1 个实心方块 | `maxWallBlocks(n)`（-1 = 无限） |

**扩展点：** `addPointModifier((agent, point) -> 系数)`——除味药水、偏好的猎物……
