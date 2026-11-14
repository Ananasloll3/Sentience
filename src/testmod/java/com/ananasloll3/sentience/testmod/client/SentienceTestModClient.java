package com.ananasloll3.sentience.testmod.client;

import com.ananasloll3.sentience.testmod.SentienceTestMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public final class SentienceTestModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(SentienceTestMod.STALKER, ctx -> new BipedAgentRenderer<>(ctx,
                EntityModelLayers.ZOMBIE, tex("stalker"), tex("stalker_eyes"), 1.05f, 1.23f, 0.6f));
        EntityRendererRegistry.register(SentienceTestMod.SENTINEL, ctx -> new BipedAgentRenderer<>(ctx,
                EntityModelLayers.SKELETON, Identifier.ofVanilla("textures/entity/skeleton/skeleton.png"),
                tex("sentinel_eyes"), 1f, 1f, 0.5f));
        EntityRendererRegistry.register(SentienceTestMod.HOUND, HoundRenderer::new);
        EntityRendererRegistry.register(SentienceTestMod.APEX, ctx -> new BipedAgentRenderer<>(ctx,
                EntityModelLayers.ZOMBIE, tex("apex"), tex("apex_eyes"), 1.2f, 1.38f, 0.8f));
    }

    private static Identifier tex(String name) {
        return SentienceTestMod.id("textures/entity/" + name + ".png");
    }
}
