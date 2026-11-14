package com.ananasloll3.sentience.api.agent;

import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessComponent;
import com.ananasloll3.sentience.api.blackboard.Blackboard;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.Stimulus;
import com.ananasloll3.sentience.api.rules.SentienceRules;
import com.ananasloll3.sentience.impl.debug.SentienceDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Le « cerveau » attaché à un mob : un {@link Blackboard} et une liste de {@link AgentComponent}.
 * N'importe quel {@link MobEntity} peut en recevoir un, y compris les mobs vanilla.
 *
 * <pre>{@code
 * // dans le constructeur de ton mob
 * Agent.builder(this)
 *     .rules(MonMod.RULES)                 // règles de perception de ton mod (optionnel)
 *     .hearing(HearingProfile.DEFAULT)
 *     .sight(SightConfig.humanoid())
 *     .damage()
 *     .awareness(AwarenessProfile.DEFAULT)
 *     .defaultGoals(2, 0.9, 1.4)
 *     .build();
 * }</pre>
 */
public final class Agent {

    @FunctionalInterface
    public interface StimulusListener {
        void onStimulus(Agent agent, Stimulus stimulus);
    }

    @FunctionalInterface
    public interface AwarenessListener {
        void onAwarenessChanged(Agent agent, Awareness from, Awareness to);
    }

    /** Distances (blocs) du niveau de détail et intervalle de réflexion correspondant (ticks). */
    private static final double[] LOD_DISTANCES = {32, 64, 128};
    private static final int[] LOD_INTERVALS = {1, 2, 5, 20};

    private final MobEntity mob;
    private final SentienceRules rules;
    private final Blackboard blackboard = new Blackboard();
    private final List<AgentComponent> components;
    @Nullable
    private final PerceptionComponent perception;
    @Nullable
    private final AwarenessComponent awareness;

    private final List<StimulusListener> stimulusListeners = new ArrayList<>();
    private final List<AwarenessListener> awarenessListeners = new ArrayList<>();

    private boolean loaded;
    private int thinkInterval = 1;
    private int ticksSinceThink;
    private final SentienceDebug.NameState debugName = new SentienceDebug.NameState();

    Agent(MobEntity mob, SentienceRules rules, List<AgentComponent> components) {
        this.mob = mob;
        this.rules = rules;
        this.components = List.copyOf(components);
        this.perception = find(PerceptionComponent.class);
        this.awareness = find(AwarenessComponent.class);
    }

    // ------------------------------------------------------------------ accès

    public static AgentBuilder builder(MobEntity mob) {
        return new AgentBuilder(mob);
    }

    /** L'agent d'une entité, ou {@code null} si elle n'en a pas. */
    @Nullable
    public static Agent of(@Nullable Entity entity) {
        return entity instanceof AgentHolder holder ? holder.sentience$getAgent() : null;
    }

    public MobEntity mob() {
        return mob;
    }

    /** Règles de perception de cet agent (celles de son mod, ou {@link SentienceRules#DEFAULT}). */
    public SentienceRules rules() {
        return rules;
    }

    public Blackboard blackboard() {
        return blackboard;
    }

    /** Composant de perception, ou {@code null} si l'agent n'a aucun sens. */
    @Nullable
    public PerceptionComponent perception() {
        return perception;
    }

    @Nullable
    public AwarenessComponent awareness() {
        return awareness;
    }

    /** Niveau d'alerte, IDLE si l'agent n'a pas d'AwarenessComponent. */
    public Awareness awarenessState() {
        return awareness == null ? Awareness.IDLE : awareness.state();
    }

    @Nullable
    public <T extends AgentComponent> T find(Class<T> type) {
        for (AgentComponent component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }
        return null;
    }

    public List<AgentComponent> components() {
        return Collections.unmodifiableList(components);
    }

    public boolean isLoaded() {
        return loaded;
    }

    /** Intervalle de réflexion actuel (1 = chaque tick). */
    public int thinkInterval() {
        return thinkInterval;
    }

    // ------------------------------------------------------------------ événements

    public void onStimulus(StimulusListener listener) {
        stimulusListeners.add(listener);
    }

    public void onAwarenessChanged(AwarenessListener listener) {
        awarenessListeners.add(listener);
    }

    public void fireStimulus(Stimulus stimulus) {
        // Un mob lointain qui pense lentement réagit quand même au tick suivant
        ticksSinceThink = Math.max(ticksSinceThink, thinkInterval - 1);
        for (StimulusListener listener : stimulusListeners) {
            listener.onStimulus(this, stimulus);
        }
    }

    public void fireAwarenessChanged(Awareness from, Awareness to) {
        for (AwarenessListener listener : awarenessListeners) {
            listener.onAwarenessChanged(this, from, to);
        }
    }

    // ------------------------------------------------------------------ cycle de vie (appelé par Sentience)

    void attach() {
        for (AgentComponent component : components) {
            component.onAttach(this);
        }
    }

    public void load(ServerWorld world) {
        if (loaded) {
            return;
        }
        loaded = true;
        SentienceDebug.clearStaleName(mob);
        for (AgentComponent component : components) {
            component.onLoad(this, world);
        }
    }

    public void unload() {
        if (!loaded) {
            return;
        }
        loaded = false;
        for (AgentComponent component : components) {
            component.onUnload(this);
        }
    }

    public void tick(ServerWorld world) {
        if (!loaded) {
            return;
        }
        // Le niveau de détail est recalculé toutes les secondes, décalé par mob pour étaler la charge
        if ((mob.age + mob.getId()) % 20 == 0) {
            thinkInterval = computeThinkInterval(world);
        }
        ticksSinceThink++;
        if (ticksSinceThink < thinkInterval) {
            return;
        }
        int delta = ticksSinceThink;
        ticksSinceThink = 0;

        for (AgentComponent component : components) {
            component.tick(this, world, delta);
        }
        SentienceDebug.updateAgentName(this, debugName);
    }

    private int computeThinkInterval(ServerWorld world) {
        double nearest = Double.MAX_VALUE;
        for (var player : world.getPlayers()) {
            if (!player.isSpectator()) {
                nearest = Math.min(nearest, player.squaredDistanceTo(mob));
            }
        }
        for (int i = 0; i < LOD_DISTANCES.length; i++) {
            if (nearest <= LOD_DISTANCES[i] * LOD_DISTANCES[i]) {
                return LOD_INTERVALS[i];
            }
        }
        return LOD_INTERVALS[LOD_INTERVALS.length - 1];
    }
}
