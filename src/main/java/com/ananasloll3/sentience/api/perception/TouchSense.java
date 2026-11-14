package com.ananasloll3.sentience.api.perception;

import com.ananasloll3.sentience.api.agent.Agent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.function.Predicate;

/** Toucher : une cible qui frôle l'agent se trahit, même dans le noir complet. */
public final class TouchSense implements SenseModule {

    public static final float TOUCH_SCORE = 25f;
    private static final int CHECK_INTERVAL = 4;

    private final Predicate<LivingEntity> targets;
    private int cooldown;

    public TouchSense(Predicate<LivingEntity> targets) {
        this.targets = targets;
    }

    @Override
    public SenseType type() {
        return SenseType.TOUCH;
    }

    @Override
    public void tick(Agent agent, PerceptionComponent perception, ServerWorld world, int deltaTicks) {
        cooldown -= deltaTicks;
        if (cooldown > 0) {
            return;
        }
        cooldown = CHECK_INTERVAL;
        for (LivingEntity other : world.getEntitiesByClass(LivingEntity.class,
                agent.mob().getBoundingBox().expand(0.35),
                e -> e != agent.mob() && e.isAlive() && targets.test(e))) {
            perception.report(StimulusInput.exact(SenseType.TOUCH, other, TOUCH_SCORE, "touch"));
        }
    }
}
