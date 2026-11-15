package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.List;

/** Exécute ses enfants dans l'ordre ; échoue dès qu'un enfant échoue, réussit s'ils réussissent tous. */
public class Sequence extends BtComposite {

    private int index;

    public Sequence(List<? extends BtNode> children) {
        super(children);
    }

    @Override
    protected void onStart(BtContext ctx) {
        index = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        while (index < children.size()) {
            BtStatus status = children.get(index).tick(ctx);
            if (status != BtStatus.SUCCESS) {
                return status;
            }
            index++;
        }
        return BtStatus.SUCCESS;
    }
}
