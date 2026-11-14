package com.ananasloll3.sentience.api.blackboard;

import net.minecraft.util.Identifier;

/**
 * Clé typée du {@link Blackboard}. Deux clés sont égales si elles ont le même identifiant.
 *
 * <pre>{@code
 * public static final BlackboardKey<BlockPos> NEST = BlackboardKey.of(Identifier.of("monmod", "nest"), BlockPos.class);
 * }</pre>
 */
public record BlackboardKey<T>(Identifier id, Class<T> type) {

    public static <T> BlackboardKey<T> of(Identifier id, Class<T> type) {
        return new BlackboardKey<>(id, type);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlackboardKey<?> other && other.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
