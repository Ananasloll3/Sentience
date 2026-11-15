package com.ananasloll3.sentience.api.behavior;

/** Résultat d'un nœud de Behavior Tree pour ce tick. */
public enum BtStatus {
    SUCCESS,
    FAILURE,
    /** Le nœud n'a pas fini : il sera de nouveau exécuté au prochain tick. */
    RUNNING;

    public static BtStatus of(boolean success) {
        return success ? SUCCESS : FAILURE;
    }
}
