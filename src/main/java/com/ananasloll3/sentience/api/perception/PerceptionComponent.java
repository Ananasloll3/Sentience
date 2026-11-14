package com.ananasloll3.sentience.api.perception;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComponent;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** Regroupe les sens d'un agent et leur mémoire commune (l'équivalent de l'AI Perception d'Unreal). */
public final class PerceptionComponent implements AgentComponent {

    private final List<SenseModule> senses;
    private final StimulusMemory memory;
    private final int memoryTicks;
    @Nullable
    private Agent agent;

    public PerceptionComponent(List<SenseModule> senses, int memoryCapacity, int memoryTicks) {
        this.senses = List.copyOf(senses);
        this.memory = new StimulusMemory(memoryCapacity);
        this.memoryTicks = memoryTicks;
    }

    /** Signale une perception (appelé par les sens, ou par ton code pour injecter une info). */
    public Stimulus report(StimulusInput input) {
        long now = agent != null ? agent.mob().getEntityWorld().getTime() : 0L;
        Stimulus stimulus = memory.remember(input, now);
        if (agent != null) {
            agent.fireStimulus(stimulus);
        }
        return stimulus;
    }

    @Override
    public void onAttach(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void onLoad(Agent agent, ServerWorld world) {
        for (SenseModule sense : senses) {
            sense.onLoad(agent, this, world);
        }
    }

    @Override
    public void onUnload(Agent agent) {
        for (SenseModule sense : senses) {
            sense.onUnload(agent, this);
        }
    }

    @Override
    public void tick(Agent agent, ServerWorld world, int deltaTicks) {
        memory.tick(world.getTime(), memoryTicks);
        for (SenseModule sense : senses) {
            sense.tick(agent, this, world, deltaTicks);
        }
    }

    public StimulusMemory memory() {
        return memory;
    }

    @Nullable
    public <T extends SenseModule> T sense(Class<T> type) {
        for (SenseModule sense : senses) {
            if (type.isInstance(sense)) {
                return type.cast(sense);
            }
        }
        return null;
    }

    public List<SenseModule> senses() {
        return Collections.unmodifiableList(senses);
    }
}
