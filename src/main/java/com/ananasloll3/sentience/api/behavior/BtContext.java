package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.blackboard.Blackboard;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;

/**
 * Ce qu'un nœud reçoit à chaque tick : l'agent, le monde et le temps écoulé.
 *
 * @param deltaTicks ticks écoulés depuis le tick précédent de l'arbre (1 ou plus)
 */
public record BtContext(Agent agent, ServerWorld world, int deltaTicks) {

    public MobEntity mob() {
        return agent.mob();
    }

    public Blackboard blackboard() {
        return agent.blackboard();
    }

    public long time() {
        return world.getTime();
    }

    public Random random() {
        return agent.mob().getRandom();
    }
}
