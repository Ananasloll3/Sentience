package com.ananasloll3.sentience.api.awareness;

/** Niveau d'alerte d'une créature, du plus calme au plus agressif. */
public enum Awareness {
    /** Rien d'intéressant : erre normalement. */
    IDLE(0),
    /** A entendu quelque chose : s'arrête et écoute dans cette direction. */
    SUSPICIOUS(40),
    /** Va voir à l'endroit estimé, puis fouille autour. */
    INVESTIGATE(80),
    /** Sait qu'il y a quelqu'un : court vers le dernier bruit. */
    HUNT(140);

    /** Durée minimale (ticks) avant de redescendre d'un cran. */
    private final int lingerTicks;

    Awareness(int lingerTicks) {
        this.lingerTicks = lingerTicks;
    }

    public int lingerTicks() {
        return lingerTicks;
    }

    public boolean atLeast(Awareness other) {
        return ordinal() >= other.ordinal();
    }
}
