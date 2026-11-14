package com.ananasloll3.sentience.api.agent;

import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseType;
import com.ananasloll3.sentience.api.perception.Stimulus;
import com.ananasloll3.sentience.api.perception.StimulusInput;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.function.Predicate;

/**
 * Communication entre agents : un agent partage son focus avec ses alliés proches.
 * L'information arrive dégradée (« il est par là-bas ») : incertitude augmentée, score réduit.
 */
public final class AgentComms {

    private AgentComms() {
    }

    /**
     * Partage le focus de {@code from} avec les agents à portée qui passent le filtre.
     *
     * @return nombre d'alliés prévenus
     */
    public static int shareFocus(Agent from, double radius, Predicate<Agent> allies) {
        Stimulus focus = from.awareness() == null ? null : from.awareness().focus();
        if (focus == null || !(from.mob().getEntityWorld() instanceof ServerWorld world)) {
            return 0;
        }
        long now = world.getTime();
        StimulusInput message = new StimulusInput(SenseType.REPORT, focus.getPosition(),
                focus.getErrorRadius() + 3.0, focus.getScore(now) * 0.8f, focus.getPeakIntensity(),
                "report", focus.getSourceId());

        int count = 0;
        for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, from.mob().getBoundingBox().expand(radius),
                m -> m != from.mob())) {
            Agent ally = Agent.of(mob);
            PerceptionComponent perception = ally == null ? null : ally.perception();
            if (perception != null && allies.test(ally)) {
                perception.report(message);
                count++;
            }
        }
        return count;
    }

    /** Même type de mob = allié. */
    public static Predicate<Agent> sameType(Agent from) {
        return other -> other.mob().getType() == from.mob().getType();
    }
}
