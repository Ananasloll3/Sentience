package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.agent.AgentComms;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessComponent;
import com.ananasloll3.sentience.api.behavior.node.Action;
import com.ananasloll3.sentience.api.behavior.node.Check;
import com.ananasloll3.sentience.api.behavior.node.MapResult;
import com.ananasloll3.sentience.api.behavior.node.Parallel;
import com.ananasloll3.sentience.api.behavior.node.RandomSelector;
import com.ananasloll3.sentience.api.behavior.node.Repeat;
import com.ananasloll3.sentience.api.behavior.node.Selector;
import com.ananasloll3.sentience.api.behavior.node.Sequence;
import com.ananasloll3.sentience.api.behavior.task.Attack;
import com.ananasloll3.sentience.api.behavior.task.FleeFrom;
import com.ananasloll3.sentience.api.behavior.task.LookAt;
import com.ananasloll3.sentience.api.behavior.task.MoveTo;
import com.ananasloll3.sentience.api.behavior.task.Search;
import com.ananasloll3.sentience.api.behavior.task.Wait;
import com.ananasloll3.sentience.api.behavior.task.Wander;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.blackboard.BlackboardKeys;
import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.ananasloll3.sentience.api.query.EnvQueries;
import com.ananasloll3.sentience.api.query.EnvQuery;
import com.ananasloll3.sentience.api.query.QueryResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raccourcis pour écrire un Behavior Tree en Java. Chaque appel crée de nouveaux nœuds :
 * utilise-les dans la fabrique d'un {@link BehaviorTree}.
 */
public final class Bt {

    private static final Set<Identifier> WARNED = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<int[]> SUBTREE_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private Bt() {
    }

    // ------------------------------------------------------------------ composites

    public static BtNode sequence(BtNode... children) {
        return new Sequence(List.of(children));
    }

    public static BtNode selector(BtNode... children) {
        return new Selector(List.of(children));
    }

    public static BtNode randomSelector(BtNode... children) {
        return new RandomSelector(List.of(children));
    }

    /** @param requireAll réussit quand tous ont réussi (sinon : dès que l'un réussit) */
    public static BtNode parallel(boolean requireAll, BtNode... children) {
        return new Parallel(List.of(children), requireAll);
    }

    /** Insère l'arbre {@code id} (rechargé avec lui). */
    public static BtNode subtree(Identifier id) {
        int[] depth = SUBTREE_DEPTH.get();
        BehaviorTree tree = BehaviorTrees.get(id);
        if (tree == null || depth[0] > 16) {
            if (WARNED.add(id)) {
                Sentience.LOGGER.warn("Sous-arbre {} {}.", id, tree == null ? "introuvable" : "trop imbriqué (boucle ?)");
            }
            return fail().named("subtree " + id);
        }
        depth[0]++;
        try {
            return tree.instantiate();
        } finally {
            depth[0]--;
        }
    }

    // ------------------------------------------------------------------ décorateurs

    public static BtNode invert(BtNode child) {
        return new MapResult(child, MapResult.Mode.INVERT);
    }

    public static BtNode forceSuccess(BtNode child) {
        return new MapResult(child, MapResult.Mode.FORCE_SUCCESS);
    }

    public static BtNode forceFailure(BtNode child) {
        return new MapResult(child, MapResult.Mode.FORCE_FAILURE);
    }

    /** {@code count < 0} : sans fin. */
    public static BtNode repeat(int count, BtNode child) {
        return new Repeat(child, count);
    }

    // ------------------------------------------------------------------ feuilles génériques

    public static BtNode check(BtCondition condition) {
        return new Check(condition);
    }

    public static BtNode action(java.util.function.Function<BtContext, BtStatus> action) {
        return new Action(action);
    }

    /** Action instantanée qui réussit toujours. */
    public static BtNode run(java.util.function.Consumer<BtContext> action) {
        return new Action(ctx -> {
            action.accept(ctx);
            return BtStatus.SUCCESS;
        });
    }

    public static BtNode succeed() {
        return new Action(ctx -> BtStatus.SUCCESS).named("succeed");
    }

    public static BtNode fail() {
        return new Action(ctx -> BtStatus.FAILURE).named("fail");
    }

    // ------------------------------------------------------------------ tâches

    public static BtNode wait(int ticks) {
        return new Wait(ticks, 0).named("wait");
    }

    public static BtNode wait(int ticks, int deviation) {
        return new Wait(ticks, deviation).named("wait");
    }

    public static BtNode moveTo(BlackboardKey<?> target, double speed) {
        return moveTo(target, speed, 1.5, 0);
    }

    public static BtNode moveTo(BlackboardKey<?> target, double speed, double acceptance, int timeout) {
        return new MoveTo(target, speed, acceptance, timeout).named("move_to");
    }

    public static BtNode wander(double radius, double speed) {
        return new Wander(radius, speed).named("wander");
    }

    public static BtNode search(BlackboardKey<?> center, double radius, int ticks, double speed) {
        return new Search(center, radius, ticks, speed).named("search");
    }

    public static BtNode flee(BlackboardKey<?> threat, double distance, double speed) {
        return new FleeFrom(threat, distance, speed).named("flee");
    }

    public static BtNode lookAt(BlackboardKey<?> target, int ticks) {
        return new LookAt(target, ticks).named("look_at");
    }

    /** Poursuit et frappe au corps à corps l'entité de la clé (UUID, entité…). */
    public static BtNode attack(BlackboardKey<?> target, double speed, int cooldown) {
        return new Attack(target, speed, cooldown).named("attack");
    }

