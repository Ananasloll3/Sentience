package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;

import java.util.List;

/** Nœud à plusieurs enfants. */
public abstract class BtComposite extends BtNode {

    protected final List<BtNode> children;

    protected BtComposite(List<? extends BtNode> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " sans enfant");
        }
        this.children = List.copyOf(children);
    }

    public List<BtNode> children() {
        return children;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        for (BtNode child : children) {
            child.abort(ctx);
        }
    }

    @Override
    public void describeActive(List<String> path) {
        if (!isActive()) {
            return;
        }
        path.add(name());
        for (BtNode child : children) {
            if (child.isActive()) {
                child.describeActive(path);
                return;
            }
        }
    }
}
