package com.ananasloll3.sentience.testmod.entity;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentComponent;
import com.ananasloll3.sentience.api.blackboard.BlackboardKey;
import com.ananasloll3.sentience.testmod.SentienceTestMod;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;

/**
 * Exemple de composant personnalisé : la créature craint la lumière.
 * Il mesure la lumière, l'écrit dans le Blackboard (lu par {@link FleeLightGoal}) et ralentit la créature.
 */
public class PhotophobiaComponent implements AgentComponent {

    /** Niveau de lumière sous la créature. */
    public static final BlackboardKey<Integer> LIGHT = BlackboardKey.of(SentienceTestMod.id("light"), Integer.class);
    /** Vrai si la lumière la gêne. */
    public static final BlackboardKey<Boolean> BLINDED = BlackboardKey.of(SentienceTestMod.id("blinded"), Boolean.class);

    private final int threshold;
    private int cooldown;

    public PhotophobiaComponent(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void tick(Agent agent, ServerWorld world, int deltaTicks) {
        cooldown -= deltaTicks;
        if (cooldown > 0) {
            return;
        }
        cooldown = 5;
        int light = world.getLightLevel(agent.mob().getBlockPos().up());
        boolean blinded = light >= threshold;
        agent.blackboard().set(LIGHT, light);
        agent.blackboard().set(BLINDED, blinded);
        if (blinded) {
            agent.mob().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, true, false));
        }
    }
}
