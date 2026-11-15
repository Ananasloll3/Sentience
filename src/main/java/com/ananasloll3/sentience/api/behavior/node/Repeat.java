package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/**
 * Relance l'enfant tant qu'il réussit, {@code count} fois (ou sans fin si {@code count < 0}).
 * Échoue dès que l'enfant échoue. Une relance au plus par tick.
 */
public class Repeat extends BtDecorator {

    private final int count;
    private int done;

    public Repeat(BtNode child, int count) {
        super(child);
        this.count = count;
    }

    @Override
    protected void onStart(BtContext ctx) {
        done = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        BtStatus status = child.tick(ctx);
        if (status == BtStatus.SUCCESS) {
            done++;
            return count >= 0 && done >= count ? BtStatus.SUCCESS : BtStatus.RUNNING;
        }
        return status;
    }
}
