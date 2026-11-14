package com.ananasloll3.sentience.api.agent;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.awareness.AwarenessComponent;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.goal.InvestigateGoal;
import com.ananasloll3.sentience.api.goal.ListenGoal;
import com.ananasloll3.sentience.api.perception.DamageSense;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseModule;
import com.ananasloll3.sentience.api.perception.TouchSense;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.api.perception.hearing.HearingSense;
import com.ananasloll3.sentience.api.perception.hearing.NoiseEvent;
import com.ananasloll3.sentience.api.perception.scent.ScentSense;
import com.ananasloll3.sentience.api.perception.sight.SightConfig;
import com.ananasloll3.sentience.api.perception.sight.SightSense;
import com.ananasloll3.sentience.api.rules.SentienceRules;
import com.ananasloll3.sentience.mixin.MobEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

/** Assemble un {@link Agent}. Obtenu avec {@link Agent#builder(MobEntity)}. */
public final class AgentBuilder {

    private final MobEntity mob;
    private SentienceRules rules = SentienceRules.DEFAULT;
    private final List<SenseModule> senses = new ArrayList<>();
    private final List<AgentComponent> components = new ArrayList<>();
    private AwarenessProfile awareness;
    private boolean withoutAwareness;
    private int memoryCapacity = 8;
    private int memoryTicks = 600;

    private int goalPriority = -1;
    private double walkSpeed = 1.0;
    private double runSpeed = 1.3;
    private int searchTicks = 240;

    AgentBuilder(MobEntity mob) {
        this.mob = mob;
    }

    // ------------------------------------------------------------------ règles

    /**
     * Règles de perception de ton mod (pluie, matériaux, camouflage…). Elles ne s'appliquent
     * qu'aux agents qui les utilisent : chaque mod garde les siennes.
     */
    public AgentBuilder rules(SentienceRules rules) {
        this.rules = rules;
        return this;
    }

    // ------------------------------------------------------------------ sens

    public AgentBuilder hearing(HearingProfile profile) {
        return hearing(profile, HearingSense.DEFAULT_INTEREST);
    }

    /** @param interest intérêt (0 = ignoré) porté à chaque bruit, pour filtrer ou prioriser */
    public AgentBuilder hearing(HearingProfile profile, ToDoubleBiFunction<Agent, NoiseEvent> interest) {
        senses.add(new HearingSense(profile, interest));
        return this;
    }

    public AgentBuilder sight(SightConfig config) {
        senses.add(new SightSense(config));
        return this;
    }

    public AgentBuilder damage() {
        senses.add(new DamageSense());
        return this;
    }

    public AgentBuilder touch() {
        return touch(SightConfig.PLAYERS);
    }

    public AgentBuilder touch(Predicate<LivingEntity> targets) {
        senses.add(new TouchSense(targets));
        return this;
    }

    /** Odorat : suit les pistes laissées par les joueurs. */
    public AgentBuilder scent(ScentSense sense) {
        senses.add(sense);
        return this;
    }

    /** Sens personnalisé (écholocation, détection de chaleur…). */
    public AgentBuilder sense(SenseModule sense) {
        senses.add(sense);
        return this;
    }

    /** Taille et durée de la mémoire perceptive (8 souvenirs, 30 s par défaut). */
    public AgentBuilder memory(int capacity, int ticks) {
        this.memoryCapacity = capacity;
        this.memoryTicks = ticks;
        return this;
    }

    // ------------------------------------------------------------------ décision

    /** Seuils d'alerte. Par défaut {@link AwarenessProfile#DEFAULT} dès qu'il y a un sens. */
    public AgentBuilder awareness(AwarenessProfile profile) {
        this.awareness = profile;
        this.withoutAwareness = false;
        return this;
    }

    /** Pas de niveau d'alerte : tu exploites toi-même la mémoire perceptive. */
    public AgentBuilder noAwareness() {
        this.withoutAwareness = true;
        return this;
    }

    /**
     * Ajoute les comportements prêts à l'emploi : aller voir / chasser ({@link InvestigateGoal})
     * puis écouter ({@link ListenGoal}, priorité + 1).
     */
    public AgentBuilder defaultGoals(int priority, double walkSpeed, double runSpeed) {
        this.goalPriority = priority;
        this.walkSpeed = walkSpeed;
        this.runSpeed = runSpeed;
        return this;
    }

    public AgentBuilder searchDuration(int ticks) {
        this.searchTicks = ticks;
        return this;
    }

    /** Composant personnalisé, exécuté après la perception et l'alerte. */
    public AgentBuilder component(AgentComponent component) {
        components.add(component);
        return this;
    }

    // ------------------------------------------------------------------ construction

    /** Construit l'agent et l'attache au mob (remplace l'éventuel agent précédent). */
    public Agent build() {
        List<AgentComponent> all = new ArrayList<>();
        if (!senses.isEmpty()) {
            all.add(new PerceptionComponent(senses, memoryCapacity, memoryTicks));
            if (!withoutAwareness) {
                all.add(new AwarenessComponent(awareness != null ? awareness : AwarenessProfile.DEFAULT));
            }
        } else if (awareness != null) {
            Sentience.LOGGER.warn("Agent de {} : niveau d'alerte sans aucun sens, il restera IDLE.", mob.getType());
            all.add(new AwarenessComponent(awareness));
        }
        all.addAll(components);

        Agent previous = Agent.of(mob);
        if (previous != null) {
            previous.unload();
        }

        Agent agent = new Agent(mob, rules, all);
        ((AgentHolder) mob).sentience$setAgent(agent);
        agent.attach();

        if (goalPriority >= 0) {
            var goals = ((MobEntityAccessor) mob).sentience$getGoalSelector();
            goals.add(goalPriority, new InvestigateGoal(agent, walkSpeed, runSpeed, searchTicks));
            goals.add(goalPriority + 1, new ListenGoal(agent));
        }

        // Construit après l'apparition (ex. : modèle appliqué à un mob déjà chargé)
        if (mob.getEntityWorld() instanceof ServerWorld world && world.getEntity(mob.getUuid()) == mob) {
            agent.load(world);
        }
        return agent;
    }
}
