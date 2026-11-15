package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.behavior.node.ConditionDecorator;
import com.ananasloll3.sentience.api.behavior.node.Cooldown;
import com.ananasloll3.sentience.api.behavior.node.MapResult;
import com.ananasloll3.sentience.api.behavior.node.Parallel;
import com.ananasloll3.sentience.api.behavior.node.RandomSelector;
import com.ananasloll3.sentience.api.behavior.node.Repeat;
import com.ananasloll3.sentience.api.behavior.node.Selector;
import com.ananasloll3.sentience.api.behavior.node.Sequence;
import com.ananasloll3.sentience.api.behavior.node.ServiceNode;
import com.ananasloll3.sentience.api.behavior.node.TimeLimit;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.ananasloll3.sentience.api.data.SentienceJson.*;

/**
 * Types de nœuds, conditions, services et décorateurs utilisables dans les Behavior Trees JSON.
 * Les mods peuvent ajouter les leurs :
 *
 * <pre>{@code
 * BtRegistry.registerNode(Identifier.of("monmod", "howl"), json -> () -> Bt.playSound(MonMod.HOWL, 2f, 1f));
 * BtRegistry.registerCondition(Identifier.of("monmod", "full_moon"), json -> ctx -> ctx.world().getMoonPhase() == 0);
 * }</pre>
 */
public final class BtRegistry {

    /** Lit un nœud et renvoie sa fabrique (une instance par agent). */
    @FunctionalInterface
    public interface NodeParser {
        Supplier<? extends BtNode> parse(JsonObject json);
    }

    @FunctionalInterface
    public interface ConditionParser {
        BtCondition parse(JsonObject json);
    }

    @FunctionalInterface
    public interface ServiceParser {
        BtService parse(JsonObject json);
    }

    /** Lit un décorateur et renvoie de quoi envelopper un nœud. */
    @FunctionalInterface
    public interface DecoratorParser {
        UnaryOperator<BtNode> parse(JsonObject json);
    }

    private static final Map<Identifier, NodeParser> NODES = new ConcurrentHashMap<>();
    private static final Map<Identifier, ConditionParser> CONDITIONS = new ConcurrentHashMap<>();
    private static final Map<Identifier, ServiceParser> SERVICES = new ConcurrentHashMap<>();
    private static final Map<Identifier, DecoratorParser> DECORATORS = new ConcurrentHashMap<>();

    private BtRegistry() {
    }

    public static void registerNode(Identifier id, NodeParser parser) {
        NODES.put(id, parser);
    }

    public static void registerCondition(Identifier id, ConditionParser parser) {
        CONDITIONS.put(id, parser);
    }

    public static void registerService(Identifier id, ServiceParser parser) {
        SERVICES.put(id, parser);
    }

    public static void registerDecorator(Identifier id, DecoratorParser parser) {
        DECORATORS.put(id, parser);
        // Un décorateur est aussi utilisable comme nœud à un enfant : {"type": "cooldown", "ticks": 40, "child": {...}}
        NODES.putIfAbsent(id, json -> {
            Supplier<? extends BtNode> child = parseNode(json.get("child"));
            UnaryOperator<BtNode> wrap = parser.parse(json);
            return () -> wrap.apply(child.get());
        });
    }

    // ------------------------------------------------------------------ lecture

    /** Lit un nœud, avec ses éventuels {@code decorators}, {@code services} et {@code name}. */
    public static Supplier<BtNode> parseNode(JsonElement element) {
        JsonObject json = object(element, "nœud");
        Identifier type = id(type(json));
        NodeParser parser = NODES.get(type);
        Supplier<? extends BtNode> base;
        if (parser != null) {
            base = parser.parse(json);
        } else if (CONDITIONS.containsKey(type)) {
            BtCondition condition = parseCondition(json);
            base = () -> Bt.check(condition).named(type.getPath());
        } else {
            throw new JsonParseException("type de nœud inconnu : " + type);
        }

        String name = json.has("name") ? json.get("name").getAsString() : null;
        Supplier<BtNode> node = name == null ? base::get : () -> base.get().named(name);

        List<ServiceNode.Entry> services = new ArrayList<>();
        for (JsonElement s : list(json, "services")) {
            JsonObject so = object(s, "service");
            services.add(new ServiceNode.Entry(parseService(so), integer(so, "interval", 20)));
        }
        if (!services.isEmpty()) {
            Supplier<BtNode> inner = node;
            node = () -> new ServiceNode(inner.get(), services);
        }

        List<JsonElement> decorators = list(json, "decorators");
        for (int i = decorators.size() - 1; i >= 0; i--) {
            UnaryOperator<BtNode> wrap = parseDecorator(object(decorators.get(i), "décorateur"));
            Supplier<BtNode> inner = node;
            node = () -> wrap.apply(inner.get());
        }
        return node;
    }

