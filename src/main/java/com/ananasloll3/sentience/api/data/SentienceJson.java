package com.ananasloll3.sentience.api.data;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lecture des fichiers JSON de Sentience (Behavior Trees, requêtes). Un identifiant sans espace de noms
 * ({@code "sequence"}, {@code "focus_position"}) désigne celui de Sentience.
 */
public final class SentienceJson {

    private SentienceJson() {
    }

    public static Identifier id(String value) {
        return value.indexOf(':') >= 0 ? Identifier.of(value) : Sentience.id(value);
    }

    public static JsonObject object(JsonElement element, String what) {
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException(what + " : objet JSON attendu");
        }
        return element.getAsJsonObject();
    }

    public static String type(JsonObject json) {
        if (!json.has("type")) {
            throw new JsonParseException("champ \"type\" manquant dans " + json);
        }
        return json.get("type").getAsString();
    }

    public static String string(JsonObject json, String field, @Nullable String fallback) {
        if (!json.has(field)) {
            if (fallback == null) throw new JsonParseException("champ \"" + field + "\" manquant dans " + json);
            return fallback;
        }
        return json.get(field).getAsString();
    }

    public static int integer(JsonObject json, String field, int fallback) {
        return json.has(field) ? json.get(field).getAsInt() : fallback;
    }

    public static double number(JsonObject json, String field, double fallback) {
        return json.has(field) ? json.get(field).getAsDouble() : fallback;
    }

    public static boolean bool(JsonObject json, String field, boolean fallback) {
        return json.has(field) ? json.get(field).getAsBoolean() : fallback;
    }

    /** Clé du Blackboard désignée par son identifiant. */
    public static BlackboardKey<?> key(JsonObject json, String field, @Nullable String fallback) {
        return BlackboardKey.byId(id(string(json, field, fallback)));
    }

    /** Valeur JSON simple convertie en booléen, entier, nombre ou texte. */
    @Nullable
    public static Object value(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        if (!element.isJsonPrimitive()) throw new JsonParseException("valeur simple attendue : " + element);
        JsonPrimitive p = element.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) {
            double d = p.getAsDouble();
            return d == Math.rint(d) && Math.abs(d) < Integer.MAX_VALUE ? (Object) (int) d : (Object) d;
        }
        return p.getAsString();
    }

    public static List<JsonElement> list(JsonObject json, String field) {
        if (!json.has(field)) return List.of();
        JsonElement e = json.get(field);
        if (!e.isJsonArray()) throw new JsonParseException("\"" + field + "\" : tableau attendu");
        JsonArray array = e.getAsJsonArray();
        List<JsonElement> out = new ArrayList<>(array.size());
        array.forEach(out::add);
        return out;
    }
}
