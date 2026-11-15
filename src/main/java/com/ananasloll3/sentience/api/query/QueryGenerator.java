package com.ananasloll3.sentience.api.query;

import com.ananasloll3.sentience.api.agent.Agent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Produit les points candidats d'une requête (déjà posés au sol). */
@FunctionalInterface
public interface QueryGenerator {

    List<Vec3d> generate(Agent agent, ServerWorld world);

    /**
     * Anneaux concentriques autour d'une cible.
     *
     * @param rings   nombre d'anneaux entre {@code minRadius} et {@code maxRadius}
     * @param perRing points par anneau
     */
    static QueryGenerator ring(QueryTarget center, double minRadius, double maxRadius, int rings, int perRing) {
        return (agent, world) -> {
            Vec3d c = center.resolve(agent, world);
            List<Vec3d> points = new ArrayList<>();
            if (c == null) {
                return points;
            }
            Set<BlockPos> seen = new HashSet<>();
            double offset = agent.mob().getRandom().nextDouble() * Math.PI * 2;
            for (int r = 0; r < Math.max(1, rings); r++) {
                double radius = rings <= 1 ? maxRadius : minRadius + (maxRadius - minRadius) * r / (rings - 1);
                int count = Math.max(1, perRing);
                for (int i = 0; i < count; i++) {
                    double angle = offset + (i + (r % 2) * 0.5) * Math.PI * 2 / count;
                    add(world, c.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius), seen, points);
                }
            }
            return points;
        };
    }

    /** Grille carrée autour d'une cible. */
    static QueryGenerator grid(QueryTarget center, double radius, double spacing) {
        return (agent, world) -> {
            Vec3d c = center.resolve(agent, world);
            List<Vec3d> points = new ArrayList<>();
            if (c == null) {
                return points;
            }
            Set<BlockPos> seen = new HashSet<>();
            double step = Math.max(1.0, spacing);
            for (double dx = -radius; dx <= radius; dx += step) {
                for (double dz = -radius; dz <= radius; dz += step) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        add(world, c.add(dx, 0, dz), seen, points);
                    }
                }
            }
            return points;
        };
    }

    private static void add(ServerWorld world, Vec3d raw, Set<BlockPos> seen, List<Vec3d> points) {
        Vec3d ground = Ground.snap(world, raw, 4, 6);
        if (ground != null && seen.add(BlockPos.ofFloored(ground))) {
            points.add(ground);
        }
    }
}
