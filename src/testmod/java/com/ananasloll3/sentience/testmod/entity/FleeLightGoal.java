package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Fuit vers l'endroit le plus sombre des environs quand le Blackboard dit qu'elle est éblouie.
 * Ne fuit pas en pleine chasse : la proie compte plus que la douleur.
 */
public class FleeLightGoal extends Goal {

    private final MobEntity mob;
    private final Agent agent;
    private final double speed;

    public FleeLightGoal(MobEntity mob, Agent agent, double speed) {
        this.mob = mob;
        this.agent = agent;
        this.speed = speed;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return Boolean.TRUE.equals(agent.blackboard().get(PhotophobiaComponent.BLINDED))
                && agent.awarenessState() != Awareness.HUNT;
    }

    @Override
    public void start() {
        BlockPos origin = mob.getBlockPos();
        BlockPos darkest = null;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < 16; i++) {
            BlockPos candidate = origin.add(mob.getRandom().nextBetween(-8, 8), 0, mob.getRandom().nextBetween(-8, 8));
            int light = mob.getEntityWorld().getLightLevel(candidate.up());
            if (light < best) {
                best = light;
                darkest = candidate;
            }
        }
        if (darkest != null) {
            mob.getNavigation().startMovingTo(darkest.getX() + 0.5, darkest.getY(), darkest.getZ() + 0.5, speed);
        }
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigation().isIdle() && agent.awarenessState() != Awareness.HUNT;
    }
}
