# 嗅觉

```java
.scent(ScentSense.hound())
```

## 踪迹

玩家每 10 tick（0.5 秒）留下一个**气味点**。每个点都会记录它是否留在**水中**或是在**潜行**时留下的。
受伤的生物会留下**血迹**——强度是普通气味的 2.5 倍——当生命值低于 40% 时，行走中会持续流血。

默认情况下：

- 气味持续 **2 分钟**；
- **雨**会让它消散得快一倍；
- **水**无法保留气味：渡河会切断踪迹；
- 气味在痕迹和鼻子之间**最多穿过一个实心方块**。

一切都可以通过[规则](../guides/regles.md#smell)按模组调整。

## 鼻子

每 10 tick，智能体会寻找范围内最强的气味（强度相同时选择最新的，也就是行进方向*前方*的那个）并上报。
沿着它走，就能一步步追踪踪迹。到达终点后，最后一个气味不再触发它：它会在附近搜索，然后放弃。

```java
ScentSense.hound()                         // 8 格，所有踪迹
ScentSense.of(12, 15f)                     // 12 格，更灵敏
ScentSense.hound().playersOnly()           // 只追踪玩家
ScentSense.hound().only(p -> p.kind().equals(ScentTrails.BLOOD))   // 只追踪血迹
```

## 让其他实体留下踪迹

```java
ScentTrails.addEmitter(EntityType.VILLAGER);
```

这是叠加式的：每个模组添加自己的类型，不会移除其他模组添加的类型。

## 放置气味

```java
ScentTrails.deposit(world, pos, 3f, baitUuid);             // 自定义气味（诱饵、尸体……）
ScentTrails.deposit(world, entity, 1f, ScentTrails.TRAIL); // 在实体当前位置留下它的气味
```

## 查看踪迹

`/sentience debug` 会显示你周围最近的踪迹：
**绿色** = 气味，**红色** = 血迹，**灰色** = 水中。
`/sentiencetest piste <起点> <终点>`（演示模组）可以立即铺设一条踪迹。
