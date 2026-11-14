package com.ananasloll3.sentience.mixin;

import com.ananasloll3.sentience.impl.sound.WorldSoundCapture;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entity.playSound appelle world.playSound(null, …) : la source est perdue.
 * On la mémorise le temps de l'appel pour savoir quelle entité a fait le bruit.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "playSound", at = @At("HEAD"))
    private void sentience$pushEmitter(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        WorldSoundCapture.pushEmitter((Entity) (Object) this);
    }

    @Inject(method = "playSound", at = @At("RETURN"))
    private void sentience$popEmitter(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        WorldSoundCapture.popEmitter((Entity) (Object) this);
    }
}
