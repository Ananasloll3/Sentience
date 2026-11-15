package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.List;

/**
 * Exécute tous ses enfants à chaque tick. Avec {@code requireAll}, réussit quand tous ont réussi ;
 * sinon réussit dès que l'un réussit. Échoue dès que le succès devient impossible.
 * Les enfants encore en cours sont interrompus à la fin.
 */
public class Parallel extends BtComposite {

    private final boolean requireAll;
    private final BtStatus[] results;

    public Parallel(List<? extends BtNode> children, boolean requireAll) {
        super(children);
        this.requireAll = requireAll;
        this.results = new BtStatus[children.size()];
    }

    @Override
    protected void onStart(BtContext ctx) {
        java.util.Arrays.fill(results, null);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        int successes = 0;
        int failures = 0;
        for (int i = 0; i < children.size(); i++) {
            if (results[i] == null) {
                BtStatus status = children.get(i).tick(ctx);
                if (status != BtStatus.RUNNING) {
                    results[i] = status;
                }
            }
            if (results[i] == BtStatus.SUCCESS) successes++;
            if (results[i] == BtStatus.FAILURE) failures++;
        }
        int n = children.size();
        if (requireAll) {
            if (failures > 0) return BtStatus.FAILURE;
            if (successes == n) return BtStatus.SUCCESS;
        } else {
            if (successes > 0) return BtStatus.SUCCESS;
            if (failures == n) return BtStatus.FAILURE;
        }
        return BtStatus.RUNNING;
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
