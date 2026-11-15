package com.ananasloll3.sentience.api.behavior;

import java.util.Locale;

/**
 * Quand un décorateur {@code condition} interrompt l'exécution (les « observer aborts » d'Unreal).
 */
public enum BtAbort {
    /** Condition vérifiée seulement à l'entrée de la branche. */
    NONE,
    /** La branche en cours est interrompue dès que la condition devient fausse. */
    SELF,
    /** Une branche moins prioritaire en cours est interrompue dès que la condition devient vraie. */
    LOWER,
    /** SELF et LOWER. */
    BOTH;

    public boolean self() {
        return this == SELF || this == BOTH;
    }

    public boolean lower() {
        return this == LOWER || this == BOTH;
    }

    public static BtAbort parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "none" -> NONE;
            case "self" -> SELF;
            case "lower", "lower_priority" -> LOWER;
            case "both" -> BOTH;
            default -> throw new IllegalArgumentException("abort inconnu : " + value + " (none, self, lower, both)");
        };
    }
}
