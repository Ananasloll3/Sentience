package com.ananasloll3.sentience;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentTemplates;
import com.ananasloll3.sentience.api.perception.DamageSense;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.scent.ScentTrails;
import com.ananasloll3.sentience.api.voice.VoiceInput;
import com.ananasloll3.sentience.impl.debug.SentienceCommand;
import com.ananasloll3.sentience.impl.sound.WorldSoundCapture;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sentience : librairie d'IA pour Minecraft, inspirée des outils d'Unreal Engine
 * (perception multi-sens, Blackboard, niveaux d'alerte…). Point d'entrée des moddeurs : {@link Agent}.
 */
public final class Sentience implements ModInitializer {

    public static final String MOD_ID = "sentience";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MobEntity mob) {
                AgentTemplates.apply(mob);
                Agent agent = Agent.of(mob);
                if (agent != null) {
                    agent.load(world);
                }
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            Agent agent = Agent.of(entity);
            if (agent != null) {
                agent.unload();
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damage, blocked) -> {
            if (damage > 0f && !blocked) {
                ScentTrails.onDamaged(entity);
            }
            Agent agent = Agent.of(entity);
            PerceptionComponent perception = agent == null ? null : agent.perception();
            DamageSense sense = perception == null ? null : perception.sense(DamageSense.class);
            if (sense != null) {
                sense.onDamaged(agent, perception, source, damage);
            }
        });

        ServerTickEvents.END_WORLD_TICK.register(HearingSystem::tick);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            VoiceInput.tick(server);
            ScentTrails.tick(server);
            WorldSoundCapture.clearEmitters();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            HearingSystem.clearAll();
            ScentTrails.clearAll();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SentienceCommand.register(dispatcher));

        LOGGER.info("Sentience chargé.");
    }
}
