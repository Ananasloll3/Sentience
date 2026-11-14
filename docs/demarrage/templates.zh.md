# 原版生物与其他模组的生物

`AgentTemplates` 可以为某个现有类型的所有生物添加智能体，而无需改动它们的代码。
每当该生物生成或被加载时，模板都会被应用。

```java
@Override
public void onInitialize() {
    AgentTemplates.register(EntityType.ZOMBIE, agent -> agent
            .rules(MyMod.RULES)
            .hearing(HearingProfile.DEFAULT)
            .sight(SightConfig.humanoid()
                    .withTargets(e -> SightConfig.PLAYERS.test(e) || e instanceof VillagerEntity))
            .damage()
            .defaultGoals(3, 1.0, 1.2));
}
```

Sentience 的行为会叠加在生物原有的行为之上：僵尸保留原版攻击（优先级 2），
在没有目标时使用“调查”（优先级 3）。

!!! warning "每种类型只有一个模板"
    如果两个模组为同一类型注册了模板，最后注册的那个生效。
    对于你自己的生物，请直接在生物内部构建智能体。

## 没有智能体的实体

不是生物的实体（或者只想“听”的实体）可以实现 `HearingListener` 并手动注册：

```java
HearingSystem.register(world, myListener);   // 加载时
HearingSystem.unregister(myListener);        // 卸载时
```
