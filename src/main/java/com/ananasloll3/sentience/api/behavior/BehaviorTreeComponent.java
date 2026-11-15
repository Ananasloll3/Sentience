package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Exécute le Behavior Tree d'un agent. L'arbre est piloté par un {@link BehaviorTreeGoal} (il prend
 * les contrôles de mouvement et de regard du mob) ; il est recréé quand les datapacks sont rechargés.
 */
public final class BehaviorTreeComponent implements AgentComponent {

    private final Identifier treeId;
    @Nullable
    private final BehaviorTree fixed;

    @Nullable
    private Agent agent;
    @Nullable
    private BehaviorTree current;
    @Nullable
    private BtNode root;
    @Nullable
    private ServerWorld world;
    private int seenVersion = -1;
    private int pending;
    private boolean warned;

    /** Arbre désigné par son identifiant (Java ou datapack, rechargeable). */
    public BehaviorTreeComponent(Identifier treeId) {
        this.treeId = treeId;
        this.fixed = null;
    }

    /** Arbre donné directement. */
    public BehaviorTreeComponent(BehaviorTree tree) {
        this.treeId = tree.id();
        this.fixed = tree;
    }

    @Override
    public void onAttach(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void onUnload(Agent agent) {
        abort();
    }

    public Identifier treeId() {
        return treeId;
    }

    /** L'arbre est-il disponible ? */
    public boolean isReady() {
        return resolve() != null;
    }

    @Nullable
    private BehaviorTree resolve() {
        if (fixed != null) {
            BehaviorTree override = BehaviorTrees.get(treeId);
            return override != null ? override : fixed;
        }
        BehaviorTree tree = BehaviorTrees.get(treeId);
        if (tree == null && !warned) {
            warned = true;
            Sentience.LOGGER.warn("Behavior Tree {} introuvable pour {}.", treeId,
                    agent == null ? "?" : agent.mob().getType().getTranslationKey());
        }
        return tree;
    }

    /** Exécute un tick de l'arbre (en respectant le niveau de détail de l'agent). */
    public void run(ServerWorld world) {
        if (agent == null) {
            return;
        }
        this.world = world;
        if (seenVersion != BehaviorTrees.version() || current == null) {
            seenVersion = BehaviorTrees.version();
            BehaviorTree tree = resolve();
            if (tree != current) {
                abort();
                current = tree;
                root = null;
                if (tree != null) {
                    try {
                        root = tree.instantiate();
                    } catch (Exception e) {
                        Sentience.LOGGER.error("Impossible de créer le Behavior Tree {} : {}", tree.id(), e.getMessage());
                    }
                }
            }
        }
        if (root == null) {
            return;
        }
        pending++;
        if (pending < agent.thinkInterval()) {
            return;
        }
        int delta = pending;
        pending = 0;
        root.tick(new BtContext(agent, world, delta));
    }

    /** Interrompt l'arbre (le mob passe à un comportement plus prioritaire, ou disparaît). */
    public void abort() {
        if (root != null && agent != null && world != null) {
            root.abort(new BtContext(agent, world, 1));
        }
        pending = 0;
    }

    /** Noms des nœuds actifs, de la racine à la tâche en cours (debug). */
    public List<String> activePath() {
        List<String> path = new ArrayList<>();
        if (root != null) {
            root.describeActive(path);
        }
        return path;
    }
}
