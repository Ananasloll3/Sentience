package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.awareness.AwarenessProfile;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.api.perception.scent.ScentSense;
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
 * Le Flaireur : presque sourd, il suit ta piste à l'odeur, pas à pas.
 * L'eau coupe la piste, la pluie l'efface plus vite. Teste l'odorat et la mémoire (fusion des traces).
 */
public class HoundEntity extends HostileEntity {

    private Agent agent;

    public HoundEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 8;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        agent = Agent.builder(this)
                .rules(SentienceTestMod.HOUND_RULES)
                .scent(ScentSense.hound())
                .hearing(HearingProfile.DULL)
                .touch()
                .damage()
                .awareness(new AwarenessProfile(0.5f, 4f, 20f))
                .defaultGoals(2, 1.0, 1.35)
                .searchDuration(80) // fouille courte : il repart vite sur la trace suivante
                .build();
        agent.onAwarenessChanged((a, from, to) -> {
            if (to == Awareness.HUNT && from != Awareness.HUNT) {
                playSound(SoundEvents.ENTITY_ZOGLIN_ANGRY, 1.5f, 0.7f);
            } else if (to == Awareness.INVESTIGATE && from.ordinal() < to.ordinal()) {
                playSound(SoundEvents.ENTITY_SNIFFER_SNIFFING, 1f, 1.2f);
            }
        });

        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new PreyMeleeGoal(this, agent, 1.6, 20));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOGLIN_DEATH;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
