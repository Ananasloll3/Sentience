package com.ananasloll3.sentience.testmod.client;

import com.ananasloll3.sentience.testmod.entity.HoundEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Le Flaireur réutilise le modèle et la texture « en colère » du loup vanilla, en plus grand. */
public class HoundRenderer extends MobEntityRenderer<HoundEntity, WolfEntityRenderState, WolfEntityModel> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/wolf/wolf_black_angry.png");

    public HoundRenderer(EntityRendererFactory.Context context) {
        super(context, new WolfEntityModel(context.getPart(EntityModelLayers.WOLF)), 0.6f);
    }

    @Override
    public WolfEntityRenderState createRenderState() {
        return new WolfEntityRenderState();
    }

    @Override
    public Identifier getTexture(WolfEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(WolfEntityRenderState state, MatrixStack matrices) {
        matrices.scale(1.25f, 1.25f, 1.25f);
    }
}
