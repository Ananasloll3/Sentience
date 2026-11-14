package com.ananasloll3.sentience.api.blackboard;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mémoire de travail d'un agent, comme le Blackboard d'Unreal :
 * les composants y écrivent (perception, alerte…) et les comportements y lisent.
 */
public final class Blackboard {

    @FunctionalInterface
    public interface Listener {
        void onChanged(BlackboardKey<?> key, @Nullable Object oldValue, @Nullable Object newValue);
    }

    private final Map<BlackboardKey<?>, Object> values = new HashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    @Nullable
    public <T> T get(BlackboardKey<T> key) {
        return key.type().cast(values.get(key));
    }

    public <T> T getOrDefault(BlackboardKey<T> key, T fallback) {
        T value = get(key);
        return value != null ? value : fallback;
    }

    public <T> Optional<T> find(BlackboardKey<T> key) {
        return Optional.ofNullable(get(key));
    }

    public boolean has(BlackboardKey<?> key) {
        return values.containsKey(key);
    }

    /** Écrit une valeur ({@code null} l'efface). Les écouteurs ne sont prévenus que si elle change. */
    public <T> void set(BlackboardKey<T> key, @Nullable T value) {
        Object old = value == null ? values.remove(key) : values.put(key, key.type().cast(value));
        if (!java.util.Objects.equals(old, value)) {
            for (Listener listener : listeners) {
                listener.onChanged(key, old, value);
            }
        }
    }

    public void remove(BlackboardKey<?> key) {
        Object old = values.remove(key);
        if (old != null) {
            for (Listener listener : listeners) {
                listener.onChanged(key, old, null);
            }
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public Map<BlackboardKey<?>, Object> view() {
        return Collections.unmodifiableMap(values);
    }
}
