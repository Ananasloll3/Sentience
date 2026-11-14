package com.ananasloll3.sentience.api.goal;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessComponent;
import com.ananasloll3.sentience.api.perception.Stimulus;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * États INVESTIGATE et HUNT : va à la position estimée (en marchant ou en courant), puis fouille
 * la zone d'incertitude. Abandonne le stimulus si rien de nouveau n'est perçu pendant la fouille.
 */
public class InvestigateGoal extends Goal {

    protected final Agent agent;
    protected final MobEntity mob;
    protected final double walkSpeed;
    protected final double runSpeed;
    protected final int searchDuration;

    @Nullable
    private Stimulus target;
    private long targetVersion;
    private Vec3d lastPathGoal = Vec3d.ZERO;
    private int repathCooldown;
    private boolean searching;
    private int searchTicks;
    private int nextSearchPoint;

    /**
     * @param searchDuration temps de fouille sur place (ticks) avant d'abandonner un stimulus
     */
    public InvestigateGoal(Agent agent, double walkSpeed, double runSpeed, int searchDuration) {
        this.agent = agent;
        this.mob = agent.mob();
        this.walkSpeed = walkSpeed;
        this.runSpeed = runSpeed;
        this.searchDuration = searchDuration;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return agent.awarenessState().atLeast(Awareness.INVESTIGATE) && focus() != null;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        target = null;
        searching = false;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        target = null;
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
        double speed = agent.awarenessState() == Awareness.HUNT ? runSpeed : walkSpeed;

        // Nouveau stimulus ou nouvelle info : on repart vers la position mise à jour
        if (focus != target || focus.getLastTick() != targetVersion) {
            target = focus;
            targetVersion = focus.getLastTick();
            searching = false;
            moveTo(focus.getPosition(), speed, true);
        }

        if (--repathCooldown <= 0 && !searching) {
            moveTo(focus.getPosition(), speed, false);
        }

        double reach = Math.max(1.5, Math.min(focus.getErrorRadius() * 0.5, 3.0));
        boolean arrived = horizontalDistanceSq(mob.getEntityPos(), focus.getPosition()) <= reach * reach;
        if (!searching && (arrived || mob.getNavigation().isIdle())) {
            searching = true;
            searchTicks = 0;
            nextSearchPoint = 0;
        }

        if (searching) {
            search(focus, speed);
        } else {
            Vec3d p = focus.getPosition();
            mob.getLookControl().lookAt(p.x, p.y, p.z, 30f, 30f);
        }
    }

    private void search(Stimulus focus, double speed) {
        searchTicks++;
        if (searchTicks >= searchDuration) {
            AwarenessComponent awareness = agent.awareness();
            if (awareness != null) {
                awareness.giveUp(focus);
            }
            target = null;
            return;
        }
        if (--nextSearchPoint <= 0) {
            double radius = Math.max(3.0, Math.min(focus.getErrorRadius(), 10.0));
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            double dist = radius * Math.sqrt(mob.getRandom().nextDouble());
            Vec3d point = focus.getPosition().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            moveTo(point, speed * 0.8, true);
            nextSearchPoint = 30 + mob.getRandom().nextInt(30);
        }
    }

    private void moveTo(Vec3d pos, double speed, boolean force) {
        if (!force && pos.squaredDistanceTo(lastPathGoal) < 1.0 && !mob.getNavigation().isIdle()) {
            repathCooldown = 10;
            return;
        }
        mob.getNavigation().startMovingTo(pos.x, pos.y, pos.z, speed);
        lastPathGoal = pos;
        repathCooldown = 10;
    }

    @Nullable
    private Stimulus focus() {
        return agent.awareness() == null ? null : agent.awareness().focus();
    }

    private static double horizontalDistanceSq(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }
}
