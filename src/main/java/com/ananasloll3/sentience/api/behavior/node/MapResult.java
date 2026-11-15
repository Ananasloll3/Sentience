package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtStatus;

/** Transforme le résultat de l'enfant : inverser, forcer le succès ou l'échec. */
public class MapResult extends BtDecorator {

    public enum Mode { INVERT, FORCE_SUCCESS, FORCE_FAILURE }

    private final Mode mode;

    public MapResult(BtNode child, Mode mode) {
        super(child);
        this.mode = mode;
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        BtStatus status = child.tick(ctx);
        if (status == BtStatus.RUNNING) {
            return status;
        }
        return switch (mode) {
            case INVERT -> status == BtStatus.SUCCESS ? BtStatus.FAILURE : BtStatus.SUCCESS;
            case FORCE_SUCCESS -> BtStatus.SUCCESS;
            case FORCE_FAILURE -> BtStatus.FAILURE;
        };
    }
}
