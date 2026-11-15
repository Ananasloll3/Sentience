package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;

/**
 * Poursuit l'entité désignée par le Blackboard et la frappe au corps à corps.
 * Réussit quand elle meurt, échoue si elle disparaît ou devient inaccessible.
 * Le mob doit avoir l'attribut de dégâts d'attaque.
 */
public class Attack extends BtNode {

    private final BlackboardKey<?> target;
    private final double speed;
    private final int cooldown;
    private final Walker walker = new Walker();
    private int nextHit;

    public Attack(BlackboardKey<?> target, double speed, int cooldown) {
        this.target = target;
        this.speed = speed;
        this.cooldown = cooldown;
    }

    @Override
    protected void onStart(BtContext ctx) {
        walker.reset();
        nextHit = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        LivingEntity victim = BtTargets.living(ctx, target);
        if (victim == null) {
            return BtStatus.FAILURE;
        }
        if (!victim.isAlive()) {
            return BtStatus.SUCCESS;
        }
        MobEntity mob = ctx.mob();
        mob.getLookControl().lookAt(victim, 30f, 30f);
        nextHit -= ctx.deltaTicks();
        if (mob.isInAttackRange(victim)) {
            mob.getNavigation().stop();
            walker.reset();
            if (nextHit <= 0 && mob.canSee(victim)) {
                mob.swingHand(Hand.MAIN_HAND);
                mob.tryAttack(ctx.world(), victim);
                nextHit = cooldown;
                if (!victim.isAlive()) {
                    return BtStatus.SUCCESS;
                }
            }
            return BtStatus.RUNNING;
        }
        BtStatus move = walker.step(ctx, victim.getEntityPos(), speed, 0.5);
        return move == BtStatus.FAILURE ? BtStatus.FAILURE : BtStatus.RUNNING;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        walker.stop(ctx);
    }
}
