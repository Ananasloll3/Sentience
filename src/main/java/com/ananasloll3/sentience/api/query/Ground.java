package com.ananasloll3.sentience.api.query;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

/** Petits outils pour trouver des positions où un mob peut se tenir debout. */
public final class Ground {

    private Ground() {
    }

    /** Un mob de 2 blocs de haut peut-il se tenir à cette position ? */
    public static boolean isStandable(ServerWorld world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        if (below.getCollisionShape(world, pos.down()).isEmpty() || !below.getFluidState().isEmpty()) {
            return false;
        }
        return isPassable(world, pos) && isPassable(world, pos.up());
    }

    private static boolean isPassable(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getCollisionShape(world, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    /**
     * Cherche le sol le plus proche verticalement (de {@code up} blocs au-dessus à {@code down} en dessous).
     *
     * @return le centre du bloc où se tenir, ou {@code null}
     */
    @Nullable
    public static Vec3d snap(ServerWorld world, Vec3d pos, int up, int down) {
        BlockPos base = BlockPos.ofFloored(pos);
        for (int dy = 0; dy <= Math.max(up, down); dy++) {
            if (dy <= up && isStandable(world, base.up(dy))) {
                return Vec3d.ofBottomCenter(base.up(dy));
            }
            if (dy > 0 && dy <= down && isStandable(world, base.down(dy))) {
                return Vec3d.ofBottomCenter(base.down(dy));
            }
        }
        return null;
    }

    /** Position debout aléatoire à moins de {@code radius} blocs de {@code center}, ou {@code null}. */
    @Nullable
    public static Vec3d random(ServerWorld world, Vec3d center, double radius, Random random) {
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = radius * Math.sqrt(random.nextDouble());
            Vec3d point = snap(world, center.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist), 3, 4);
            if (point != null) {
                return point;
            }
        }
        return null;
    }
}