    /** Lance une requête d'environnement et écrit le meilleur point dans {@code result}. Échoue s'il n'y en a pas. */
    public static BtNode runQuery(Identifier query, BlackboardKey<?> result) {
        return new Action(ctx -> {
            EnvQuery q = EnvQueries.get(query);
            if (q == null) {
                if (WARNED.add(query)) {
                    Sentience.LOGGER.warn("Requête d'environnement {} introuvable.", query);
                }
                return BtStatus.FAILURE;
            }
            return writeQuery(ctx, q, result);
        }).named("query " + query.getPath());
    }

    public static BtNode runQuery(EnvQuery query, BlackboardKey<?> result) {
        return new Action(ctx -> writeQuery(ctx, query, result)).named("query");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BtStatus writeQuery(BtContext ctx, EnvQuery query, BlackboardKey<?> result) {
        QueryResult res = query.run(ctx.agent());
        Vec3d best = res.best();
        Object value = best == null ? null : adapt(best, result.type());
        ctx.blackboard().set((BlackboardKey) result, value);
        return BtStatus.of(best != null);
    }

    private static Object adapt(Vec3d pos, Class<?> type) {
        return type == BlockPos.class ? BlockPos.ofFloored(pos) : pos;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BtNode set(BlackboardKey<?> key, @Nullable Object value) {
        return run(ctx -> ctx.blackboard().set((BlackboardKey) key, value)).named("set " + key.id().getPath());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BtNode copy(BlackboardKey<?> from, BlackboardKey<?> to) {
        return run(ctx -> ctx.blackboard().set((BlackboardKey) to, ctx.blackboard().get(from))).named("copy");
    }

    public static BtNode clear(BlackboardKey<?> key) {
        return run(ctx -> ctx.blackboard().remove(key)).named("clear " + key.id().getPath());
    }

    public static BtNode playSound(SoundEvent sound, float volume, float pitch) {
        return run(ctx -> ctx.mob().playSound(sound, volume, pitch)).named("play_sound");
    }

    /** Émet un bruit entendu par les agents (alarme, cri…). */
    public static BtNode emitNoise(float db, NoiseType type) {
        return run(ctx -> HearingSystem.report(ctx.world(), ctx.mob().getEyePos(), db, type, ctx.mob())).named("emit_noise");
    }

    /** Partage le focus avec les alliés proches (du même type si {@code sameType}). */
    public static BtNode shareFocus(double range, boolean sameType) {
        return run(ctx -> AgentComms.shareFocus(ctx.agent(), range,
                sameType ? AgentComms.sameType(ctx.agent()) : other -> true)).named("share_focus");
    }

    /** Oublie le stimulus en cours (l'alerte redescend). */
    public static BtNode giveUp() {
        return run(ctx -> {
            AwarenessComponent awareness = ctx.agent().awareness();
            if (awareness != null && awareness.focus() != null) {
                awareness.giveUp(awareness.focus());
            }
        }).named("give_up");
    }

    /** Monte l'alerte au moins à {@code level}. */
    public static BtNode alert(Awareness level) {
        return run(ctx -> {
            AwarenessComponent awareness = ctx.agent().awareness();
            if (awareness != null) {
                awareness.escalate(level);
            }
        }).named("alert");
    }

    /** Donne l'entité de la clé comme cible vanilla ({@code setTarget}) ; échoue si elle n'existe pas. */
    public static BtNode setAttackTarget(BlackboardKey<?> target) {
        return action(ctx -> {
            LivingEntity living = BtTargets.living(ctx, target);
            ctx.mob().setTarget(living);
            return BtStatus.of(living != null);
        }).named("set_attack_target");
    }

    // ------------------------------------------------------------------ conditions

    public static BtCondition awarenessAtLeast(Awareness level) {
        return ctx -> ctx.agent().awarenessState().atLeast(level);
    }

    public static BtCondition awarenessIs(Awareness level) {
        return ctx -> ctx.agent().awarenessState() == level;
    }

    public static BtCondition isSet(BlackboardKey<?> key) {
        return ctx -> ctx.blackboard().has(key);
    }

    /** Une cible est vue avec certitude. */
    public static BtCondition seesTarget() {
        return ctx -> BtTargets.entity(ctx, BlackboardKeys.VISIBLE_TARGET) != null;
    }

    /** Vie restante sous une fraction (0..1) de la vie max. */
    public static BtCondition healthBelow(float fraction) {
        return ctx -> ctx.mob().getHealth() < ctx.mob().getMaxHealth() * fraction;
    }

    public static BtCondition distanceBelow(BlackboardKey<?> target, double distance) {
        return ctx -> {
            Vec3d pos = BtTargets.position(ctx, target);
            return pos != null && ctx.mob().getEntityPos().squaredDistanceTo(pos) < distance * distance;
        };
    }

    public static BtCondition distanceAbove(BlackboardKey<?> target, double distance) {
        return ctx -> {
            Vec3d pos = BtTargets.position(ctx, target);
            return pos != null && ctx.mob().getEntityPos().squaredDistanceTo(pos) > distance * distance;
        };
    }

    public static BtCondition isNight() {
        return ctx -> ctx.world().isNight();
    }

    public static BtCondition lightBelow(int level) {
        return ctx -> ctx.world().getLightLevel(ctx.mob().getBlockPos().up()) < level;
    }

    public static BtCondition chance(double probability) {
        return ctx -> ctx.random().nextDouble() < probability;
    }

    public static BtCondition inWater() {
        return ctx -> ctx.mob().isTouchingWater();
    }

    /** L'entité de la clé existe et est vivante. */
    public static BtCondition alive(BlackboardKey<?> key) {
        return ctx -> {
            Entity entity = BtTargets.entity(ctx, key);
            return entity != null;
        };
    }
}
