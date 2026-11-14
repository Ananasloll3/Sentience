package com.ananasloll3.sentience.impl.sound;

import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

/** Traduit les sons capturés par le mixin en bruits pour le {@link HearingSystem}. */
public final class WorldSoundCapture {

    /** Entité en train de jouer un son via Entity.playSound (thread serveur uniquement). */
    private static final java.util.ArrayDeque<Entity> EMITTERS = new java.util.ArrayDeque<>();

    private WorldSoundCapture() {
    }

    public static void pushEmitter(Entity entity) {
        if (entity.getEntityWorld() instanceof ServerWorld) {
            EMITTERS.push(entity);
        }
    }

    public static void popEmitter(Entity entity) {
        if (entity.getEntityWorld() instanceof ServerWorld && EMITTERS.peek() == entity) {
            EMITTERS.pop();
        }
    }

    /** Sécurité si un playSound a levé une exception entre push et pop. */
    public static void clearEmitters() {
        EMITTERS.clear();
    }

    public static void onSound(ServerWorld world, Vec3d pos, RegistryEntry<SoundEvent> sound, SoundCategory category,
                               float volume, @Nullable Entity source) {
        Entity emitter = EMITTERS.peek();
        if (source == null && emitter != null && emitter.getEntityWorld() == world) {
            source = emitter;
        }
        SoundLoudness.Loudness loudness = SoundLoudness.classify(sound.value().id(), category, volume, source);
        if (loudness != null) {
            HearingSystem.report(world, pos, loudness.db(), loudness.type(), source, sound.value().id());
        }
    }

    public static void onWorldEvent(ServerWorld world, @Nullable Entity source, int eventId, BlockPos pos, int data) {
        float db;
        NoiseType type;
        if (eventId == WorldEvents.BLOCK_BROKEN) {
            db = 62f;
            type = NoiseType.BLOCK;
        } else if (eventId == WorldEvents.ANVIL_USED || eventId == WorldEvents.ANVIL_LANDS) {
            db = 85f;
            type = NoiseType.BLOCK;
        } else if (eventId >= 1000 && eventId < 1100) {
            db = 55f; // portes zombifiées, distributeurs, feu éteint, etc.
            type = NoiseType.MECHANISM;
        } else {
            return;
        }
        HearingSystem.report(world, Vec3d.ofCenter(pos), db, type, source);
    }

    public static void onGameEvent(ServerWorld world, RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter) {
        if (event.matches(GameEvent.EXPLODE)) {
            HearingSystem.report(world, pos, 125f, NoiseType.EXPLOSION, emitter.sourceEntity());
        }
    }
}
