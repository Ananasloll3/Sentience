package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.function.Function;

/** Feuille écrite avec une lambda, pour les tâches simples sans état. */
public class Action extends BtNode {

    private final Function<BtContext, BtStatus> action;

    public Action(Function<BtContext, BtStatus> action) {
        this.action = action;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        return action.apply(ctx);
    }
}