    public static BtCondition parseCondition(JsonElement element) {
        JsonObject json = object(element, "condition");
        Identifier type = id(type(json));
        ConditionParser parser = CONDITIONS.get(type);
        if (parser == null) {
            throw new JsonParseException("condition inconnue : " + type);
        }
        BtCondition condition = parser.parse(json);
        return bool(json, "negate", false) ? condition.negate() : condition;
    }

    public static BtService parseService(JsonObject json) {
        Identifier type = id(type(json));
        ServiceParser parser = SERVICES.get(type);
        if (parser == null) {
            throw new JsonParseException("service inconnu : " + type);
        }
        return parser.parse(json);
    }

    /** Décorateur explicite, ou condition utilisée directement comme décorateur (avec {@code abort}). */
    public static UnaryOperator<BtNode> parseDecorator(JsonObject json) {
        Identifier type = id(type(json));
        DecoratorParser parser = DECORATORS.get(type);
        if (parser != null) {
            return parser.parse(json);
        }
        if (CONDITIONS.containsKey(type)) {
            BtCondition condition = parseCondition(json);
            BtAbort abort = BtAbort.parse(string(json, "abort", "none"));
            return child -> new ConditionDecorator(child, condition, abort);
        }
        throw new JsonParseException("décorateur inconnu : " + type);
    }

    private static List<Supplier<BtNode>> children(JsonObject json) {
        List<Supplier<BtNode>> out = new ArrayList<>();
        for (JsonElement child : list(json, "children")) {
            out.add(parseNode(child));
        }
        if (out.isEmpty()) {
            throw new JsonParseException(type(json) + " sans \"children\"");
        }
        return out;
    }

    private static List<BtNode> create(List<Supplier<BtNode>> factories) {
        List<BtNode> nodes = new ArrayList<>(factories.size());
        factories.forEach(f -> nodes.add(f.get()));
        return nodes;
    }

    private static Awareness awareness(String value) {
        try {
            return Awareness.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("niveau d'alerte inconnu : " + value + " (idle, suspicious, investigate, hunt)");
        }
    }

    private static NoiseType noise(String value) {
        try {
            return NoiseType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("type de bruit inconnu : " + value);
        }
    }

    private static Identifier s(String path) {
        return com.ananasloll3.sentience.Sentience.id(path);
    }

    // ------------------------------------------------------------------ types intégrés

