package com.ananasloll3.sentience.api.perception;

import com.ananasloll3.sentience.api.agent.Agent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

/**
 * Douleur : être blessé révèle l'attaquant. Position quasi exacte au corps à corps,
 * plus floue pour un tir de loin (on sait d'où ça vient, pas exactement qui).
 * Alimenté par Sentience via l'événement de dégâts de Fabric.
 */
public final class DamageSense implements SenseModule {

    public static final float MELEE_SCORE = 40f;

    @Override
    public SenseType type() {
        return SenseType.DAMAGE;
    }

    /** Appelé par Sentience après des dégâts subis par l'agent. */
    public void onDamaged(Agent agent, PerceptionComponent perception, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        Entity direct = source.getSource();
        if (attacker != null && attacker != agent.mob()) {
            double distance = attacker.distanceTo(agent.mob());
            // Au-delà du corps à corps, on situe l'origine du tir avec une erreur croissante
            double error = distance < 4 ? 0.5 : Math.min(2.0 + distance * 0.15, 8.0);
            perception.report(new StimulusInput(SenseType.DAMAGE, attacker.getEntityPos(), error,
                    MELEE_SCORE, amount, direct != attacker ? "ranged" : "melee", attacker.getUuid()));
        } else if (direct != null) {
            // Projectile sans tireur connu : on remonte sa trajectoire
            Vec3d origin = direct.getEntityPos().subtract(direct.getVelocity().normalize().multiply(6));
            perception.report(new StimulusInput(SenseType.DAMAGE, origin, 5.0, MELEE_SCORE * 0.6f, amount, "ranged", null));
        }
    }
}
