package com.ananasloll3.sentience.api.query;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.behavior.BtTargets;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.blackboard.BlackboardKeys;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Position de référence d'une requête : l'agent lui-même, sa menace, son focus ou une clé du Blackboard. */
@FunctionalInterface
public interface QueryTarget {

    /** Position aux pieds de la cible, ou {@code null} si elle est inconnue. */
    @Nullable
    Vec3d resolve(Agent agent, ServerWorld world);

    /** Position des yeux (pour les tests de visibilité). */
    @Nullable
    default Vec3d resolveEye(Agent agent, ServerWorld world) {
        Vec3d pos = resolve(agent, world);
        return pos == null ? null : pos.add(0, 1.6, 0);
    }

    /** L'agent qui lance la requête. */
    QueryTarget QUERIER = new QueryTarget() {
        @Override
        public Vec3d resolve(Agent agent, ServerWorld world) {
            return agent.mob().getEntityPos();
        }

        @Override
        public Vec3d resolveEye(Agent agent, ServerWorld world) {
            return agent.mob().getEyePos();
        }
    };

    /** Position estimée du focus (ce que l'agent croit savoir). */
    QueryTarget FOCUS = key(BlackboardKeys.FOCUS_POSITION);

    /** La menace : la cible vue avec certitude si elle existe, sinon la position du focus. */
    QueryTarget THREAT = new QueryTarget() {
        @Override
        public Vec3d resolve(Agent agent, ServerWorld world) {
            Entity seen = BtTargets.entity(world, agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET));
            return seen != null ? seen.getEntityPos() : agent.blackboard().get(BlackboardKeys.FOCUS_POSITION);
        }

        @Override
        public Vec3d resolveEye(Agent agent, ServerWorld world) {
            Entity seen = BtTargets.entity(world, agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET));
            if (seen != null) {
                return seen.getEyePos();
            }
            Vec3d focus = agent.blackboard().get(BlackboardKeys.FOCUS_POSITION);
            return focus == null ? null : focus.add(0, 1.6, 0);
        }
    };

    /** Valeur d'une clé du Blackboard (position, bloc, entité…). */
    static QueryTarget key(BlackboardKey<?> key) {
        return new QueryTarget() {
            @Override
            public Vec3d resolve(Agent agent, ServerWorld world) {
                return BtTargets.position(world, agent.blackboard().get(key));
            }

            @Override
            public Vec3d resolveEye(Agent agent, ServerWorld world) {
                Entity entity = BtTargets.entity(world, agent.blackboard().get(key));
                if (entity != null) {
                    return entity.getEyePos();
                }
                Vec3d pos = resolve(agent, world);
                return pos == null ? null : pos.add(0, 1.6, 0);
            }
        };
    }

    /** {@code querier}, {@code threat}, {@code focus}, ou l'identifiant d'une clé du Blackboard. */
    static QueryTarget parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "querier", "self" -> QUERIER;
            case "threat" -> THREAT;
            case "focus" -> FOCUS;
            default -> key(BlackboardKey.byId(Identifier.of(value)));
        };
    }
}
