package com.ananasloll3.sentience.api.perception.sight;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

/**
 * Réglages de la vue.
 *
 * @param range             portée maximale (blocs)
 * @param fovDegrees        champ de vision net (angle total)
 * @param peripheralDegrees champ périphérique (angle total, ≥ fov) : on y voit mal
 * @param detectTicks       temps pour repérer une cible parfaitement visible (jauge de détection)
 * @param darkVision        0 = ne voit rien dans le noir, 1 = vision nocturne parfaite
 * @param seesInvisible     ignore l'effet d'invisibilité
 * @param checkInterval     ticks entre deux vérifications (coût des raycasts)
 * @param targets           ce que l'agent cherche à voir
 */
public record SightConfig(double range, float fovDegrees, float peripheralDegrees, int detectTicks,
                          float darkVision, boolean seesInvisible, int checkInterval,
                          Predicate<LivingEntity> targets) {

    /** Joueurs vivants, hors spectateurs. */
    public static final Predicate<LivingEntity> PLAYERS = e -> e instanceof PlayerEntity p && !p.isSpectator();

    /** Vue humaine : 110° nets, 170° périphériques, 32 blocs, gênée par le noir. */
    public static SightConfig humanoid() {
        return new SightConfig(32, 110f, 170f, 20, 0.15f, false, 4, PLAYERS);
    }

    /** Prédateur nocturne : voit bien dans le noir, repère vite. */
    public static SightConfig nocturnal() {
        return new SightConfig(40, 100f, 160f, 12, 0.8f, false, 3, PLAYERS);
    }

    public SightConfig withRange(double value) {
        return new SightConfig(value, fovDegrees, peripheralDegrees, detectTicks, darkVision, seesInvisible, checkInterval, targets);
    }

    public SightConfig withFov(float fov, float peripheral) {
        return new SightConfig(range, fov, Math.max(fov, peripheral), detectTicks, darkVision, seesInvisible, checkInterval, targets);
    }

    public SightConfig withDetectTicks(int value) {
        return new SightConfig(range, fovDegrees, peripheralDegrees, value, darkVision, seesInvisible, checkInterval, targets);
    }

    public SightConfig withDarkVision(float value) {
        return new SightConfig(range, fovDegrees, peripheralDegrees, detectTicks, value, seesInvisible, checkInterval, targets);
    }

    public SightConfig withSeesInvisible(boolean value) {
        return new SightConfig(range, fovDegrees, peripheralDegrees, detectTicks, darkVision, value, checkInterval, targets);
    }

    public SightConfig withTargets(Predicate<LivingEntity> value) {
        return new SightConfig(range, fovDegrees, peripheralDegrees, detectTicks, darkVision, seesInvisible, checkInterval, value);
    }
}
