package com.ananasloll3.sentience.api.rules;

import com.ananasloll3.sentience.Sentience;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

/**
 * Jeu de règles de perception d'un mod. Chaque mod crée le sien et l'attache à SES agents :
 * les règles s'appliquent côté perceveur, donc les mods ne se marchent jamais dessus.
 *
 * <pre>{@code
 * public static final SentienceRules RULES = SentienceRules.create("monmod")
 *     .scent(s -> s.ignoreRain())
 *     .sight(s -> s.addStealthModifier((observer, target) -> wearsCamo(target) ? 0.3f : 1f))
 *     .build();
 *
 * Agent.builder(this).rules(RULES).hearing(...).build();
 * }</pre>
 *
 * Un agent sans {@code .rules(...)} utilise {@link #DEFAULT}, qui ne change jamais.
 */
public final class SentienceRules {

    public static final SentienceRules DEFAULT =
            new SentienceRules(Sentience.id("default"), HearingRules.DEFAULT, SightRules.DEFAULT, ScentRules.DEFAULT);

    private final Identifier id;
    private final HearingRules hearing;
    private final SightRules sight;
    private final ScentRules scent;

    private SentienceRules(Identifier id, HearingRules hearing, SightRules sight, ScentRules scent) {
        this.id = id;
        this.hearing = hearing;
        this.sight = sight;
        this.scent = scent;
    }

    /** Nouveau jeu de règles pour ton mod, partant des valeurs par défaut. */
    public static Builder create(String modId) {
        return new Builder(Identifier.of(modId, "rules"), DEFAULT);
    }

    /** Nouveau jeu de règles nommé (plusieurs jeux par mod : « monmod:boss », « monmod:animaux »…). */
    public static Builder create(Identifier id) {
        return new Builder(id, DEFAULT);
    }

    /** Variante d'un jeu existant, pour un mob particulier. */
    public Builder derive(Identifier id) {
        return new Builder(id, this);
    }

    public Identifier id() {
        return id;
    }

    public HearingRules hearing() {
        return hearing;
    }

    public SightRules sight() {
        return sight;
    }

    public ScentRules scent() {
        return scent;
    }

    @Override
    public String toString() {
        return "SentienceRules[" + id + "]";
    }

    public static final class Builder {
        private final Identifier id;
        private HearingRules.Builder hearing;
        private SightRules.Builder sight;
        private ScentRules.Builder scent;

        private Builder(Identifier id, SentienceRules base) {
            this.id = id;
            this.hearing = base.hearing.toBuilder();
            this.sight = base.sight.toBuilder();
            this.scent = base.scent.toBuilder();
        }

        public Builder hearing(UnaryOperator<HearingRules.Builder> edit) {
            hearing = edit.apply(hearing);
            return this;
        }

        public Builder sight(UnaryOperator<SightRules.Builder> edit) {
            sight = edit.apply(sight);
            return this;
        }

        public Builder scent(UnaryOperator<ScentRules.Builder> edit) {
            scent = edit.apply(scent);
            return this;
        }

        public SentienceRules build() {
            return new SentienceRules(id, hearing.build(), sight.build(), scent.build());
        }
    }
}
