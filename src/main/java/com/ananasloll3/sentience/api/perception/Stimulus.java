package com.ananasloll3.sentience.api.perception;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Un souvenir de perception : « il y a quelque chose, à peu près là ».
 * Les perceptions proches (ou de la même source) sont fusionnées : la position s'affine,
 * la priorité monte, et le stimulus garde la trace des sens qui l'ont confirmé.
 */
public final class Stimulus {

    /** Demi-vie de la priorité, en ticks. */
    public static final double PRIORITY_HALF_LIFE = 160.0;

    private Vec3d position;
    private double errorRadius;
    private float lastScore;
    private float peakIntensity;
    private SenseType lastSense;
    private String kind;
    private final Set<SenseType> senses = new HashSet<>();
    @Nullable
    private UUID sourceId;
    private final long firstTick;
    private long lastTick;
    private int count;

    Stimulus(StimulusInput in, long tick) {
        this.position = in.position();
        this.errorRadius = Math.max(0.25, in.errorRadius());
        this.lastScore = in.score();
        this.peakIntensity = in.intensity();
        this.lastSense = in.sense();
        this.kind = in.kind();
        this.senses.add(in.sense());
        this.sourceId = in.sourceId();
        this.firstTick = tick;
        this.lastTick = tick;
        this.count = 1;
    }

    /** Fusion bayésienne simple : chaque estimation est pondérée par 1/σ², l'ancienne vieillit. */
    void merge(StimulusInput in, long tick) {
        double age = tick - lastTick;
        double newError = Math.max(0.25, in.errorRadius());
        boolean sameSource = in.sourceId() != null && in.sourceId().equals(sourceId);

        if (sameSource && position.distanceTo(in.position()) > errorRadius + newError) {
            // La source identifiée s'est déplacée : la nouvelle mesure remplace l'ancienne
            // (une moyenne laisserait l'agent viser un point périmé, en retard sur sa cible)
            position = in.position();
            errorRadius = newError;
        } else {
            double oldWeight = 1.0 / (errorRadius * errorRadius) * Math.pow(0.5, age / 40.0);
            if (sameSource) {
                oldWeight *= 0.35; // source qui bouge peu : la mesure récente compte davantage
            }
            double newWeight = 1.0 / (newError * newError);
            double sum = oldWeight + newWeight;
            position = position.multiply(oldWeight / sum).add(in.position().multiply(newWeight / sum));
            errorRadius = Math.max(0.25, 1.0 / Math.sqrt(sum));
        }
        // On compare au score de base vieilli (sans bonus de répétition, sinon il s'auto-alimente)
        float decayedBase = (float) (lastScore * Math.pow(0.5, age / PRIORITY_HALF_LIFE));
        if (in.score() >= decayedBase) {
            kind = in.kind();
            lastSense = in.sense();
        }
        lastScore = Math.max(in.score(), decayedBase);
        peakIntensity = Math.max(peakIntensity, in.intensity());
        senses.add(in.sense());
        if (in.sourceId() != null) {
            sourceId = in.sourceId();
        }
        lastTick = tick;
        count++;
    }

    boolean canMerge(StimulusInput in, long tick) {
        if (tick - lastTick > 100) {
            return false;
        }
        if (in.sourceId() != null && in.sourceId().equals(sourceId)) {
            return true;
        }
        double gate = Math.max(errorRadius, in.errorRadius()) * 1.5 + 2.0;
        return position.squaredDistanceTo(in.position()) <= gate * gate;
    }

    /** Priorité actuelle : dernier score atténué par l'âge, plus un bonus de répétition. */
    public float getScore(long now) {
        double decay = Math.pow(0.5, (now - lastTick) / PRIORITY_HALF_LIFE);
        return (float) ((lastScore + Math.min(count - 1, 5) * 1.5) * decay);
    }

    /** Position estimée (jamais la vraie position, sauf perception parfaite). */
    public Vec3d getPosition() {
        return position;
    }

    public double getErrorRadius() {
        return errorRadius;
    }

    /** dB pour l'ouïe, visibilité ×100 pour la vue, dégâts pour les coups… */
    public float getPeakIntensity() {
        return peakIntensity;
    }

    public SenseType getLastSense() {
        return lastSense;
    }

    public boolean wasSensedBy(SenseType sense) {
        return senses.contains(sense);
    }

    public Set<SenseType> getSenses() {
        return Collections.unmodifiableSet(senses);
    }

    /** Détail libre fourni par le sens : « voice », « footstep », « seen », « glimpse »… */
    public String getKind() {
        return kind;
    }

    @Nullable
    public UUID getSourceId() {
        return sourceId;
    }

    public long getFirstTick() {
        return firstTick;
    }

    public long getLastTick() {
        return lastTick;
    }

    public long getAge(long now) {
        return now - lastTick;
    }

    public int getCount() {
        return count;
    }
}
