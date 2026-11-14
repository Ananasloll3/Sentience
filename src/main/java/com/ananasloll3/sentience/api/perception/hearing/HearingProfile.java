package com.ananasloll3.sentience.api.perception.hearing;

/**
 * Caractéristiques de l'oreille d'une créature.
 *
 * @param thresholdDb       seuil d'audition absolu (plus bas = oreille plus fine)
 * @param maxRange          distance maximale d'écoute, en blocs (borne de performance)
 * @param localizationSkill 1 = normal, 2 = localise deux fois plus précisément
 */
public record HearingProfile(float thresholdDb, double maxRange, double localizationSkill) {

    public static final HearingProfile DEFAULT = new HearingProfile(15f, 64, 1.0);
    /** Oreille de prédateur : fine, porte loin, localise bien. */
    public static final HearingProfile KEEN = new HearingProfile(10f, 80, 1.4);
    /** Créature distraite ou bruyante elle-même. */
    public static final HearingProfile DULL = new HearingProfile(25f, 40, 0.7);

    public HearingProfile withThreshold(float db) {
        return new HearingProfile(db, maxRange, localizationSkill);
    }

    public HearingProfile withMaxRange(double range) {
        return new HearingProfile(thresholdDb, range, localizationSkill);
    }

    public HearingProfile withLocalization(double skill) {
        return new HearingProfile(thresholdDb, maxRange, skill);
    }
}
