package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.List;

/**
 * Essaie ses enfants dans l'ordre de priorité ; réussit dès qu'un enfant réussit, échoue si tous échouent.
 * Un enfant plus prioritaire qui le demande ({@code abort: lower}) interrompt l'enfant en cours.
 */
public class Selector extends BtComposite {

    private int index;

    public Selector(List<? extends BtNode> children) {
        super(children);
    }

    @Override
    protected void onStart(BtContext ctx) {
        index = 0;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        for (int i = 0; i < index && i < children.size(); i++) {
            if (children.get(i).wantsToPreempt(ctx)) {
                children.get(index).abort(ctx);
                index = i;
                break;
            }
        }
        while (index < children.size()) {
            BtStatus status = children.get(index).tick(ctx);
            if (status != BtStatus.FAILURE) {
                return status;
            }
            index++;
        }
        return BtStatus.FAILURE;
    }
}
