package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Lit une cible dans le Blackboard, quel que soit son type : {@link Vec3d}, {@link BlockPos},
 * {@link UUID} d'entité, {@link Entity} ou {@link Stimulus}.
 */
public final class BtTargets {

    private BtTargets() {
    }

    @Nullable
    public static Vec3d position(BtContext ctx, BlackboardKey<?> key) {
        return position(ctx.world(), ctx.blackboard().get(key));
    }

    @Nullable
    public static Vec3d position(ServerWorld world, @Nullable Object value) {
        if (value instanceof Vec3d vec) return vec;
        if (value instanceof BlockPos pos) return Vec3d.ofBottomCenter(pos);
        if (value instanceof Stimulus stimulus) return stimulus.getPosition();
        Entity entity = entity(world, value);
        return entity != null ? entity.getEntityPos() : null;
    }

    @Nullable
    public static Entity entity(BtContext ctx, BlackboardKey<?> key) {
        return entity(ctx.world(), ctx.blackboard().get(key));
    }

    @Nullable
    public static Entity entity(ServerWorld world, @Nullable Object value) {
        Entity entity = null;
        if (value instanceof Entity e) entity = e;
        else if (value instanceof UUID uuid) entity = world.getEntity(uuid);
        else if (value instanceof Stimulus stimulus && stimulus.getSourceId() != null) entity = world.getEntity(stimulus.getSourceId());
        return entity != null && entity.isAlive() && !entity.isRemoved() ? entity : null;
    }

    @Nullable
    public static LivingEntity living(BtContext ctx, BlackboardKey<?> key) {
        return entity(ctx, key) instanceof LivingEntity living ? living : null;
    }
}
