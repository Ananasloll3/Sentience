package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.ArrayList;
import java.util.List;

/** Comme un sélecteur, mais essaie ses enfants dans un ordre aléatoire à chaque entrée. */
public class RandomSelector extends BtComposite {

    private final List<BtNode> order = new ArrayList<>();
    private int index;

    public RandomSelector(List<? extends BtNode> children) {
        super(children);
    }

    @Override
    protected void onStart(BtContext ctx) {
        order.clear();
        order.addAll(children);
        for (int i = order.size() - 1; i > 0; i--) {
            int j = ctx.random().nextInt(i + 1);
            BtNode tmp = order.get(i);
            order.set(i, order.get(j));
            order.set(j, tmp);
        }
        index = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        while (index < order.size()) {
            BtStatus status = order.get(index).tick(ctx);
            if (status != BtStatus.FAILURE) {
                return status;
            }
            index++;
        }
        return BtStatus.FAILURE;
    }
}
