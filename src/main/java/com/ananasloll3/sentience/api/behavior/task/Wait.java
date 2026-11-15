package com.ananasloll3.sentience.api.behavior.task;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** Attend {@code ticks} (± {@code deviation}) puis réussit. */
public class Wait extends BtNode {

    private final int ticks;
    private final int deviation;
    private int remaining;

    public Wait(int ticks, int deviation) {
        this.ticks = ticks;
        this.deviation = deviation;
    }

    @Override
    protected void onStart(BtContext ctx) {
        remaining = ticks + (deviation > 0 ? ctx.random().nextBetween(-deviation, deviation) : 0);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        remaining -= ctx.deltaTicks();
        return remaining <= 0 ? BtStatus.SUCCESS : BtStatus.RUNNING;
    }
}