    static {
        // Composites
        registerNode(s("sequence"), json -> {
            var c = children(json);
            return () -> new Sequence(create(c)).named("sequence");
        });
        registerNode(s("selector"), json -> {
            var c = children(json);
            return () -> new Selector(create(c)).named("selector");
        });
        registerNode(s("random_selector"), json -> {
            var c = children(json);
            return () -> new RandomSelector(create(c)).named("random_selector");
        });
        registerNode(s("parallel"), json -> {
            var c = children(json);
            boolean all = string(json, "require", "all").equalsIgnoreCase("all");
            return () -> new Parallel(create(c), all).named("parallel");
        });
        registerNode(s("subtree"), json -> {
            Identifier tree = id(string(json, "tree", null));
            return () -> Bt.subtree(tree);
        });
        registerNode(s("check"), json -> {
            BtCondition condition = parseCondition(json.get("condition"));
            return () -> Bt.check(condition);
        });
        registerNode(s("succeed"), json -> Bt::succeed);
        registerNode(s("fail"), json -> Bt::fail);

        // Décorateurs (aussi utilisables comme nœuds avec "child")
        registerDecorator(s("condition"), json -> {
            BtCondition condition = parseCondition(json.get("condition"));
            BtAbort abort = BtAbort.parse(string(json, "abort", "none"));
            return child -> new ConditionDecorator(child, condition, abort);
        });
        registerDecorator(s("cooldown"), json -> {
            int ticks = integer(json, "ticks", 100);
            return child -> new Cooldown(child, ticks);
        });
        registerDecorator(s("time_limit"), json -> {
            int ticks = integer(json, "ticks", 200);
            return child -> new TimeLimit(child, ticks);
        });
        registerDecorator(s("repeat"), json -> {
            int count = integer(json, "count", -1);
            return child -> new Repeat(child, count);
        });
        registerDecorator(s("invert"), json -> child -> new MapResult(child, MapResult.Mode.INVERT));
        registerDecorator(s("force_success"), json -> child -> new MapResult(child, MapResult.Mode.FORCE_SUCCESS));
        registerDecorator(s("force_failure"), json -> child -> new MapResult(child, MapResult.Mode.FORCE_FAILURE));

        // Tâches
        registerNode(s("wait"), json -> {
            int ticks = integer(json, "ticks", 20);
            int deviation = integer(json, "deviation", 0);
            return () -> Bt.wait(ticks, deviation);
        });
        registerNode(s("move_to"), json -> {
            BlackboardKey<?> target = key(json, "target", "focus_position");
            double speed = number(json, "speed", 1.0);
            double acceptance = number(json, "acceptance", 1.5);
            int timeout = integer(json, "timeout", 0);
            return () -> Bt.moveTo(target, speed, acceptance, timeout);
        });
        registerNode(s("wander"), json -> {
            double radius = number(json, "radius", 8);
            double speed = number(json, "speed", 0.8);
            return () -> Bt.wander(radius, speed);
        });
        registerNode(s("search"), json -> {
            BlackboardKey<?> center = key(json, "target", "focus_position");
            double radius = number(json, "radius", 6);
            int ticks = integer(json, "ticks", 200);
            double speed = number(json, "speed", 0.8);
            return () -> Bt.search(center, radius, ticks, speed);
        });
        registerNode(s("flee"), json -> {
            BlackboardKey<?> from = key(json, "from", "focus_position");
            double distance = number(json, "distance", 12);
            double speed = number(json, "speed", 1.3);
            return () -> Bt.flee(from, distance, speed);
        });
        registerNode(s("look_at"), json -> {
            BlackboardKey<?> target = key(json, "target", "focus_position");
            int ticks = integer(json, "ticks", 0);
            return () -> Bt.lookAt(target, ticks);
        });
        registerNode(s("attack"), json -> {
            BlackboardKey<?> target = key(json, "target", "visible_target");
            double speed = number(json, "speed", 1.3);
            int cooldown = integer(json, "cooldown", 20);
            return () -> Bt.attack(target, speed, cooldown);
        });
        registerNode(s("run_query"), json -> {
            Identifier query = id(string(json, "query", null));
            BlackboardKey<?> result = key(json, "result", "query_result");
            return () -> Bt.runQuery(query, result);
        });
        registerNode(s("set"), json -> {
            BlackboardKey<?> key = key(json, "key", null);
            if (json.has("from")) {
                BlackboardKey<?> from = key(json, "from", null);
                return () -> Bt.copy(from, key);
            }
            Object value = value(json.get("value"));
            if (value != null && key.type() != Object.class && !key.type().isInstance(value)) {
                throw new JsonParseException("la clé " + key.id() + " attend un " + key.type().getSimpleName());
            }
            return () -> Bt.set(key, value);
        });
        registerNode(s("clear"), json -> {
            BlackboardKey<?> key = key(json, "key", null);
            return () -> Bt.clear(key);
        });
        registerNode(s("play_sound"), json -> {
            SoundEvent sound = SoundEvent.of(id(string(json, "sound", null)));
            float volume = (float) number(json, "volume", 1);
            float pitch = (float) number(json, "pitch", 1);
            return () -> Bt.playSound(sound, volume, pitch);
        });
        registerNode(s("emit_noise"), json -> {
            float db = (float) number(json, "db", 80);
            NoiseType type = noise(string(json, "noise", "generic"));
            return () -> Bt.emitNoise(db, type);
        });
        registerNode(s("share_focus"), json -> {
            double range = number(json, "range", 32);
            boolean sameType = bool(json, "same_type", true);
            return () -> Bt.shareFocus(range, sameType);
        });
        registerNode(s("give_up"), json -> Bt::giveUp);
        registerNode(s("alert"), json -> {
            Awareness level = awareness(string(json, "level", null));
            return () -> Bt.alert(level);
        });
        registerNode(s("set_attack_target"), json -> {
            BlackboardKey<?> target = key(json, "target", "visible_target");
            return () -> Bt.setAttackTarget(target);
        });

        // Conditions
        registerCondition(s("awareness"), json -> {
            if (json.has("at_least")) return Bt.awarenessAtLeast(awareness(json.get("at_least").getAsString()));
            if (json.has("below")) return Bt.awarenessAtLeast(awareness(json.get("below").getAsString())).negate();
            return Bt.awarenessIs(awareness(string(json, "is", null)));
        });
        registerCondition(s("is_set"), json -> Bt.isSet(key(json, "key", null)));
        registerCondition(s("sees_target"), json -> Bt.seesTarget());
        registerCondition(s("alive"), json -> Bt.alive(key(json, "target", "visible_target")));
        registerCondition(s("health"), json -> {
            if (json.has("below")) return Bt.healthBelow((float) number(json, "below", 0.5));
            return Bt.healthBelow((float) number(json, "above", 0.5)).negate();
        });
        registerCondition(s("distance"), json -> {
            BlackboardKey<?> target = key(json, "target", "focus_position");
            BtCondition c = ctx -> true;
            if (json.has("below")) c = c.and(Bt.distanceBelow(target, number(json, "below", 0)));
            if (json.has("above")) c = c.and(Bt.distanceAbove(target, number(json, "above", 0)));
            return c;
        });
        registerCondition(s("is_night"), json -> Bt.isNight());
        registerCondition(s("is_day"), json -> Bt.isNight().negate());
        registerCondition(s("light"), json -> {
            BtCondition c = ctx -> true;
            if (json.has("below")) c = c.and(Bt.lightBelow(integer(json, "below", 0)));
            if (json.has("above")) c = c.and(Bt.lightBelow(integer(json, "above", 0) + 1).negate());
            return c;
        });
        registerCondition(s("chance"), json -> Bt.chance(number(json, "chance", 0.5)));
        registerCondition(s("in_water"), json -> Bt.inWater());
        registerCondition(s("equals"), json -> {
            BlackboardKey<?> key = key(json, "key", null);
            Object value = value(json.get("value"));
            return ctx -> java.util.Objects.equals(ctx.blackboard().get(key), value);
        });
        registerCondition(s("not"), json -> parseCondition(json.get("condition")).negate());
        registerCondition(s("all"), json -> {
            BtCondition c = ctx -> true;
            for (JsonElement e : list(json, "conditions")) c = c.and(parseCondition(e));
            return c;
        });
        registerCondition(s("any"), json -> {
            BtCondition c = ctx -> false;
            for (JsonElement e : list(json, "conditions")) c = c.or(parseCondition(e));
            return c;
        });

        // Services
        registerService(s("run_query"), json -> {
            Identifier query = id(string(json, "query", null));
            BlackboardKey<?> result = key(json, "result", "query_result");
            BtNode node = Bt.runQuery(query, result);   // action sans état : partageable
            return node::tick;
        });
        registerService(s("share_focus"), json -> {
            double range = number(json, "range", 32);
            boolean sameType = bool(json, "same_type", true);
            BtNode node = Bt.shareFocus(range, sameType);
            return node::tick;
        });
        registerService(s("copy"), json -> {
            BtNode node = Bt.copy(key(json, "from", null), key(json, "to", null));
            return node::tick;
        });
        registerService(s("look_at"), json -> {
            BlackboardKey<?> target = key(json, "target", "focus_position");
            return ctx -> {
                var pos = BtTargets.position(ctx, target);
                if (pos != null) ctx.mob().getLookControl().lookAt(pos.x, pos.y + 1.5, pos.z, 30f, 30f);
            };
        });
    }

    /** Force l'enregistrement des types intégrés (appelé par Sentience). */
    public static void bootstrap() {
    }
}
