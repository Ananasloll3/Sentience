package com.ananasloll3.sentience.api.behavior;

/**
 * Service : tâche de fond exécutée à intervalle régulier tant que la branche qui le porte est active
 * (mettre à jour une cible, relancer une requête EQS, prévenir les alliés…), comme dans Unreal.
 * Un service doit être sans état : la même instance est partagée par tous les agents.
 */
@FunctionalInterface
public interface BtService {

    void tick(BtContext ctx);
}
