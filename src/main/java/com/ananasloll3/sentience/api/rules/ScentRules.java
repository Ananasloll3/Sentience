package com.ananasloll3.sentience.api.rules;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.perception.scent.ScentTrails;

import java.util.ArrayList;
import java.util.List;

/**
 * Règles de l'odorat, appliquées côté RENIFLEUR : les pistes sont enregistrées une seule fois
 * (avec l'eau et la météo notées), chaque agent les interprète avec ses propres règles.
 */
public final class ScentRules {

    /**
     * Facteur appliqué à l'intensité d'un point pour un agent (0 = ignoré).
     * Ex. : un mod de « potion anti-odeur » ou un prédateur qui préfère une proie précise.
     */
    @FunctionalInterface
    public interface PointModifier {
        float factor(Agent agent, ScentTrails.ScentPoint point);
    }

    /** Durée maximale de conservation des pistes par Sentience, quelles que soient les règles. */
    public static final int MAX_RETENTION_TICKS = 12000;

    public static final ScentRules DEFAULT = new Builder().build();

    public final int lifetimeTicks;
    public final float rainLifetimeFactor;
    public final boolean waterBreaksTrail;
    public final float sneakStrength;
    public final float bloodFactor;
    public final int maxWallBlocks;
    public final List<PointModifier> pointModifiers;

    private ScentRules(Builder b) {
        lifetimeTicks = b.lifetimeTicks;
        rainLifetimeFactor = b.rainLifetimeFactor;
        waterBreaksTrail = b.waterBreaksTrail;
        sneakStrength = b.sneakStrength;
        bloodFactor = b.bloodFactor;
        maxWallBlocks = b.maxWallBlocks;
        pointModifiers = List.copyOf(b.pointModifiers);
    }

    /** Produit des modificateurs de cet agent pour un point. */
    public float modifierFactor(Agent agent, ScentTrails.ScentPoint point) {
        float factor = 1f;
        for (PointModifier modifier : pointModifiers) {
            factor *= modifier.factor(agent, point);
        }
        return factor;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private int lifetimeTicks = 2400;
        private float rainLifetimeFactor = 0.5f;
        private boolean waterBreaksTrail = true;
        private float sneakStrength = 1f;
        private float bloodFactor = 1f;
        private int maxWallBlocks = 1;
        private final List<PointModifier> pointModifiers = new ArrayList<>();

        public Builder() {
        }

        private Builder(ScentRules r) {
            lifetimeTicks = r.lifetimeTicks;
            rainLifetimeFactor = r.rainLifetimeFactor;
            waterBreaksTrail = r.waterBreaksTrail;
            sneakStrength = r.sneakStrength;
            bloodFactor = r.bloodFactor;
            maxWallBlocks = r.maxWallBlocks;
            pointModifiers.addAll(r.pointModifiers);
        }

        /** Durée de vie d'une trace (2 min, max {@value #MAX_RETENTION_TICKS} ticks). */
        public Builder lifetime(int ticks) {
            this.lifetimeTicks = Math.min(ticks, MAX_RETENTION_TICKS);
            return this;
        }

        /** Durée de vie sous la pluie, en fraction (0,5 = deux fois plus courte, 1 = la pluie ne change rien). */
        public Builder rainLifetime(float factor) { this.rainLifetimeFactor = factor; return this; }

        /** La pluie n'efface pas la piste. */
        public Builder ignoreRain() { this.rainLifetimeFactor = 1f; return this; }

        /** L'eau coupe la piste (vrai par défaut). */
        public Builder waterBreaksTrail(boolean value) { this.waterBreaksTrail = value; return this; }

        /** Force d'une trace laissée accroupi (1 = comme debout). */
        public Builder sneakStrength(float value) { this.sneakStrength = value; return this; }

        /** Attrait du sang (1 = normal, 3 = requin, 0 = l'ignore). */
        public Builder blood(float factor) { this.bloodFactor = factor; return this; }

        /**
         * Nombre de blocs pleins que l'odeur peut traverser entre le nez et la trace (1 par défaut :
         * une porte ou un mur fin, pas un bunker). -1 pour ignorer les murs.
         */
        public Builder maxWallBlocks(int blocks) { this.maxWallBlocks = blocks; return this; }

        public Builder addPointModifier(PointModifier modifier) { pointModifiers.add(modifier); return this; }

        public ScentRules build() {
            return new ScentRules(this);
        }
    }
}
