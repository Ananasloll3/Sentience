package com.ananasloll3.sentience.api.behavior.node;

import com.ananasloll3.sentience.api.behavior.BtContext;
import com.ananasloll3.sentience.api.behavior.BtNode;
import com.ananasloll3.sentience.api.behavior.BtService;
import com.ananasloll3.sentience.api.behavior.BtStatus;

import java.util.List;

/** Exécute des services à intervalle régulier tant que l'enfant est actif (et une fois à l'entrée). */
public class ServiceNode extends BtDecorator {

    public record Entry(BtService service, int interval) {
    }

    private final List<Entry> services;
    private final int[] countdowns;

    public ServiceNode(BtNode child, List<Entry> services) {
        super(child);
        this.services = List.copyOf(services);
        this.countdowns = new int[services.size()];
    }

    @Override
    protected void onStart(BtContext ctx) {
        for (int i = 0; i < services.size(); i++) {
            services.get(i).service().tick(ctx);
            countdowns[i] = Math.max(1, services.get(i).interval());
        }
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        for (int i = 0; i < services.size(); i++) {
            countdowns[i] -= ctx.deltaTicks();
            if (countdowns[i] <= 0) {
                services.get(i).service().tick(ctx);
                countdowns[i] = Math.max(1, services.get(i).interval());
            }
        }
        return child.tick(ctx);
    }
}
