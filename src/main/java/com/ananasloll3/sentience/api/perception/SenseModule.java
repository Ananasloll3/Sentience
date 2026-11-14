package com.ananasloll3.sentience.api.perception;

import com.ananasloll3.sentience.api.agent.Agent;
import net.minecraft.server.world.ServerWorld;

/**
 * Un sens de l'agent. Il produit des {@link StimulusInput} via {@link PerceptionComponent#report}.
 * Implémenter cette interface pour créer un sens personnalisé.
 */
public interface SenseModule {

    SenseType type();

    default void onLoad(Agent agent, PerceptionComponent perception, ServerWorld world) {
    }

    default void onUnload(Agent agent, PerceptionComponent perception) {
    }

    /** Appelé à chaque réflexion de l'agent (voir {@link Agent#thinkInterval()}). */
    default void tick(Agent agent, PerceptionComponent perception, ServerWorld world, int deltaTicks) {
    }
}
