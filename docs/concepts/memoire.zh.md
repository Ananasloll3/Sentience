# 记忆与刺激

每种感官都会产生**原始感知**（`StimulusInput`）。记忆（`StimulusMemory`）会将它们融合为
**刺激（Stimulus）**：类似“那边大概有什么东西”的记忆。

## 刺激包含的内容

| 访问方法 | 含义 |
|---|---|
| `getPosition()` | **估计的**位置 |
| `getErrorRadius()` | 不确定性（以格为单位） |
| `getScore(now)` | 当前优先级（会随时间衰减） |
| `getLastSense()`、`getSenses()`、`wasSensedBy(感官)` | 哪些感官确认过它 |
| `getKind()` | 细节：`footstep`、`voice`、`seen`、`glimpse`、`scent`、`blood`、`melee`…… |
| `getSourceId()` | 已识别的实体（如果已知） |
| `getPeakIntensity()` | 物理测量值（听觉为分贝……） |
| `getCount()`、`getFirstTick()`、`getLastTick()` | 历史记录 |

## 融合

位置相近或来自同一来源的两次感知会被融合：

- **同一位置**：按精度加权（1/σ²）平均位置，不确定性随之减小——就像三角定位。
- **同一来源发生了移动**：新位置会**替换**旧位置，避免智能体瞄准一个过时的点。
- 每次重复都会增加少量优先级：反复的脚步声最终会引起注意。

## 衰减

分数的半衰期为 160 tick。当分数过低，或超过记忆时长（默认 600 tick，`.memory(容量, ticks)`）时，
刺激会被遗忘。

## 直接使用

```java
StimulusMemory memory = agent.perception().memory();
Stimulus best = memory.best(world.getTime());
Stimulus player = memory.findBySource(player.getUuid());
memory.forgetNear(pos, 5.0);

// 注入信息（例如：盟友发出警告、陷阱被触发）
agent.perception().report(new StimulusInput(SenseType.REPORT, pos, 3.0, 20f, 0f, "trap", null));
```

## 分数参考 {#score}

| 情况 | 典型分数 |
|---|---|
| 8 格外的脚步声 | ≈ 6 |
| 10 格外的正常说话声 | ≈ 18 |
| 瞥见（视觉进度条 35%） | ≈ 4 |
| 清楚看到目标 | 30 |
| 触碰 | 25 |
| 受到攻击 | 40 |
