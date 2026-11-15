package com.ananasloll3.sentience.api.blackboard;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clé typée du {@link Blackboard}. Deux clés sont égales si elles ont le même identifiant.
 *
 * <pre>{@code
 * public static final BlackboardKey<BlockPos> NEST = BlackboardKey.of(Identifier.of("monmod", "nest"), BlockPos.class);
 * }</pre>
 */
public record BlackboardKey<T>(Identifier id, Class<T> type) {

    private static final Map<Identifier, BlackboardKey<?>> KNOWN = new ConcurrentHashMap<>();

    public static <T> BlackboardKey<T> of(Identifier id, Class<T> type) {
        BlackboardKey<T> key = new BlackboardKey<>(id, type);
        KNOWN.putIfAbsent(id, key);
        return key;
    }

    /**
     * Clé déjà déclarée avec cet identifiant, ou une clé non typée sinon. Utilisé par les fichiers JSON
     * (Behavior Trees, requêtes) qui désignent les clés par leur identifiant.
     */
    public static BlackboardKey<?> byId(Identifier id) {
        BlackboardKey<?> known = KNOWN.get(id);
        return known != null ? known : new BlackboardKey<>(id, Object.class);
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
