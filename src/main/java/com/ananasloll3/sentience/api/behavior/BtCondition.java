package com.ananasloll3.sentience.api.behavior;

/**
 * Condition testée par un Behavior Tree (décorateur {@code condition}, nœud {@code check}…).
 * Une condition doit être sans état : la même instance est partagée par tous les agents.
 */
@FunctionalInterface
public interface BtCondition {

    boolean test(BtContext ctx);

    default BtCondition negate() {
        return ctx -> !test(ctx);
    }

    default BtCondition and(BtCondition other) {
        return ctx -> test(ctx) && other.test(ctx);
    }

    default BtCondition or(BtCondition other) {
        return ctx -> test(ctx) || other.test(ctx);
    }
}
