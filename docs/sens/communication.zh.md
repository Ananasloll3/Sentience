# 通信

智能体可以把自己的关注点分享给盟友。信息到达时会**有所衰减**：不确定性 +3 格，
分数 ×0.8（`SenseType.REPORT`，`kind = "report"`）。

```java
// 通知 48 格内的同类生物
int warned = AgentComms.shareFocus(agent, 48, AgentComms.sameType(agent));

// 通知附近所有智能体
AgentComms.shareFocus(agent, 40, other -> true);
```

典型用法：在 `onAwarenessChanged` 中，当智能体开始追猎时调用。

## 声音警报

如果希望**所有**具有听觉的生物都能收到警报（可以穿墙，遵循声音物理），只需发出一个巨大的声音：

```java
HearingSystem.report(world, mob.getEyePos(), 105f, NoiseType.MECHANISM, mob);
```

[演示模组](../demo.md)中的哨兵同时使用了这两种方式：用无线电通知盟友，用钟声通知所有生物。
