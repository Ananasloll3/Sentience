package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/** Base des tâches qui choisissent un point à l'entrée puis y marchent (errer, fuir). */
public abstract class MoveToPoint extends BtNode {

    protected final double speed;
    private final Walker walker = new Walker();
    @Nullable
    private Vec3d point;

    protected MoveToPoint(double speed) {
        this.speed = speed;
    }

    /** Point à rejoindre, ou {@code null} pour échouer. */
    @Nullable
    protected abstract Vec3d choose(BtContext ctx);

    @Override
    protected void onStart(BtContext ctx) {
        walker.reset();
        point = choose(ctx);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        return point == null ? BtStatus.FAILURE : walker.step(ctx, point, speed, 1.5);
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        walker.stop(ctx);
    }
}
