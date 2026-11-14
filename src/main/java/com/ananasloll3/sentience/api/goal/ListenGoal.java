package com.ananasloll3.sentience.api.goal;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/** État SUSPICIOUS : l'agent se fige et tourne la tête vers ce qu'il croit avoir perçu. */
public class ListenGoal extends Goal {

    protected final Agent agent;
    protected final MobEntity mob;
    private int jitterTicks;
    private Vec3d lookTarget = Vec3d.ZERO;

    public ListenGoal(Agent agent) {
        this.agent = agent;
        this.mob = agent.mob();
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return agent.awarenessState() == Awareness.SUSPICIOUS && focus() != null;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        jitterTicks = 0;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Stimulus focus = focus();
        if (focus == null) {
            return;
        }
        // Petits mouvements de tête : il cherche d'où ça vient
        if (--jitterTicks <= 0) {
            double spread = Math.min(focus.getErrorRadius(), 4.0);
            lookTarget = focus.getPosition().add(
                    (mob.getRandom().nextDouble() - 0.5) * spread,
                    0,
                    (mob.getRandom().nextDouble() - 0.5) * spread);
            jitterTicks = 15 + mob.getRandom().nextInt(20);
        }
        mob.getLookControl().lookAt(lookTarget.x, lookTarget.y, lookTarget.z, 20f, 20f);
    }

    private Stimulus focus() {
        return agent.awareness() == null ? null : agent.awareness().focus();
    }
}
