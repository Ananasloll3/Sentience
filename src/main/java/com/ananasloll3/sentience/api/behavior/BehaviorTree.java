package com.ananasloll3.sentience.api.behavior;

import net.minecraft.util.Identifier;

import java.util.function.Supplier;

/**
 * Un Behavior Tree : un identifiant et une fabrique qui crée la racine pour chaque agent
 * (les nœuds gardent un état d'exécution, ils ne sont jamais partagés).
 *
 * <pre>{@code
 * public static final BehaviorTree GUARD = BehaviorTree.of(Identifier.of("monmod", "guard"), () ->
 *     Bt.selector(
 *         Bt.sequence(Bt.moveTo(BlackboardKeys.FOCUS_POSITION, 1.3), Bt.wait(40))
 *             .guard(Bt.awarenessAtLeast(Awareness.INVESTIGATE), BtAbort.BOTH),
 *         Bt.wander(8, 0.7)));
 * }</pre>
 *
 * Les arbres peuvent aussi être écrits en JSON dans un datapack, voir {@link BehaviorTrees}.
 */
public record BehaviorTree(Identifier id, Supplier<? extends BtNode> root) {

    public static BehaviorTree of(Identifier id, Supplier<? extends BtNode> root) {
        return new BehaviorTree(id, root);
    }

    public BtNode instantiate() {
        return root.get();
    }
}
