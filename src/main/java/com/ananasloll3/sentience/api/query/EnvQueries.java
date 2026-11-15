package com.ananasloll3.sentience.api.query;

import com.ananasloll3.sentience.Sentience;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.ananasloll3.sentience.api.data.SentienceJson.*;

/**
 * Requêtes d'environnement connues : intégrées, déclarées en Java ({@link #register}), ou chargées depuis
 * {@code data/<namespace>/sentience/env_query/<nom>.json} (rechargées par {@code /reload}).
 *
 * <p>Requêtes intégrées (la « menace » est la cible vue, sinon le focus) :</p>
 * <ul>
 *   <li>{@link #HIDE} : un endroit proche où la menace ne te voit pas, plutôt loin d'elle ;</li>
 *   <li>{@link #COVER} : un abri tout proche, avec un bloc entre toi et la menace ;</li>
 *   <li>{@link #FLANK} : un point sur le côté de la menace, pour la contourner ;</li>
 *   <li>{@link #FLEE} : un point loin de la menace, hors de sa vue si possible.</li>
 * </ul>
 */
public final class EnvQueries {

    public static final Identifier HIDE = Sentience.id("hide");
    public static final Identifier COVER = Sentience.id("cover");
    public static final Identifier FLANK = Sentience.id("flank");
    public static final Identifier FLEE = Sentience.id("flee");

    public static final ResourceFinder FINDER = ResourceFinder.json("sentience/env_query");

    private static final Map<Identifier, EnvQuery> CODE = new ConcurrentHashMap<>();
    private static volatile Map<Identifier, EnvQuery> data = Map.of();

    static {
        register(HIDE, EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 4, 16, 3, 12))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))
                .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(1))
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1.5))
                .pathable()
                .build());
        register(COVER, EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 2, 10, 3, 12))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))
                .test(QueryTest.cover(QueryTarget.THREAT).atLeast(1))
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(2))
                .test(QueryTest.distance(QueryTarget.THREAT).preferLow(0.5))
                .pathable()
                .build());
        register(FLANK, EnvQuery.builder(QueryGenerator.ring(QueryTarget.THREAT, 4, 8, 2, 16))
                .test(QueryTest.dot(QueryTarget.THREAT, QueryTarget.QUERIER).between(-0.4, 0.5))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).preferLow(0.5))
                .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1))
                .pathable()
                .build());
        register(FLEE, EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 10, 20, 2, 16))
                .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(2))
                .test(QueryTest.visibleFrom(QueryTarget.THREAT).preferLow(1))
                .pathable()
                .build());
    }

    private EnvQueries() {
    }

    public static EnvQuery register(Identifier id, EnvQuery query) {
        CODE.put(id, query);
        return query;
    }

    @Nullable
    public static EnvQuery get(Identifier id) {
        EnvQuery query = data.get(id);
        return query != null ? query : CODE.get(id);
    }

    public static Set<Identifier> ids() {
        Set<Identifier> ids = new TreeSet<>(CODE.keySet());
        ids.addAll(data.keySet());
        return ids;
    }

    public static void reload(ResourceManager manager) {
        Map<Identifier, EnvQuery> loaded = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : FINDER.findResources(manager).entrySet()) {
            Identifier id = FINDER.toResourceId(entry.getKey());
            try (Reader reader = entry.getValue().getReader()) {
                loaded.put(id, parse(object(JsonParser.parseReader(reader), "requête")));
            } catch (Exception e) {
                Sentience.LOGGER.error("Requête d'environnement {} ignorée : {}", id, e.getMessage());
            }
        }
        data = Map.copyOf(loaded);
        if (!loaded.isEmpty()) {
            Sentience.LOGGER.info("{} requête(s) d'environnement chargée(s).", loaded.size());
        }
    }

    // ------------------------------------------------------------------ JSON

    public static EnvQuery parse(JsonObject json) {
        EnvQuery.Builder builder = EnvQuery.builder(generator(object(json.get("generator"), "generator")));
        for (JsonElement e : list(json, "tests")) {
            builder.test(test(object(e, "test")));
        }
        if (bool(json, "pathable", false)) {
            builder.pathable();
        }
        return builder.build();
    }

    private static QueryGenerator generator(JsonObject json) {
        QueryTarget center = QueryTarget.parse(string(json, "center", "querier"));
        return switch (type(json)) {
            case "ring" -> {
                double min;
                double max;
                JsonElement radius = json.get("radius");
                if (radius != null && radius.isJsonArray()) {
                    min = radius.getAsJsonArray().get(0).getAsDouble();
                    max = radius.getAsJsonArray().get(1).getAsDouble();
                } else {
                    min = max = number(json, "radius", 8);
                }
                yield QueryGenerator.ring(center, min, max, integer(json, "rings", 3), integer(json, "points", 12));
            }
            case "grid" -> QueryGenerator.grid(center, number(json, "radius", 10), number(json, "spacing", 2));
            default -> throw new JsonParseException("générateur inconnu : " + type(json) + " (ring, grid)");
        };
    }

    private static QueryTest test(JsonObject json) {
        QueryTest test = switch (type(json)) {
            case "distance" -> QueryTest.distance(QueryTarget.parse(string(json, "target", "threat")));
            case "visible_from" -> QueryTest.visibleFrom(QueryTarget.parse(string(json, "target", "threat")));
            case "dot" -> QueryTest.dot(QueryTarget.parse(string(json, "from", "threat")),
                    QueryTarget.parse(string(json, "to", "querier")));
            case "cover" -> QueryTest.cover(QueryTarget.parse(string(json, "target", "threat")));
            case "light" -> QueryTest.light();
            case "random" -> QueryTest.random();
            default -> throw new JsonParseException("test inconnu : " + type(json)
                    + " (distance, visible_from, dot, cover, light, random)");
        };
        test = test.between(number(json, "min", Double.NEGATIVE_INFINITY), number(json, "max", Double.POSITIVE_INFINITY));
        double weight = number(json, "weight", 0);
        if (weight != 0) {
            boolean low = string(json, "prefer", "high").toLowerCase(Locale.ROOT).equals("low");
            test = low ? test.preferLow(weight) : test.preferHigh(weight);
        }
        return test;
    }
}
