package com.ananasloll3.sentience.api.perception.hearing;

import net.minecraft.util.math.Vec3d;

/**
 * Ce qu'un auditeur a réellement perçu d'un {@link NoiseEvent}.
 * L'IA ne doit utiliser que {@link #estimatedPos()} : la vraie position reste dans {@code event}
 * uniquement pour le debug.
 *
 * @param event        bruit d'origine
 * @param estimatedPos position estimée (floue) de la source
 * @param perceivedDb  intensité reçue à l'oreille après atténuation et occlusion
 * @param margin       dB au-dessus du seuil d'audition (≈ rapport signal/bruit)
 * @param distance     distance réelle oreille → source
 * @param occlusionDb  atténuation due aux blocs traversés
 * @param errorRadius  incertitude de localisation, en blocs
 */
public record PerceivedNoise(NoiseEvent event, Vec3d estimatedPos, float perceivedDb, float margin,
                             double distance, float occlusionDb, double errorRadius) {
}
