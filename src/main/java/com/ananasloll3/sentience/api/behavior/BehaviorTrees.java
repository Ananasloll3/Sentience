package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.Sentience;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Tous les Behavior Trees connus : ceux déclarés en Java ({@link #register}) et ceux des datapacks,
 * dans {@code data/<namespace>/sentience/behavior_tree/<nom>.json}, rechargés par {@code /reload}.
 * Un arbre JSON remplace un arbre Java de même identifiant (un datapack peut donc modifier le comportement
 * des mobs d'un mod).
 *
 * <pre>{@code
 * {
 *   "root": {
 *     "type": "selector",
 *     "children": [
 *       { "type": "attack", "decorators": [ { "type": "sees_target", "abort": "both" } ] },
 *       { "type": "wander", "radius": 10 }
 *     ]
 *   }
 * }
 * }</pre>
 */
public final class BehaviorTrees {

    public static final ResourceFinder FINDER = ResourceFinder.json("sentience/behavior_tree");

    private static final Map<Identifier, BehaviorTree> CODE = new ConcurrentHashMap<>();
    private static volatile Map<Identifier, BehaviorTree> data = Map.of();
    private static volatile int version;

    private BehaviorTrees() {
    }

    /** Déclare un arbre écrit en Java. */
    public static BehaviorTree register(BehaviorTree tree) {
        CODE.put(tree.id(), tree);
        version++;
        return tree;
    }

    public static BehaviorTree register(Identifier id, Supplier<? extends BtNode> root) {
        return register(BehaviorTree.of(id, root));
    }

    @Nullable
    public static BehaviorTree get(Identifier id) {
        BehaviorTree tree = data.get(id);
        return tree != null ? tree : CODE.get(id);
    }

    public static Set<Identifier> ids() {
        Set<Identifier> ids = new TreeSet<>(CODE.keySet());
        ids.addAll(data.keySet());
        return ids;
    }

    /** Change à chaque rechargement : les agents recréent alors leur arbre. */
    public static int version() {
        return version;
    }

    /** Recharge les arbres des datapacks. Appelé par Sentience au chargement et à chaque {@code /reload}. */
    public static void reload(ResourceManager manager) {
        Map<Identifier, BehaviorTree> loaded = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : FINDER.findResources(manager).entrySet()) {
            Identifier id = FINDER.toResourceId(entry.getKey());
            try (Reader reader = entry.getValue().getReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Supplier<BtNode> root = BtRegistry.parseNode(json.getAsJsonObject().get("root"));
                loaded.put(id, BehaviorTree.of(id, root));
            } catch (Exception e) {
                Sentience.LOGGER.error("Behavior Tree {} ignoré : {}", id, e.getMessage());
            }
        }
        data = Map.copyOf(loaded);
        version++;
        // Vérifie que chaque arbre s'instancie (sous-arbres présents) une fois tout chargé
        for (BehaviorTree tree : loaded.values()) {
            try {
                tree.instantiate();
            } catch (Exception e) {
                Sentience.LOGGER.error("Behavior Tree {} invalide : {}", tree.id(), e.getMessage());
            }
        }
        if (!loaded.isEmpty()) {
            Sentience.LOGGER.info("{} Behavior Tree(s) chargé(s).", loaded.size());
        }
    }
}
