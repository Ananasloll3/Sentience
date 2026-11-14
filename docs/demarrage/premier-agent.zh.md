# 第一个智能体

**智能体（Agent）**就是附加在生物身上的“大脑”。你的生物依然是 `HostileEntity`、`PathAwareEntity`……
不需要继承任何特殊的类。

## 1. 构建智能体

最简单的位置是 `initGoals()`，它在生物创建时调用一次：

```java
public class GuardianEntity extends HostileEntity {

    private Agent agent;

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .hearing(HearingProfile.DEFAULT)      // (1)!
                .sight(SightConfig.humanoid())        // (2)!
                .damage()                             // (3)!
                .awareness(AwarenessProfile.DEFAULT)  // (4)!
                .defaultGoals(2, 0.9, 1.4)            // (5)!
                .build();

        goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        goalSelector.add(6, new LookAroundGoal(this));
    }
}
```

1. 能听到游戏中的所有声音以及玩家的语音。
2. 能在 110° 的视野锥内看到玩家，会受黑暗影响。
3. 受伤时会知道攻击来自哪里。
4. 阈值：怀疑、调查、追猎。
5. 添加“倾听”（优先级 3）和“调查 / 追猎”（优先级 2），步行速度 0.9，奔跑速度 1.4。

就这么简单：生物生成时，智能体会自动注册到各个感知系统中，
并在每个服务器 tick 中、原版 AI 之前进行思考。

## 2. 做出反应

```java
agent.onAwarenessChanged((a, from, to) -> {
    if (to == Awareness.HUNT) {
        playSound(SoundEvents.ENTITY_WARDEN_ROAR, 2f, 1f);
    }
});

agent.onStimulus((a, stimulus) -> {
    if (stimulus.wasSensedBy(SenseType.SIGHT) && "seen".equals(stimulus.getKind())) {
        // 它刚刚清楚地看到了你
    }
});
```

## 3. 读取它所知道的信息

```java
Agent agent = Agent.of(mob);                                   // 任意生物
Awareness level = agent.awarenessState();                      // IDLE、SUSPICIOUS、INVESTIGATE、HUNT
Vec3d guess = agent.blackboard().get(BlackboardKeys.FOCUS_POSITION);
UUID visible = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);
```

!!! warning "它永远不知道你的真实位置"
    `FOCUS_POSITION` 是一个**估计值**：如果声音微弱或被遮挡，它就很模糊；
    如果智能体清楚地看到了你，它就很精确。正是这一点让行为显得可信——你在自己的行为中也应该使用它。

## 下一步

- [原版生物与其他模组的生物](templates.md)：不改动僵尸的代码就给它一个智能体。
- [你的模组规则](../guides/regles.md)：调整雨、材质、伪装……
- [调试](../guides/debug.md)：用 `/sentience debug` 查看它在想什么。
