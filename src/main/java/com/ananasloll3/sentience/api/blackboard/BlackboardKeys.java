package com.ananasloll3.sentience.api.blackboard;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/** Clés standard écrites par les composants de Sentience. */
public final class BlackboardKeys {

    /** Niveau d'alerte courant (écrit par l'AwarenessComponent). */
    public static final BlackboardKey<Awareness> AWARENESS = key("awareness", Awareness.class);
    /** Stimulus le plus prioritaire en mémoire. */
    public static final BlackboardKey<Stimulus> FOCUS = key("focus", Stimulus.class);
    /** Position estimée du focus (la meilleure supposition de l'agent). */
    public static final BlackboardKey<Vec3d> FOCUS_POSITION = key("focus_position", Vec3d.class);
    /** Entité à l'origine du focus, si l'agent l'a identifiée. */
    public static final BlackboardKey<UUID> FOCUS_SOURCE = key("focus_source", UUID.class);
    /** Entité actuellement vue avec certitude par la vue. */
    public static final BlackboardKey<UUID> VISIBLE_TARGET = key("visible_target", UUID.class);

    /** Résultat par défaut des requêtes d'environnement (EQS) lancées par un Behavior Tree. */
    public static final BlackboardKey<Vec3d> QUERY_RESULT = key("query_result", Vec3d.class);

    /** Force le chargement des clés standard (appelé par Sentience). */
    public static void bootstrap() {
    }

    private BlackboardKeys() {
    }

    private static <T> BlackboardKey<T> key(String path, Class<T> type) {
        return BlackboardKey.of(Sentience.id(path), type);
    }
}
