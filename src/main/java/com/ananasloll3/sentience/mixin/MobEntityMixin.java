package com.ananasloll3.sentience.mixin;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.agent.AgentHolder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tout mob peut porter un agent ; celui-ci réfléchit juste avant l'IA vanilla. */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements AgentHolder {

    @Unique
    @Nullable
    private Agent sentience$agent;

    @Override
    public @Nullable Agent sentience$getAgent() {
        return sentience$agent;
    }

    @Override
    public void sentience$setAgent(@Nullable Agent agent) {
        this.sentience$agent = agent;
    }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    private void sentience$tickAgent(CallbackInfo ci) {
        if (sentience$agent != null && ((MobEntity) (Object) this).getEntityWorld() instanceof ServerWorld world) {
            sentience$agent.tick(world);
        }
    }
}
