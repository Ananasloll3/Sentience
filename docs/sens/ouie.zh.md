# 听觉与语音

```java
.hearing(HearingProfile.KEEN)
```

## 能听到什么

**服务器上播放的每一个声音**都会被自动捕捉，并转换为物理噪音（类型 + 分贝强度）：
脚步、摔落、游泳、方块破坏/放置/敲击、门、箱子、活塞、机械、战斗、弹射物、爆炸、生物叫声……
音乐、天气和界面音效会被忽略。

| 参考 | 声源处的分贝 |
|---|---|
| 耳语（麦克风） | ≈ 35 |
| 在羊毛、苔藓、雪上的脚步 | 42 |
| 脚步 | 50（疾跑 +6，**潜行：没有脚步声**） |
| 正常说话（麦克风） | 60 |
| 破坏方块 | 62 |
| 喊叫（麦克风） | ≈ 80 |
| 钟 | 95 |
| 爆炸 | 125 |

## 传播

对每个聆听者：

```
感知分贝 = 声源分贝 − 20·log10(距离) − 0.35 × 距离 − 遮挡
```

- **遮挡**：在声源与耳朵之间逐个方块计算。羊毛和地毯 −18 dB，石头约 −9，关闭的门 −7，
  打开的门 −0.5，树叶 −1.5，玻璃 −3.5……
- **掩蔽**：只有超过背景噪音的声音才能被听到——洞穴 12 dB、地表 18、雨 42、雷暴 50、流水、岩浆。
  超过 85 dB 的声音会造成暂时**失聪**。
- **定位**：方向判断准确，距离判断较差；声音越弱或越沉闷，定位越模糊。

所有这些数值都可以**按模组**调整：参见[你的模组规则](../guides/regles.md)。

## 配置

| 配置 | 阈值 | 范围 | 定位 |
|---|---|---|---|
| `HearingProfile.DEFAULT` | 15 dB | 64 | ×1 |
| `HearingProfile.KEEN` | 10 dB | 80 | ×1.4 |
| `HearingProfile.DULL` | 25 dB | 40 | ×0.7 |

```java
HearingProfile.KEEN.withThreshold(12f).withMaxRange(72).withLocalization(1.3)
```

## 过滤它感兴趣的内容

默认情况下，兴趣取决于噪音类型（人声比羊叫更有意思），并且智能体会忽略旁观者和同类。你可以修改这一点：

```java
.hearing(HearingProfile.DEFAULT, (agent, event) -> {
    if (event.type() == NoiseType.CREATURE) return 0;          // 忽略动物
    if (event.type() == NoiseType.VOICE) return 1.5;           // 对人声特别敏感
    return event.type().defaultInterest();
})
```

## 语音（Simple Voice Chat）

如果安装了 Simple Voice Chat，每位玩家的麦克风音量会在服务器端测量、自动校准
（每个麦克风的增益都不同），并转换为 `VOICE` 噪音。耳语模式会降低 15 dB。

其他语音模组也可以向 Sentience 提供数据：

```java
VoiceInput.submit(player.getUuid(), levelDbfs, whispering);   // 可在任意线程调用
```

## 发出你自己的噪音

```java
HearingSystem.report(world, pos, 90f, NoiseType.MECHANISM, sourceEntity);
```

`NoiseType`：`VOICE`、`FOOTSTEP`、`COMBAT`、`BLOCK`、`MECHANISM`、`PROJECTILE`、`EXPLOSION`、
`ITEM`、`CREATURE`、`GENERIC`。
