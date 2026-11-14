package com.ananasloll3.sentience.api.awareness;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComponent;
import com.ananasloll3.sentience.api.blackboard.Blackboard;
import com.ananasloll3.sentience.api.blackboard.BlackboardKeys;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

/**
 * Niveau d'alerte de l'agent, calculé à partir du stimulus le plus prioritaire de sa mémoire.
 * Monte immédiatement, redescend progressivement. Écrit {@link BlackboardKeys#AWARENESS},
 * {@link BlackboardKeys#FOCUS}, {@link BlackboardKeys#FOCUS_POSITION} et {@link BlackboardKeys#FOCUS_SOURCE}.
 */
public final class AwarenessComponent implements AgentComponent {

    private final AwarenessProfile profile;
    private Awareness state = Awareness.IDLE;
    private int linger;
    @Nullable
    private Stimulus focus;
    @Nullable
    private Agent agent;

    public AwarenessComponent(AwarenessProfile profile) {
        this.profile = profile;
    }

    @Override
    public void onAttach(Agent agent) {
        this.agent = agent;
        agent.blackboard().set(BlackboardKeys.AWARENESS, state);
    }

    @Override
    public void tick(Agent agent, ServerWorld world, int deltaTicks) {
        PerceptionComponent perception = agent.perception();
        long now = world.getTime();
        focus = perception == null ? null : perception.memory().best(now);

        float score = focus == null ? 0f : focus.getScore(now);
        Awareness desired = desiredState(score);

        if (desired.ordinal() >= state.ordinal()) {
            setState(desired);
            linger = state.lingerTicks();
        } else {
            linger -= deltaTicks;
            if (linger <= 0) {
                setState(Awareness.values()[state.ordinal() - 1]);
                linger = state.lingerTicks();
            }
        }
        writeBlackboard(agent.blackboard());
    }

    private Awareness desiredState(float score) {
        if (score >= profile.huntScore()) return Awareness.HUNT;
        if (score >= profile.investigateScore()) return Awareness.INVESTIGATE;
        if (score >= profile.suspiciousScore()) return Awareness.SUSPICIOUS;
        return Awareness.IDLE;
    }

    private void setState(Awareness next) {
        if (next == state) {
            return;
        }
        Awareness previous = state;
        state = next;
        if (agent != null) {
            agent.blackboard().set(BlackboardKeys.AWARENESS, next);
            agent.fireAwarenessChanged(previous, next);
        }
    }

    private void writeBlackboard(Blackboard bb) {
        bb.set(BlackboardKeys.FOCUS, focus);
        bb.set(BlackboardKeys.FOCUS_POSITION, focus == null ? null : focus.getPosition());
        bb.set(BlackboardKeys.FOCUS_SOURCE, focus == null ? null : focus.getSourceId());
    }

    /** Force un niveau minimal (ex. : alerte donnée par un allié). */
    public void escalate(Awareness to) {
        if (to.ordinal() > state.ordinal()) {
            setState(to);
            linger = to.lingerTicks();
        }
    }

    /** Fin d'une fouille sans résultat : l'agent oublie ce stimulus. */
    public void giveUp(Stimulus stimulus) {
        if (agent != null && agent.perception() != null) {
            agent.perception().memory().forget(stimulus);
        }
        if (focus == stimulus) {
            focus = null;
        }
        linger = Math.min(linger, 20);
    }

    public Awareness state() {
        return state;
    }

    @Nullable
    public Stimulus focus() {
        return focus;
    }

    public AwarenessProfile profile() {
        return profile;
    }
}
