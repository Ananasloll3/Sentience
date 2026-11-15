package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** Échoue tant que l'enfant a fini il y a moins de {@code ticks}. */
public class Cooldown extends BtDecorator {

    private final int ticks;
    private long readyAt = Long.MIN_VALUE;

    public Cooldown(BtNode child, int ticks) {
        super(child);
        this.ticks = ticks;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        if (!child.isActive() && ctx.time() < readyAt) {
            return BtStatus.FAILURE;
        }
        BtStatus status = child.tick(ctx);
        if (status != BtStatus.RUNNING) {
            readyAt = ctx.time() + ticks;
        }
        return status;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        if (aborted && child.isActive()) {
            readyAt = ctx.time() + ticks;
        }
        super.onStop(ctx, aborted);
    }

    @Override
    public boolean isBlocked(BtContext ctx) {
        return (!child.isActive() && ctx.time() < readyAt) || child.isBlocked(ctx);
    }

    @Override
    public boolean wantsToPreempt(BtContext ctx) {
        return ctx.time() >= readyAt && child.wantsToPreempt(ctx);
    }
}
