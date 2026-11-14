package com.ananasloll3.sentience.api.perception.hearing;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseModule;
import com.ananasloll3.sentience.api.perception.SenseType;
import com.ananasloll3.sentience.api.perception.StimulusInput;
import com.ananasloll3.sentience.api.rules.HearingRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleBiFunction;

/**
 * Ouïe : propagation physique du son (atténuation, occlusion par les blocs, masquage par le bruit
 * de fond) et localisation imparfaite de la source. Entend aussi la voix via Simple Voice Chat.
 */
public final class HearingSense implements SenseModule, HearingListener {

    /** Intérêt par défaut : celui du type de bruit ; on ignore les spectateurs et ses congénères. */
    public static final ToDoubleBiFunction<Agent, NoiseEvent> DEFAULT_INTEREST = (agent, event) -> {
        Entity source = event.source();
        if (source instanceof PlayerEntity player && player.isSpectator()) {
            return 0.0;
        }
        if (source != null && source.getType() == agent.mob().getType()) {
            return 0.0;
        }
        return event.type().defaultInterest();
    };

    private final HearingProfile profile;
    private final ToDoubleBiFunction<Agent, NoiseEvent> interest;
    @Nullable
    private Agent agent;
    @Nullable
    private PerceptionComponent perception;

    public HearingSense(HearingProfile profile, ToDoubleBiFunction<Agent, NoiseEvent> interest) {
        this.profile = profile;
        this.interest = interest;
    }

    @Override
    public SenseType type() {
        return SenseType.HEARING;
    }

    @Override
    public void onLoad(Agent agent, PerceptionComponent perception, ServerWorld world) {
        this.agent = agent;
        this.perception = perception;
        HearingSystem.register(world, this);
    }

    @Override
    public void onUnload(Agent agent, PerceptionComponent perception) {
        HearingSystem.unregister(this);
    }

    // ------------------------------------------------------------------ HearingListener

    @Override
    public Entity listenerEntity() {
        return agent != null ? agent.mob() : null;
    }

    @Override
    public HearingProfile hearingProfile() {
        return profile;
    }

    @Override
    public HearingRules hearingRules() {
        return agent != null ? agent.rules().hearing() : HearingRules.DEFAULT;
    }

    @Override
    public float getInterest(NoiseEvent event) {
        if (agent == null || event.source() == agent.mob()) {
            return 0f;
        }
        return (float) interest.applyAsDouble(agent, event);
    }

    @Override
    public void onNoiseHeard(PerceivedNoise noise, float interest) {
        if (perception == null) {
            return;
        }
        Entity source = noise.event().source();
        perception.report(new StimulusInput(SenseType.HEARING, noise.estimatedPos(), noise.errorRadius(),
                noise.margin() * interest, noise.perceivedDb(), noise.event().type().name().toLowerCase(),
                source != null ? source.getUuid() : null));
    }
}
