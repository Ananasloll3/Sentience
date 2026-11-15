package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** Interrompt l'enfant et échoue s'il tourne plus de {@code ticks}. */
public class TimeLimit extends BtDecorator {

    private final int ticks;
    private int elapsed;

    public TimeLimit(BtNode child, int ticks) {
        super(child);
        this.ticks = ticks;
    }

    @Override
    protected void onStart(BtContext ctx) {
        elapsed = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        elapsed += ctx.deltaTicks();
        if (elapsed > ticks) {
            child.abort(ctx);
            return BtStatus.FAILURE;
        }
        return child.tick(ctx);
    }
}
