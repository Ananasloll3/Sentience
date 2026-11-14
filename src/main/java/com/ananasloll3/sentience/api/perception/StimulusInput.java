package com.ananasloll3.sentience.api.perception;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Une perception brute, avant fusion dans la mémoire.
 *
 * @param sense       sens qui a perçu
 * @param position    position estimée de la source
 * @param errorRadius incertitude (blocs)
 * @param score       importance pour l'IA (marge de perception × intérêt)
 * @param intensity   mesure physique (dB, visibilité…) pour le debug et le gameplay
 * @param kind        détail libre (« footstep », « seen »…)
 * @param sourceId    entité identifiée, si connue
 */
public record StimulusInput(SenseType sense, Vec3d position, double errorRadius, float score, float intensity,
                            String kind, @Nullable UUID sourceId) {

    public static StimulusInput exact(SenseType sense, Entity source, float score, String kind) {
        return new StimulusInput(sense, source.getEntityPos(), 0.5, score, score, kind, source.getUuid());
    }
}
