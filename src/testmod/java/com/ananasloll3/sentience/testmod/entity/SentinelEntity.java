package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComms;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.blackboard.BlackboardKeys;
import com.ananasloll3.sentience.api.perception.Stimulus;
import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.ananasloll3.sentience.api.perception.sight.SightConfig;
import com.ananasloll3.sentience.testmod.SentienceTestMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.UUID;

/**
 * La Sentinelle : sourde et immobile, elle ne fait que regarder.
 * Teste la vue : lumière, obscurité, accroupi, invisibilité, couverture, vitres.
 * Quand elle te repère, elle sonne l'alarme (un vrai bruit, que les autres créatures entendent),
 * prévient ses alliées par radio (AgentComms) et tire.
 */
public class SentinelEntity extends HostileEntity {

    private static final int ALARM_INTERVAL = 60;
    private Agent agent;
    private int alarmCooldown;

    public SentinelEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 30.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .rules(SentienceTestMod.RULES)
                .sight(SightConfig.humanoid().withRange(40).withDetectTicks(30))
                .damage()
                .awareness(AwarenessProfile.DEFAULT)
                .build();
        agent.onAwarenessChanged((a, from, to) -> {
            if (to == Awareness.HUNT) {
                alarmCooldown = 0;
            }
        });
        goalSelector.add(1, new WatchGoal());
        goalSelector.add(2, new ShootVisibleGoal());
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        if (agent.awarenessState() == Awareness.HUNT && --alarmCooldown <= 0) {
            alarmCooldown = ALARM_INTERVAL;
            // Alarme sonore : entendue par TOUTES les créatures dotées d'ouïe, à travers les murs
            playSound(SoundEvents.BLOCK_BELL_USE, 3f, 0.8f);
            HearingSystem.report(world, getEyePos(), 105f, NoiseType.MECHANISM, this);
            // Radio : les autres sentinelles reçoivent directement la position (dégradée)
            AgentComms.shareFocus(agent, 48, AgentComms.sameType(agent));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    /** Regarde le focus ; sinon balaye lentement l'horizon. */
    private final class WatchGoal extends Goal {
        private float scanYaw;
        private int scanTicks;

        WatchGoal() {
            setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Stimulus focus = agent.awareness() == null ? null : agent.awareness().focus();
            if (focus != null && agent.awarenessState().atLeast(Awareness.SUSPICIOUS)) {
                Vec3d p = focus.getPosition();
                getLookControl().lookAt(p.x, p.y + 1.5, p.z, 15f, 30f);
                return;
            }
            if (--scanTicks <= 0) {
                scanYaw = getBodyYaw() + (random.nextBoolean() ? 1 : -1) * (50f + random.nextFloat() * 60f);
                scanTicks = 50 + random.nextInt(40);
            }
            Vec3d dir = Vec3d.fromPolar(0f, scanYaw);
            getLookControl().lookAt(getX() + dir.x * 10, getEyeY(), getZ() + dir.z * 10, 4f, 30f);
            setBodyYaw(getHeadYaw());
        }
    }

    /** Tire une flèche sur la cible qu'elle voit réellement (pas sur une supposition). */
    private final class ShootVisibleGoal extends Goal {
        private int cooldown;

        @Override
        public boolean canStart() {
            return agent.blackboard().has(BlackboardKeys.VISIBLE_TARGET);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (--cooldown > 0 || !(getEntityWorld() instanceof ServerWorld world)) {
                return;
            }
            UUID id = agent.blackboard().get(BlackboardKeys.VISIBLE_TARGET);
            if (id == null || !(world.getEntity(id) instanceof LivingEntity target)) {
                return;
            }
            ArrowEntity arrow = new ArrowEntity(world, SentinelEntity.this, new ItemStack(Items.ARROW), null);
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            double dy = target.getBodyY(0.33) - arrow.getY() + Math.sqrt(dx * dx + dz * dz) * 0.2;
            arrow.setVelocity(dx, dy, dz, 1.6f, 6f);
            arrow.setDamage(1.5);
            world.spawnEntity(arrow);
            playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1f, 1f);
            cooldown = 40;
        }
    }
}
