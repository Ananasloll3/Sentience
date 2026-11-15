package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;

import java.util.List;

/** Nœud qui enveloppe un enfant pour modifier son exécution ou son résultat. */
public abstract class BtDecorator extends BtNode {

    protected final BtNode child;

    protected BtDecorator(BtNode child) {
        this.child = child;
    }

    public BtNode child() {
        return child;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        child.abort(ctx);
    }

    @Override
    public boolean wantsToPreempt(BtContext ctx) {
        return child.wantsToPreempt(ctx);
    }

    @Override
    public boolean isBlocked(BtContext ctx) {
        return child.isBlocked(ctx);
    }

    /** Un décorateur est transparent dans le chemin de debug : seul l'enfant apparaît. */
    @Override
    public void describeActive(List<String> path) {
        if (isActive()) {
            child.describeActive(path);
        }
    }

    @Override
    public String name() {
        return child.name();
    }
}
