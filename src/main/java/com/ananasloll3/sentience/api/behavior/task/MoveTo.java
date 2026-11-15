package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import net.minecraft.util.math.Vec3d;

/**
 * Va jusqu'à la cible lue dans le Blackboard (position, bloc ou entité, suivie si elle bouge).
 * Réussit à moins de {@code acceptance} blocs ; échoue si la cible disparaît, si aucun chemin
 * n'existe ou après {@code timeout} ticks (0 = jamais).
 */
public class MoveTo extends BtNode {

    private final BlackboardKey<?> target;
    private final double speed;
    private final double acceptance;
    private final int timeout;
    private final Walker walker = new Walker();
    private int elapsed;

    public MoveTo(BlackboardKey<?> target, double speed, double acceptance, int timeout) {
        this.target = target;
        this.speed = speed;
        this.acceptance = acceptance;
        this.timeout = timeout;
    }

    @Override
    protected void onStart(BtContext ctx) {
        walker.reset();
        elapsed = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        elapsed += ctx.deltaTicks();
        if (timeout > 0 && elapsed > timeout) {
            return BtStatus.FAILURE;
        }
        Vec3d pos = BtTargets.position(ctx, target);
        return pos == null ? BtStatus.FAILURE : walker.step(ctx, pos, speed, acceptance);
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        walker.stop(ctx);
    }
}
