package com.ananasloll3.sentience.mixin;

import com.ananasloll3.sentience.impl.sound.WorldSoundCapture;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Capture de tous les sons émis côté serveur. */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "playSound(Lnet/minecraft/entity/Entity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("HEAD"))
    private void sentience$onPlaySound(@Nullable Entity source, double x, double y, double z,
                                           RegistryEntry<SoundEvent> sound, SoundCategory category,
                                           float volume, float pitch, long seed, CallbackInfo ci) {
        WorldSoundCapture.onSound((ServerWorld) (Object) this, new Vec3d(x, y, z), sound, category, volume, source);
    }

    @Inject(method = "playSoundFromEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("HEAD"))
    private void sentience$onPlaySoundFromEntity(@Nullable Entity except, Entity entity,
                                                     RegistryEntry<SoundEvent> sound, SoundCategory category,
                                                     float volume, float pitch, long seed, CallbackInfo ci) {
        WorldSoundCapture.onSound((ServerWorld) (Object) this, entity.getEntityPos(), sound, category, volume, entity);
    }

    /** Casse de blocs, mécanismes… : joués côté client via un « world event », pas via playSound. */
    @Inject(method = "syncWorldEvent", at = @At("HEAD"))
    private void sentience$onWorldEvent(@Nullable Entity source, int eventId, BlockPos pos, int data, CallbackInfo ci) {
        WorldSoundCapture.onWorldEvent((ServerWorld) (Object) this, source, eventId, pos, data);
    }

    /** Les explosions envoient leur son dans un paquet dédié : on passe par le GameEvent. */
    @Inject(method = "emitGameEvent", at = @At("HEAD"))
    private void sentience$onGameEvent(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, CallbackInfo ci) {
        WorldSoundCapture.onGameEvent((ServerWorld) (Object) this, event, pos, emitter);
    }
}
