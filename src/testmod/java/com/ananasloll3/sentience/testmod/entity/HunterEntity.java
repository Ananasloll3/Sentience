package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.api.perception.sight.SightConfig;
import com.ananasloll3.sentience.testmod.SentienceTestMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Le Chasseur : tout son comportement est un Behavior Tree JSON
 * ({@code data/sentience_testmod/sentience/behavior_tree/hunter.json}), modifiable à chaud avec /reload.
 * Il patrouille dans les coins sombres, contourne sa proie, se cache quand il est blessé
 * et prévient ses semblables — grâce aux requêtes d'environnement (EQS).
 */
public class HunterEntity extends HostileEntity {

    public HunterEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 15;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        Agent.builder(this)
                .rules(SentienceTestMod.RULES)
                .hearing(HearingProfile.DEFAULT)
                .sight(SightConfig.humanoid())
                .damage()
                .awareness(AwarenessProfile.DEFAULT)
                .behaviorTree(SentienceTestMod.id("hunter"), 1)
                .build();

        goalSelector.add(0, new SwimGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PILLAGER_DEATH;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
