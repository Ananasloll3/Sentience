package com.ananasloll3.sentience.api.agent;

import net.minecraft.server.world.ServerWorld;

/**
 * Brique de comportement attachée à un {@link Agent} (perception, alerte, ou ton propre système).
 * Toutes les méthodes sont appelées sur le thread serveur.
 */
public interface AgentComponent {

    /** Appelé une fois, à la construction de l'agent. */
    default void onAttach(Agent agent) {
    }

    /** Le mob entre dans un monde serveur (spawn, chargement de chunk). */
    default void onLoad(Agent agent, ServerWorld world) {
    }

    /** Le mob quitte le monde (mort, déchargement). */
    default void onUnload(Agent agent) {
    }

    /**
     * Mise à jour de l'agent.
     *
     * @param deltaTicks ticks écoulés depuis le dernier appel : plus de 1 quand l'agent est loin
     *                   des joueurs et « pense » moins souvent (niveau de détail)
     */
    default void tick(Agent agent, ServerWorld world, int deltaTicks) {
    }
}
