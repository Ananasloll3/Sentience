package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

/**
 * Frappe ce qui est à portée de bras pendant une alerte : les joueurs (jamais en créatif)
 * et l'entité qu'il est en train de traquer. Ne nécessite pas de voir la cible.
 */
public class PreyMeleeGoal extends Goal {

    private final MobEntity mob;
    private final Agent agent;
    private final double reach;
    private final int cooldownTicks;
    private int cooldown;

    public PreyMeleeGoal(MobEntity mob, Agent agent, double reach, int cooldownTicks) {
        this.mob = mob;
        this.agent = agent;
        this.reach = reach;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public boolean canStart() {
        return agent.awarenessState().atLeast(Awareness.SUSPICIOUS);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (--cooldown > 0 || !(mob.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }
        Stimulus focus = agent.awareness() == null ? null : agent.awareness().focus();
        LivingEntity victim = null;
        double best = reach * reach;
        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, mob.getBoundingBox().expand(reach),
                e -> e != mob && e.isAlive() && e.getType() != mob.getType())) {
            boolean isPrey = e instanceof PlayerEntity p
                    ? !p.isCreative() && !p.isSpectator()
                    : focus != null && e.getUuid().equals(focus.getSourceId());
            double d = mob.squaredDistanceTo(e);
            if (isPrey && d <= best) {
                victim = e;
                best = d;
            }
        }
        if (victim != null) {
            mob.getLookControl().lookAt(victim, 30f, 30f);
            mob.swingHand(Hand.MAIN_HAND);
            mob.tryAttack(world, victim);
            cooldown = cooldownTicks;
        }
    }
}
