package com.ananasloll3.sentience.api.awareness;

/**
 * Seuils de réaction : à quel score de stimulus l'agent devient méfiant, va voir, ou chasse.
 * Repères de score : un pas à 8 blocs ≈ 6, une voix à 10 blocs ≈ 18, une cible vue = 30, un coup = 40.
 */
public record AwarenessProfile(float suspiciousScore, float investigateScore, float huntScore) {

    public static final AwarenessProfile DEFAULT = new AwarenessProfile(0.5f, 6f, 18f);
    /** Réagit au moindre bruit et chasse vite. */
    public static final AwarenessProfile PARANOID = new AwarenessProfile(0.3f, 4f, 13f);
    /** Il en faut beaucoup pour le faire bouger. */
    public static final AwarenessProfile CALM = new AwarenessProfile(2f, 10f, 25f);
}
