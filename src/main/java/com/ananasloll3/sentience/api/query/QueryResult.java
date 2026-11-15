package com.ananasloll3.sentience.api.query;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Points retenus par une requête, du meilleur au moins bon. */
public record QueryResult(List<ScoredPoint> points) {

    public record ScoredPoint(Vec3d pos, double score) {
    }

    public static final QueryResult EMPTY = new QueryResult(List.of());

    @Nullable
    public Vec3d best() {
        return points.isEmpty() ? null : points.getFirst().pos();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }
}
