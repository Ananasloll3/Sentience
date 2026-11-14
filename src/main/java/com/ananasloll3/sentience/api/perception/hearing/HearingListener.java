package com.ananasloll3.sentience.api.perception.hearing;

import com.ananasloll3.sentience.api.rules.HearingRules;
import net.minecraft.entity.Entity;

/**
 * Tout ce qui écoute les bruits du monde. Les agents l'utilisent via {@link HearingSense} ;
 * une entité sans agent peut aussi l'implémenter et s'enregistrer avec {@link HearingSystem#register}.
 */
public interface HearingListener {

    /** Entité dont la position des oreilles est utilisée. */
    Entity listenerEntity();

    HearingProfile hearingProfile();

    /** Règles physiques appliquées par cet auditeur. */
    default HearingRules hearingRules() {
        return HearingRules.DEFAULT;
    }

    /** Intérêt porté à un bruit : 0 = ignoré (aucun calcul), 1 = normal. */
    default float getInterest(NoiseEvent event) {
        if (event.source() == listenerEntity()) {
            return 0f;
        }
        return event.type().defaultInterest();
    }

    /** Appelé sur le thread serveur pour chaque bruit effectivement entendu. */
    void onNoiseHeard(PerceivedNoise noise, float interest);
}
