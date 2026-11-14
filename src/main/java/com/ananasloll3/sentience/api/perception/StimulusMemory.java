package com.ananasloll3.sentience.api.perception;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Mémoire perceptive d'un agent, commune à tous les sens. */
public final class StimulusMemory {

    private static final float FORGET_SCORE = 0.3f;

    private final List<Stimulus> stimuli = new ArrayList<>();
    private final int capacity;

    public StimulusMemory(int capacity) {
        this.capacity = capacity;
    }

    /** Ajoute ou fusionne une perception. Renvoie le stimulus mis à jour. */
    public Stimulus remember(StimulusInput input, long now) {
        for (Stimulus s : stimuli) {
            if (s.canMerge(input, now)) {
                s.merge(input, now);
                return s;
            }
        }
        if (stimuli.size() >= capacity) {
            stimuli.sort((a, b) -> Float.compare(a.getScore(now), b.getScore(now)));
            stimuli.removeFirst();
        }
        Stimulus created = new Stimulus(input, now);
        stimuli.add(created);
        return created;
    }

    public void tick(long now, int memoryTicks) {
        stimuli.removeIf(s -> now - s.getLastTick() > memoryTicks || s.getScore(now) < FORGET_SCORE);
    }

    @Nullable
    public Stimulus best(long now) {
        Stimulus best = null;
        float bestScore = 0f;
        for (Stimulus s : stimuli) {
            float score = s.getScore(now);
            if (score > bestScore) {
                best = s;
                bestScore = score;
            }
        }
        return best;
    }

    @Nullable
    public Stimulus findBySource(UUID source) {
        for (Stimulus s : stimuli) {
            if (source.equals(s.getSourceId())) {
                return s;
            }
        }
        return null;
    }

    public void forget(Stimulus stimulus) {
        stimuli.remove(stimulus);
    }

    public void forgetNear(Vec3d pos, double radius) {
        double r2 = radius * radius;
        stimuli.removeIf(s -> s.getPosition().squaredDistanceTo(pos) <= r2);
    }

    public void clear() {
        stimuli.clear();
    }

    public List<Stimulus> view() {
        return Collections.unmodifiableList(stimuli);
    }
}
