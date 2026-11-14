package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.testmod.SentienceTestMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Le Rôdeur : aveugle, il ne chasse qu'à l'oreille et au toucher.
 * Exemple complet d'utilisation de Sentience : tout son « cerveau » tient dans {@link #initGoals()}.
 */
public class StalkerEntity extends HostileEntity {

    private Agent agent;

    public StalkerEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 60.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.ATTACK_DAMAGE, 9.0)
                .add(EntityAttributes.FOLLOW_RANGE, 72.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .rules(SentienceTestMod.RULES)
                .hearing(HearingProfile.KEEN.withThreshold(12f).withMaxRange(72).withLocalization(1.3))
                .damage()
                .touch()
                .awareness(new AwarenessProfile(0.5f, 5f, 15f))
                .defaultGoals(2, 0.9, 1.45)
                .build();
        agent.onAwarenessChanged((a, from, to) -> growl(to, from));

        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new PreyMeleeGoal(this, agent, 2.0, 20));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        goalSelector.add(6, new LookAroundGoal(this));
    }

    private void growl(Awareness to, Awareness from) {
        if (to.ordinal() <= from.ordinal()) {
            return;
        }
        SoundEvent sound = switch (to) {
            case SUSPICIOUS -> SoundEvents.ENTITY_WARDEN_LISTENING;
            case INVESTIGATE -> SoundEvents.ENTITY_WARDEN_SNIFF;
            case HUNT -> SoundEvents.ENTITY_WARDEN_AGITATED;
            default -> null;
        };
        if (sound != null) {
            playSound(sound, to == Awareness.HUNT ? 2.0f : 1.0f, 0.6f + random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // silencieux tant qu'il n'a rien entendu
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WARDEN_DEATH;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
