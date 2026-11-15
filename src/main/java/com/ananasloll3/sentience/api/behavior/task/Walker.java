package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/** Marche vers un point (éventuellement mobile) avec la navigation du mob. Un par nœud. */
final class Walker {

    @Nullable
    private Vec3d goal;
    private int repath;
    private int stuck;

    void reset() {
        goal = null;
        repath = 0;
        stuck = 0;
    }

    BtStatus step(BtContext ctx, Vec3d pos, double speed, double acceptance) {
        MobEntity mob = ctx.mob();
        if (arrived(mob, pos, acceptance)) {
            return BtStatus.SUCCESS;
        }
        repath -= ctx.deltaTicks();
        boolean moved = goal != null && pos.squaredDistanceTo(goal) > 1.0;
        if (goal == null || (repath <= 0 && (moved || mob.getNavigation().isIdle()))) {
            if (!mob.getNavigation().startMovingTo(pos.x, pos.y, pos.z, speed)) {
                return BtStatus.FAILURE;
            }
            goal = pos;
            repath = 10;
        }
        if (mob.getNavigation().isIdle()) {
            stuck += ctx.deltaTicks();
            if (stuck > 30) {
                return BtStatus.FAILURE;
            }
        } else {
            stuck = 0;
        }
        return BtStatus.RUNNING;
    }

    void stop(BtContext ctx) {
        ctx.mob().getNavigation().stop();
        reset();
    }

    static boolean arrived(MobEntity mob, Vec3d pos, double acceptance) {
        double dx = mob.getX() - pos.x;
        double dz = mob.getZ() - pos.z;
        return dx * dx + dz * dz <= acceptance * acceptance && Math.abs(mob.getY() - pos.y) < 2.5;
    }
}
