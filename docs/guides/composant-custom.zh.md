# 创建组件

`AgentComponent` 是附加在智能体上的一段逻辑：它每个 tick 都会思考，读写黑板，并可以控制生物。
它是实现性格特征的理想位置。

## 示例：怕光的生物

取自演示模组（憎恶体）。组件负责**测量**并写入黑板；另一个独立的 goal 负责**决定**逃跑。
两者只通过黑板相互了解。

```java
public class PhotophobiaComponent implements AgentComponent {

    public static final BlackboardKey<Boolean> BLINDED =
            BlackboardKey.of(Identifier.of("mymod", "blinded"), Boolean.class);

    private final int threshold;
    private int cooldown;

    public PhotophobiaComponent(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void tick(Agent agent, ServerWorld world, int deltaTicks) {
        cooldown -= deltaTicks;
        if (cooldown > 0) return;
        cooldown = 5;

        int light = world.getLightLevel(agent.mob().getBlockPos().up());
        boolean blinded = light >= threshold;
        agent.blackboard().set(BLINDED, blinded);
        if (blinded) {
            agent.mob().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false));
        }
    }
}
```

```java
public class FleeLightGoal extends Goal {
    @Override
    public boolean canStart() {
        return Boolean.TRUE.equals(agent.blackboard().get(PhotophobiaComponent.BLINDED))
                && agent.awarenessState() != Awareness.HUNT;   // 猎物比疼痛更重要
    }
    // ……寻找最暗的地方并跑过去
}
```

```java
Agent.builder(this)
        .component(new PhotophobiaComponent(10))
        ...
        .build();
goalSelector.add(2, new FleeLightGoal(this, agent, 1.3));
```

## 方法

| 方法 | 时机 |
|---|---|
| `onAttach(agent)` | 构建智能体时调用一次 |
| `onLoad(agent, world)` | 生物生成 / 其区块加载 |
| `tick(agent, world, deltaTicks)` | 每次思考（在感知和警觉之后） |
| `onUnload(agent)` | 生物消失 |

在任何地方都可以通过 `agent.find(PhotophobiaComponent.class)` 获取组件。
