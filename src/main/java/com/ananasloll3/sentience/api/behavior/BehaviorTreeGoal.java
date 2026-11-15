package com.ananasloll3.sentience.api.behavior;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;

/**
 * Goal vanilla qui exécute le Behavior Tree. Les goals plus prioritaires (nager, paniquer…) passent
 * avant ; l'arbre est alors interrompu, puis reprend depuis la racine.
 */
public class BehaviorTreeGoal extends Goal {

    private final MobEntity mob;
    private final BehaviorTreeComponent tree;

    public BehaviorTreeGoal(MobEntity mob, BehaviorTreeComponent tree) {
        this.mob = mob;
        this.tree = tree;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return tree.isReady();
    }

    @Override
    public boolean shouldContinue() {
        return tree.isReady();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (mob.getEntityWorld() instanceof ServerWorld world) {
            tree.run(world);
        }
    }

    @Override
    public void stop() {
        tree.abort();
    }
}
