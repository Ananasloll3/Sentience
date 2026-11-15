package com.ananasloll3.sentience.api.agent;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
 *
 * <p><b>Plusieurs mods peuvent viser le même type</b> : leurs modèles sont appliqués au même builder,
 * par priorité croissante (puis dans l'ordre d'enregistrement). Les sens et composants s'additionnent ;
 * un sens ou composant de même classe, les règles, l'alerte, les comportements par défaut et le
 * Behavior Tree sont remplacés par le modèle le plus prioritaire.</p>
 */
public final class AgentTemplates {

    private record Entry(int priority, int order, Consumer<AgentBuilder> template) {
    }

    private static final Map<EntityType<?>, List<Entry>> TEMPLATES = new ConcurrentHashMap<>();
    private static int registrations;

    private AgentTemplates() {
    }

    public static <T extends MobEntity> void register(EntityType<T> type, Consumer<AgentBuilder> template) {
        register(type, 0, template);
    }

    /** @param priority les modèles plus prioritaires sont appliqués après et l'emportent en cas de conflit */
    public static synchronized <T extends MobEntity> void register(EntityType<T> type, int priority,
                                                                   Consumer<AgentBuilder> template) {
        List<Entry> list = new ArrayList<>(TEMPLATES.getOrDefault(type, List.of()));
        list.add(new Entry(priority, registrations++, template));
        list.sort(Comparator.comparingInt(Entry::priority).thenComparingInt(Entry::order));
        TEMPLATES.put(type, List.copyOf(list));
    }

    public static boolean has(EntityType<?> type) {
        return TEMPLATES.containsKey(type);
    }

    /** Applique les modèles si le mob n'a pas déjà un agent. Appelé par Sentience. */
    public static void apply(MobEntity mob) {
        if (Agent.of(mob) != null) {
            return;
        }
        List<Entry> templates = TEMPLATES.get(mob.getType());
        if (templates != null) {
            AgentBuilder builder = Agent.builder(mob);
            for (Entry entry : templates) {
                entry.template().accept(builder);
            }
            builder.build();
        }
    }
}
