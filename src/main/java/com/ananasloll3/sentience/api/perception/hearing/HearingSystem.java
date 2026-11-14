package com.ananasloll3.sentience.api.perception.hearing;

import com.ananasloll3.sentience.api.rules.HearingRules;
import com.ananasloll3.sentience.impl.debug.SentienceDebug;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Système de bruit du monde (l'équivalent du {@code ReportNoiseEvent} d'Unreal).
 * Tous les sons du jeu y passent automatiquement ; un mod peut aussi signaler ses propres bruits
 * avec {@link #report}. Les bruits sont distribués en fin de tick du monde, doublons fusionnés.
 */
public final class HearingSystem {

    private static final int AMBIENT_REFRESH_TICKS = 20;
    private static final int MAX_EVENTS_PER_TICK = 256;

    private static final Map<ServerWorld, WorldState> STATES = new WeakHashMap<>();

    private HearingSystem() {
    }

    // ------------------------------------------------------------------ API publique

    /** Signale un bruit. Sans effet hors du thread serveur ou si personne n'écoute dans ce monde. */
    public static void report(ServerWorld world, Vec3d pos, float db, NoiseType type, @Nullable Entity source) {
        report(world, pos, db, type, source, null);
    }

    /**
     * Signale un bruit en précisant le son Minecraft d'origine, pour que les règles de chaque mod
     * puissent le reconnaître ({@link HearingRules.NoiseModifier}).
     */
    public static void report(ServerWorld world, Vec3d pos, float db, NoiseType type, @Nullable Entity source,
                              @Nullable Identifier soundId) {
        if (!world.getServer().isOnThread()) {
            return;
        }
        WorldState state = STATES.get(world);
        if (state == null || state.listeners.isEmpty()) {
            return;
        }
        NoiseEvent event = new NoiseEvent(world, pos, db, type, source, world.getTime(), soundId);
        long key = source != null
                ? ((long) source.getId() << 8) | type.ordinal()
                : (BlockPos.ofFloored(pos).asLong() * 31L) ^ type.ordinal();
        state.pending.merge(key, event, (a, b) -> a.db() >= b.db() ? a : b);
    }

    public static void register(ServerWorld world, HearingListener listener) {
        STATES.computeIfAbsent(world, w -> new WorldState()).listeners.put(listener, new ListenerState());
    }

    public static void unregister(HearingListener listener) {
        for (WorldState state : STATES.values()) {
            state.listeners.remove(listener);
        }
    }

    /** Niveau de bruit de fond subi par un auditeur (ambiance + assourdissement). */
    public static float getAmbientFor(HearingListener listener) {
        Entity entity = listener.listenerEntity();
        if (!(entity.getEntityWorld() instanceof ServerWorld world)) {
            return 0f;
        }
        WorldState state = STATES.get(world);
        ListenerState ls = state == null ? null : state.listeners.get(listener);
        return ls == null ? AmbientNoise.levelAt(world, entity.getBlockPos(), listener.hearingRules()) : ls.effectiveAmbient();
    }

    // ------------------------------------------------------------------ interne

    public static void tick(ServerWorld world) {
        WorldState state = STATES.get(world);
        if (state == null) {
            return;
        }
        if (state.listeners.isEmpty()) {
            state.pending.clear();
            return;
        }

        for (Map.Entry<HearingListener, ListenerState> e : state.listeners.entrySet()) {
            e.getValue().update(world, e.getKey(), world.getTime());
        }
        if (state.pending.isEmpty()) {
            return;
        }

        List<NoiseEvent> events = new ArrayList<>(state.pending.values());
        state.pending.clear();
        if (events.size() > MAX_EVENTS_PER_TICK) {
            events.sort((a, b) -> Float.compare(b.db(), a.db()));
            events = events.subList(0, MAX_EVENTS_PER_TICK);
        }

        // Copie : un callback peut faire charger/décharger des entités
        List<Map.Entry<HearingListener, ListenerState>> listeners = new ArrayList<>(state.listeners.entrySet());
        for (NoiseEvent event : events) {
            for (Map.Entry<HearingListener, ListenerState> entry : listeners) {
                dispatch(event, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void dispatch(NoiseEvent event, HearingListener listener, ListenerState ls) {
        Entity entity = listener.listenerEntity();
        if (entity.isRemoved() || entity == event.source() || !entity.isAlive()) {
            return;
        }
        float interest = listener.getInterest(event);
        if (interest <= 0f) {
            return;
        }
        HearingRules rules = listener.hearingRules();
        float db = rules.modifyNoise(event);
        if (Float.isNaN(db)) {
            return;
        }
        PerceivedNoise noise = SoundPropagation.perceive(event, db, entity.getEyePos(), listener.hearingProfile(),
                ls.effectiveAmbient(), entity.getRandom(), rules);
        if (noise == null) {
            return;
        }
        if (noise.perceivedDb() >= rules.deafeningDb) {
            ls.deafen(noise.perceivedDb() - 20f);
        }
        listener.onNoiseHeard(noise, interest);
        SentienceDebug.onNoiseHeard(entity, noise);
    }

    public static void clearAll() {
        STATES.clear();
    }

    private static final class WorldState {
        final Map<HearingListener, ListenerState> listeners = new IdentityHashMap<>();
        final Map<Long, NoiseEvent> pending = new HashMap<>();
    }

    private static final class ListenerState {
        float ambient = HearingRules.DEFAULT.surfaceDb;
        float deafness = 0f;
        long nextAmbientRefresh = 0;

        void update(ServerWorld world, HearingListener listener, long now) {
            Entity entity = listener.listenerEntity();
            if (now >= nextAmbientRefresh) {
                ambient = AmbientNoise.levelAt(world, entity.getBlockPos(), listener.hearingRules());
                nextAmbientRefresh = now + AMBIENT_REFRESH_TICKS + (entity.getId() & 7); // étale la charge
            }
            if (deafness > 0f) {
                deafness = Math.max(0f, deafness - 0.5f);
            }
        }

        void deafen(float level) {
            deafness = Math.max(deafness, level);
        }

        float effectiveAmbient() {
            return deafness > 0f ? AmbientNoise.combine(ambient, deafness) : ambient;
        }
    }
}
