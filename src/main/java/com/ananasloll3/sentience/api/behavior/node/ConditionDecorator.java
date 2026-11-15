package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtAbort;
import com.ananasloll3.sentience.api.behavior.BtCondition;
import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** N'exécute l'enfant que si la condition est vraie. Voir {@link BtAbort} pour les interruptions. */
public class ConditionDecorator extends BtDecorator {

    private final BtCondition condition;
    private final BtAbort abort;

    public ConditionDecorator(BtNode child, BtCondition condition, BtAbort abort) {
        super(child);
        this.condition = condition;
        this.abort = abort;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        if ((!child.isActive() || abort.self()) && !condition.test(ctx)) {
            child.abort(ctx);
            return BtStatus.FAILURE;
        }
        return child.tick(ctx);
    }

    @Override
    public boolean wantsToPreempt(BtContext ctx) {
        return abort.lower() && !isActive() && condition.test(ctx) && !child.isBlocked(ctx);
    }

    @Override
    public boolean isBlocked(BtContext ctx) {
        return (!isActive() && !condition.test(ctx)) || child.isBlocked(ctx);
    }
}
