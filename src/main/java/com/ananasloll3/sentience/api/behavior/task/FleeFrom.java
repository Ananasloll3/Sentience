package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.query.Ground;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/** S'éloigne d'une menace lue dans le Blackboard, d'environ {@code distance} blocs. */
public class FleeFrom extends MoveToPoint {

    private final BlackboardKey<?> threat;
    private final double distance;

    public FleeFrom(BlackboardKey<?> threat, double distance, double speed) {
        super(speed);
        this.threat = threat;
        this.distance = distance;
    }

    @Nullable
    @Override
    protected Vec3d choose(BtContext ctx) {
        Vec3d from = BtTargets.position(ctx, threat);
        if (from == null) {
            return null;
        }
        Vec3d self = ctx.mob().getEntityPos();
        Vec3d away = self.subtract(from).multiply(1, 0, 1);
        double base = away.lengthSquared() < 1e-4 ? ctx.random().nextDouble() * Math.PI * 2 : Math.atan2(away.z, away.x);
        Vec3d best = null;
        double bestDist = -1;
        for (int i = 0; i < 9; i++) {
            double angle = base + (i - 4) * Math.PI / 10;
            Vec3d candidate = Ground.snap(ctx.world(),
                    self.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance), 4, 6);
            if (candidate != null && candidate.squaredDistanceTo(from) > bestDist) {
                best = candidate;
                bestDist = candidate.squaredDistanceTo(from);
            }
        }
        return best;
    }
}
