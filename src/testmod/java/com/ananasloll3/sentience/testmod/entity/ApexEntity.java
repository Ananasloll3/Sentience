package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComms;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.api.perception.scent.ScentSense;
import com.ananasloll3.sentience.api.perception.sight.SightConfig;
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
 * L'Abomination : utilise TOUT Sentience.
 * Ouïe fine + voix, vue nocturne, odorat, douleur, toucher, alerte paranoïaque,
 * composant personnalisé (craint la lumière) + goal qui lit le Blackboard,
 * et prévient les autres créatures quand elle te chasse.
 */
public class ApexEntity extends HostileEntity {

    private Agent agent;

    public ApexEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 40;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 80.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.FOLLOW_RANGE, 72.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .rules(SentienceTestMod.RULES)
                .hearing(HearingProfile.KEEN)
                .sight(SightConfig.nocturnal().withRange(28))
                .scent(ScentSense.hound())
                .damage()
                .touch()
                .awareness(AwarenessProfile.PARANOID)
                .component(new PhotophobiaComponent(10))
                .defaultGoals(3, 0.95, 1.5)
                .searchDuration(300)
                .build();
        agent.onAwarenessChanged((a, from, to) -> {
            if (to == Awareness.HUNT && from != Awareness.HUNT) {
                playSound(SoundEvents.ENTITY_WARDEN_ROAR, 2f, 1.3f);
                // Toutes les créatures « Sentience » à 40 blocs apprennent où tu es
                AgentComms.shareFocus(a, 40, other -> true);
            } else if (to.ordinal() > from.ordinal()) {
                playSound(SoundEvents.ENTITY_WARDEN_SNIFF, 1f, 1.4f);
            }
        });

        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new PreyMeleeGoal(this, agent, 2.2, 25));
        goalSelector.add(2, new FleeLightGoal(this, agent, 1.3));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.5));
        goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
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
