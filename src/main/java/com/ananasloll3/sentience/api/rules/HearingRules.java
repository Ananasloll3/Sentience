package com.ananasloll3.sentience.api.rules;

import com.ananasloll3.sentience.api.perception.hearing.NoiseEvent;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Règles physiques de l'ouïe, appliquées côté AUDITEUR : deux mods peuvent entendre le même bruit
 * avec des règles différentes. Immuable ; modifier via {@link #toBuilder()}.
 */
public final class HearingRules {

    /** Change l'intensité d'un bruit pour cet auditeur. Renvoyer {@code Float.NaN} pour l'ignorer. */
    @FunctionalInterface
    public interface NoiseModifier {
        float modify(NoiseEvent event, float db);
    }

    /** Atténuation acoustique d'un bloc (dB). Renvoyer {@code null} pour laisser la règle suivante décider. */
    @FunctionalInterface
    public interface BlockAcoustics {
        @Nullable
        Float attenuation(ServerWorld world, BlockPos pos, BlockState state);
    }

    /** Source de bruit de fond supplémentaire (blizzard, machine, foule…). Renvoyer {@code Float.NaN} si rien. */
    @FunctionalInterface
    public interface AmbientSource {
        float level(ServerWorld world, BlockPos pos);
    }

    public static final HearingRules DEFAULT = new Builder().build();

    // Propagation
    public final float airAbsorptionPerBlock;
    public final float maxOcclusionDb;
    public final float deafeningDb;
    // Bruit de fond
    public final float caveDb;
    public final float surfaceDb;
    public final float rainDb;
    public final float thunderstormDb;
    public final float flowingWaterDb;
    public final float lavaDb;
    // Matériaux
    public final float muffledDb;
    public final float closedDoorDb;
    public final float openDoorDb;
    public final float leavesDb;
    public final float solidBaseDb;
    public final float hardnessFactor;
    public final float unbreakableDb;
    public final float thinSolidDb;
    public final float waterDb;
    public final float lavaBlockDb;
    // Points d'extension
    public final List<NoiseModifier> noiseModifiers;
    public final List<BlockAcoustics> blockAcoustics;
    public final List<AmbientSource> ambientSources;

    private HearingRules(Builder b) {
        airAbsorptionPerBlock = b.airAbsorptionPerBlock;
        maxOcclusionDb = b.maxOcclusionDb;
        deafeningDb = b.deafeningDb;
        caveDb = b.caveDb;
        surfaceDb = b.surfaceDb;
        rainDb = b.rainDb;
        thunderstormDb = b.thunderstormDb;
        flowingWaterDb = b.flowingWaterDb;
        lavaDb = b.lavaDb;
        muffledDb = b.muffledDb;
        closedDoorDb = b.closedDoorDb;
        openDoorDb = b.openDoorDb;
        leavesDb = b.leavesDb;
        solidBaseDb = b.solidBaseDb;
        hardnessFactor = b.hardnessFactor;
        unbreakableDb = b.unbreakableDb;
        thinSolidDb = b.thinSolidDb;
        waterDb = b.waterDb;
        lavaBlockDb = b.lavaBlockDb;
        noiseModifiers = List.copyOf(b.noiseModifiers);
        blockAcoustics = List.copyOf(b.blockAcoustics);
        ambientSources = List.copyOf(b.ambientSources);
    }

    /** Applique les modificateurs ; NaN = bruit ignoré par cet auditeur. */
    public float modifyNoise(NoiseEvent event) {
        float db = event.db();
        for (NoiseModifier modifier : noiseModifiers) {
            db = modifier.modify(event, db);
            if (Float.isNaN(db)) {
                return db;
            }
        }
        return db;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private float airAbsorptionPerBlock = 0.35f;
        private float maxOcclusionDb = 60f;
        private float deafeningDb = 85f;
        private float caveDb = 12f;
        private float surfaceDb = 18f;
        private float rainDb = 42f;
        private float thunderstormDb = 50f;
        private float flowingWaterDb = 38f;
        private float lavaDb = 30f;
        private float muffledDb = 18f;
        private float closedDoorDb = 7f;
        private float openDoorDb = 0.5f;
        private float leavesDb = 1.5f;
        private float solidBaseDb = 9f;
        private float hardnessFactor = 0.2f;
        private float unbreakableDb = 25f;
        private float thinSolidDb = 3.5f;
        private float waterDb = 4f;
        private float lavaBlockDb = 6f;
        private final List<NoiseModifier> noiseModifiers = new ArrayList<>();
        private final List<BlockAcoustics> blockAcoustics = new ArrayList<>();
        private final List<AmbientSource> ambientSources = new ArrayList<>();

        public Builder() {
        }

        private Builder(HearingRules r) {
            airAbsorptionPerBlock = r.airAbsorptionPerBlock;
            maxOcclusionDb = r.maxOcclusionDb;
            deafeningDb = r.deafeningDb;
            caveDb = r.caveDb;
            surfaceDb = r.surfaceDb;
            rainDb = r.rainDb;
            thunderstormDb = r.thunderstormDb;
            flowingWaterDb = r.flowingWaterDb;
            lavaDb = r.lavaDb;
            muffledDb = r.muffledDb;
            closedDoorDb = r.closedDoorDb;
            openDoorDb = r.openDoorDb;
            leavesDb = r.leavesDb;
            solidBaseDb = r.solidBaseDb;
            hardnessFactor = r.hardnessFactor;
            unbreakableDb = r.unbreakableDb;
            thinSolidDb = r.thinSolidDb;
            waterDb = r.waterDb;
            lavaBlockDb = r.lavaBlockDb;
            noiseModifiers.addAll(r.noiseModifiers);
            blockAcoustics.addAll(r.blockAcoustics);
            ambientSources.addAll(r.ambientSources);
        }

        /** dB perdus par bloc parcouru dans l'air (0,35 par défaut). */
        public Builder airAbsorption(float dbPerBlock) { this.airAbsorptionPerBlock = dbPerBlock; return this; }
        /** Atténuation maximale due aux obstacles (60 dB). */
        public Builder maxOcclusion(float db) { this.maxOcclusionDb = db; return this; }
        /** Au-delà, un bruit assourdit temporairement (85 dB). {@code Float.MAX_VALUE} pour désactiver. */
        public Builder deafening(float db) { this.deafeningDb = db; return this; }

        /** Bruit de fond : grotte, surface, pluie, orage. */
        public Builder ambient(float cave, float surface, float rain, float thunderstorm) {
            this.caveDb = cave; this.surfaceDb = surface; this.rainDb = rain; this.thunderstormDb = thunderstorm;
            return this;
        }
        /** Bruit de fond de l'eau qui coule et de la lave (base, avant quantité). */
        public Builder fluidAmbient(float flowingWater, float lava) {
            this.flowingWaterDb = flowingWater; this.lavaDb = lava; return this;
        }
        /** Pluie silencieuse pour cet auditeur (ex. créature qui « entend à travers »). */
        public Builder ignoreRain() { this.rainDb = 0f; this.thunderstormDb = 0f; return this; }

        /** Matériaux absorbants (laine, tapis) : 18 dB. */
        public Builder muffled(float db) { this.muffledDb = db; return this; }
        public Builder doors(float closed, float open) { this.closedDoorDb = closed; this.openDoorDb = open; return this; }
        public Builder leaves(float db) { this.leavesDb = db; return this; }
        /** Blocs pleins : base + dureté × facteur ; incassables. */
        public Builder solids(float base, float hardnessFactor, float unbreakable) {
            this.solidBaseDb = base; this.hardnessFactor = hardnessFactor; this.unbreakableDb = unbreakable; return this;
        }
        /** Blocs fins (verre, dalles, barrières). */
        public Builder thinSolids(float db) { this.thinSolidDb = db; return this; }
        public Builder fluids(float water, float lava) { this.waterDb = water; this.lavaBlockDb = lava; return this; }

        public Builder addNoiseModifier(NoiseModifier modifier) { noiseModifiers.add(modifier); return this; }
        /** Prioritaire sur les règles de matériaux intégrées, dans l'ordre d'ajout. */
        public Builder addBlockAcoustics(BlockAcoustics acoustics) { blockAcoustics.add(acoustics); return this; }
        public Builder addAmbientSource(AmbientSource source) { ambientSources.add(source); return this; }

        public HearingRules build() {
            return new HearingRules(this);
        }
    }
}
