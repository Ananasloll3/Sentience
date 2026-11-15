package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.query.Ground;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/** Fouille la zone autour d'une position pendant {@code duration} ticks, puis réussit. */
public class Search extends BtNode {

    private final BlackboardKey<?> center;
    private final double radius;
    private final int duration;
    private final double speed;
    private final Walker walker = new Walker();

    @Nullable
    private Vec3d origin;
    @Nullable
    private Vec3d point;
    private int elapsed;
    private int pause;

    public Search(BlackboardKey<?> center, double radius, int duration, double speed) {
        this.center = center;
        this.radius = radius;
        this.duration = duration;
        this.speed = speed;
    }

    @Override
    protected void onStart(BtContext ctx) {
        Vec3d pos = BtTargets.position(ctx, center);
        origin = pos != null ? pos : ctx.mob().getEntityPos();
        point = null;
        elapsed = 0;
        pause = 0;
        walker.reset();
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        elapsed += ctx.deltaTicks();
        if (elapsed >= duration) {
            return BtStatus.SUCCESS;
        }
        if (pause > 0) {
            pause -= ctx.deltaTicks();
            return BtStatus.RUNNING;
        }
        if (point == null) {
            point = Ground.random(ctx.world(), origin, radius, ctx.random());
            walker.reset();
            if (point == null) {
                pause = 20;
                return BtStatus.RUNNING;
            }
        }
        if (walker.step(ctx, point, speed, 1.5) != BtStatus.RUNNING) {
            point = null;
            pause = 10 + ctx.random().nextInt(25);
        }
        return BtStatus.RUNNING;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        walker.stop(ctx);
    }
}
