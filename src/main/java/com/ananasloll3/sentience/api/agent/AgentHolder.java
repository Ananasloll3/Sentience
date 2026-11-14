package com.ananasloll3.sentience.api.agent;

import org.jetbrains.annotations.Nullable;

/**
 * Implémentée par tous les {@link net.minecraft.entity.mob.MobEntity} via mixin.
 * Utiliser plutôt {@link Agent#of(net.minecraft.entity.Entity)}.
 */
public interface AgentHolder {

    @Nullable
    Agent sentience$getAgent();

    void sentience$setAgent(@Nullable Agent agent);
}
