package com.ananasloll3.sentience.api.rules;

import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Règles de la vue, appliquées côté OBSERVATEUR. Immuable ; modifier via {@link #toBuilder()}.
 */
public final class SightRules {

    /**
     * Discrétion d'une cible, multipliée à sa visibilité (camouflage, cape, peinture…).
     * 1 = neutre, 0,5 = deux fois moins visible, 2 = deux fois plus.
     */
    @FunctionalInterface
    public interface StealthModifier {
        float factor(MobEntity observer, LivingEntity target);
    }

    public static final Predicate<BlockState> DEFAULT_SEE_THROUGH =
            state -> state.isIn(BlockTags.IMPERMEABLE) || state.getBlock() instanceof PaneBlock;

    public static final SightRules DEFAULT = new Builder().build();

    public final float darknessFloor;
    public final float peripheralFactor;
    public final float sneakFactor;
    public final float invisibilityFactor;
    public final float movementFactor;
    public final boolean lightInHandReveals;
    public final boolean fireReveals;
    public final boolean glowingReveals;
    public final double instantDetectionDistance;
    public final Predicate<BlockState> seeThrough;
    public final List<StealthModifier> stealthModifiers;

    private SightRules(Builder b) {
        darknessFloor = b.darknessFloor;
        peripheralFactor = b.peripheralFactor;
        sneakFactor = b.sneakFactor;
        invisibilityFactor = b.invisibilityFactor;
        movementFactor = b.movementFactor;
        lightInHandReveals = b.lightInHandReveals;
        fireReveals = b.fireReveals;
        glowingReveals = b.glowingReveals;
        instantDetectionDistance = b.instantDetectionDistance;
        seeThrough = b.seeThrough;
        stealthModifiers = List.copyOf(b.stealthModifiers);
    }

    /** Produit de tous les modificateurs de discrétion. */
    public float stealthFactor(MobEntity observer, LivingEntity target) {
        float factor = 1f;
        for (StealthModifier modifier : stealthModifiers) {
            factor *= modifier.factor(observer, target);
        }
        return factor;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private float darknessFloor = 0.05f;
        private float peripheralFactor = 0.3f;
        private float sneakFactor = 0.5f;
        private float invisibilityFactor = 0.08f;
        private float movementFactor = 1.3f;
        private boolean lightInHandReveals = true;
        private boolean fireReveals = true;
        private boolean glowingReveals = true;
        private double instantDetectionDistance = 2.5;
        private Predicate<BlockState> seeThrough = DEFAULT_SEE_THROUGH;
        private final List<StealthModifier> stealthModifiers = new ArrayList<>();

        public Builder() {
        }

        private Builder(SightRules r) {
            darknessFloor = r.darknessFloor;
            peripheralFactor = r.peripheralFactor;
            sneakFactor = r.sneakFactor;
            invisibilityFactor = r.invisibilityFactor;
            movementFactor = r.movementFactor;
            lightInHandReveals = r.lightInHandReveals;
            fireReveals = r.fireReveals;
            glowingReveals = r.glowingReveals;
            instantDetectionDistance = r.instantDetectionDistance;
            seeThrough = r.seeThrough;
            stealthModifiers.addAll(r.stealthModifiers);
        }

        /** Visibilité minimale dans le noir total (0,05). */
        public Builder darknessFloor(float value) { this.darknessFloor = value; return this; }
        /** Efficacité de la vision périphérique (0,3). */
        public Builder peripheral(float factor) { this.peripheralFactor = factor; return this; }
        public Builder sneak(float factor) { this.sneakFactor = factor; return this; }
        public Builder invisibility(float factor) { this.invisibilityFactor = factor; return this; }
        /** Le mouvement attire l'œil (×1,3). */
        public Builder movement(float factor) { this.movementFactor = factor; return this; }
        public Builder lightInHandReveals(boolean value) { this.lightInHandReveals = value; return this; }
        public Builder fireReveals(boolean value) { this.fireReveals = value; return this; }
        public Builder glowingReveals(boolean value) { this.glowingReveals = value; return this; }
        /** En dessous de cette distance, une cible visible est repérée instantanément (2,5 blocs). */
        public Builder instantDetection(double distance) { this.instantDetectionDistance = distance; return this; }
        /** Blocs que le regard traverse (verre, vitres, barreaux par défaut). */
        public Builder seeThrough(Predicate<BlockState> predicate) { this.seeThrough = predicate; return this; }
        public Builder addStealthModifier(StealthModifier modifier) { stealthModifiers.add(modifier); return this; }

        public SightRules build() {
            return new SightRules(this);
        }
    }
}
