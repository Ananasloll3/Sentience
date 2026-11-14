package com.ananasloll3.sentience.testmod.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Rendu humanoïde générique : modèle vanilla, texture, yeux lumineux optionnels, mise à l'échelle. */
public class BipedAgentRenderer<T extends MobEntity> extends BipedEntityRenderer<T, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {

    private final Identifier texture;
    private final float scaleXZ;
    private final float scaleY;

    public BipedAgentRenderer(EntityRendererFactory.Context context, EntityModelLayer layer, Identifier texture,
                              @Nullable Identifier eyes, float scaleXZ, float scaleY, float shadow) {
        super(context, new BipedEntityModel<>(context.getPart(layer)), shadow);
        this.texture = texture;
        this.scaleXZ = scaleXZ;
        this.scaleY = scaleY;
        if (eyes != null) {
            RenderLayer eyesLayer = RenderLayer.getEyes(eyes);
            addFeature(new EyesFeatureRenderer<>(this) {
                @Override
                public RenderLayer getEyesTexture() {
                    return eyesLayer;
                }
            });
        }
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) {
        return texture;
    }

    @Override
    protected void scale(BipedEntityRenderState state, MatrixStack matrices) {
        matrices.scale(scaleXZ, scaleY, scaleXZ);
    }
}
