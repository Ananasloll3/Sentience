package com.ananasloll3.sentience.api.agent;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Donne un agent aux mobs d'un type existant (vanilla ou d'un autre mod), sans toucher à leur code.
 * Le modèle est appliqué à chaque apparition ou chargement du mob.
 *
 * <pre>{@code
 * AgentTemplates.register(EntityType.ZOMBIE, b -> b
 *     .hearing(HearingProfile.DEFAULT)
 *     .sight(SightConfig.humanoid())
 *     .defaultGoals(3, 1.0, 1.2));
 * }</pre>
 */
public final class AgentTemplates {

    private static final Map<EntityType<?>, Consumer<AgentBuilder>> TEMPLATES = new HashMap<>();

    private AgentTemplates() {
    }

    public static <T extends MobEntity> void register(EntityType<T> type, Consumer<AgentBuilder> template) {
        TEMPLATES.put(type, template);
    }

    public static boolean has(EntityType<?> type) {
        return TEMPLATES.containsKey(type);
    }

    /** Applique le modèle si le mob n'a pas déjà un agent. Appelé par Sentience. */
    public static void apply(MobEntity mob) {
        if (Agent.of(mob) != null) {
            return;
        }
        Consumer<AgentBuilder> template = TEMPLATES.get(mob.getType());
        if (template != null) {
            AgentBuilder builder = Agent.builder(mob);
            template.accept(builder);
            builder.build();
        }
    }
}
