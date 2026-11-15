package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.query.Ground;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/** Marche vers un point aléatoire à moins de {@code radius} blocs. */
public class Wander extends MoveToPoint {

    private final double radius;

    public Wander(double radius, double speed) {
        super(speed);
        this.radius = radius;
    }

    @Nullable
    @Override
    protected Vec3d choose(BtContext ctx) {
        return Ground.random(ctx.world(), ctx.mob().getEntityPos(), radius, ctx.random());
    }
}
