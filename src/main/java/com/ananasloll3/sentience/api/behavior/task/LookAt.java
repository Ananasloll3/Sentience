package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/** Regarde la cible pendant {@code duration} ticks (0 = un seul tick), puis réussit. */
public class LookAt extends BtNode {

    private final BlackboardKey<?> target;
    private final int duration;
    private int remaining;

    public LookAt(BlackboardKey<?> target, int duration) {
        this.target = target;
        this.duration = duration;
    }

    @Override
    protected void onStart(BtContext ctx) {
        remaining = duration;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        Entity entity = BtTargets.entity(ctx, target);
        Vec3d pos = entity != null ? entity.getEyePos() : BtTargets.position(ctx, target);
        if (pos == null) {
            return BtStatus.FAILURE;
        }
        ctx.mob().getLookControl().lookAt(pos.x, pos.y, pos.z, 30f, 30f);
        remaining -= ctx.deltaTicks();
        return remaining <= 0 ? BtStatus.SUCCESS : BtStatus.RUNNING;
    }
}
