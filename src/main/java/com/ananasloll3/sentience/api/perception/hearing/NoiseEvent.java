package com.ananasloll3.sentience.api.perception.hearing;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Un bruit émis dans le monde, avant toute propagation.
 *
 * @param world   monde serveur
 * @param pos     position réelle de la source
 * @param db      intensité à la source (≈ dB SPL à 1 bloc). Repères : chuchotement 35, pas 42, voix 60, cri 80, explosion 125
 * @param type    catégorie du bruit
 * @param source  entité à l'origine du bruit, si connue
 * @param tick    temps du monde au moment de l'émission
 * @param soundId son Minecraft d'origine (ex. {@code minecraft:block.wool.step}), {@code null} pour un bruit custom
 */
public record NoiseEvent(ServerWorld world, Vec3d pos, float db, NoiseType type, @Nullable Entity source, long tick,
                         @Nullable Identifier soundId) {
}
