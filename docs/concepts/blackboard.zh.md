# 黑板

黑板（Blackboard）是智能体的**工作记忆**，与虚幻引擎中的概念相同：组件向其中写入，
行为从中读取。键是带类型的。

## 标准键

| 键 | 类型 | 写入者 |
|---|---|---|
| `BlackboardKeys.AWARENESS` | `Awareness` | 警觉 |
| `BlackboardKeys.FOCUS` | `Stimulus` | 警觉——优先级最高的记忆 |
| `BlackboardKeys.FOCUS_POSITION` | `Vec3d` | 警觉——智能体**认为**目标所在的位置 |
| `BlackboardKeys.FOCUS_SOURCE` | `UUID` | 警觉——是谁（如果已识别） |
| `BlackboardKeys.VISIBLE_TARGET` | `UUID` | 视觉——此刻确定看到的目标 |

## 你自己的键

```java
public static final BlackboardKey<BlockPos> NEST =
        BlackboardKey.of(Identifier.of("mymod", "nest"), BlockPos.class);

agent.blackboard().set(NEST, pos);
BlockPos nest = agent.blackboard().get(NEST);                 // 不存在则为 null
BlockPos where = agent.blackboard().getOrDefault(NEST, BlockPos.ORIGIN);
agent.blackboard().set(NEST, null);                           // 清除
```

## 监听变化

```java
agent.blackboard().addListener((key, oldValue, newValue) -> {
    if (key.equals(BlackboardKeys.VISIBLE_TARGET) && newValue != null) {
        // 一个目标刚刚进入了它的视野
    }
});
```

只有当值真正发生变化时，监听器才会被调用。
