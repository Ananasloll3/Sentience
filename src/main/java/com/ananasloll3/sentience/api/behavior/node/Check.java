package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtCondition;
import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** Feuille qui réussit si la condition est vraie. */
public class Check extends BtNode {

    private final BtCondition condition;

    public Check(BtCondition condition) {
        this.condition = condition;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        return BtStatus.of(condition.test(ctx));
    }
}
