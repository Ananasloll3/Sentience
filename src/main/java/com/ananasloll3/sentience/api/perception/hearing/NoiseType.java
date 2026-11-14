package com.ananasloll3.sentience.api.perception.hearing;

/**
 * Catégorie d'un bruit. L'intérêt par défaut sert à pondérer la priorité d'un stimulus :
 * une voix intéresse plus un prédateur qu'un mouton qui bêle.
 * Chaque {@link HearingListener} peut redéfinir son propre intérêt via {@link HearingListener#getInterest}.
 */
public enum NoiseType {
    VOICE(1.0f),
    FOOTSTEP(0.9f),
    COMBAT(1.0f),
    BLOCK(0.8f),
    MECHANISM(0.75f),
    PROJECTILE(0.8f),
    EXPLOSION(0.7f),
    ITEM(0.6f),
    CREATURE(0.35f),
    GENERIC(0.6f);

    private final float defaultInterest;

    NoiseType(float defaultInterest) {
        this.defaultInterest = defaultInterest;
    }

    public float defaultInterest() {
        return defaultInterest;
    }
}
