package com.ananasloll3.sentience.impl.sound;

import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Convertit un son vanilla (ou moddé) en bruit physique : type + intensité en dB.
 * Classement par mots-clés dans l'identifiant, ce qui couvre aussi la plupart des sons moddés.
 */
public final class SoundLoudness {

    public record Loudness(NoiseType type, float db) {
    }

    private SoundLoudness() {
    }

    @Nullable
    public static Loudness classify(Identifier id, SoundCategory category, float volume, @Nullable Entity source) {
        switch (category) {
            case MUSIC, RECORDS, WEATHER, AMBIENT, VOICE, UI -> {
                return null;
            }
            default -> {
            }
        }

        String path = id.getPath();
        if (path.startsWith("ui.") || path.startsWith("music") || path.contains("ambient.cave")
                || path.startsWith("weather.") || path.contains("thunder")) {
            return null;
        }

        Loudness base = byPath(path, category);
        float db = base.db();

        if (base.type() == NoiseType.FOOTSTEP) {
            // Vanilla joue tous les pas à ~0,15 de volume : valeur de mixage, pas une intensité physique.
            // On ignore donc le volume et on tient compte de l'allure.
            if (source instanceof LivingEntity living) {
                if (living.isSprinting()) {
                    db += 6f;
                } else if (living.isSneaking()) {
                    db -= 14f;
                }
            }
        } else {
            // Le volume vanilla est un réglage de mixage : on n'en garde qu'une partie en dessous de 1
            double v = Math.max(volume, 0.05f);
            db += (float) (v < 1.0 ? 10.0 * Math.log10(v) : 20.0 * Math.log10(v));
        }
        return new Loudness(base.type(), db);
    }

    private static Loudness byPath(String p, SoundCategory category) {
        // --- Très fort
        if (p.contains("explode") || p.contains("explosion")) return of(NoiseType.EXPLOSION, 125);
        if (p.contains("raid.horn")) return of(NoiseType.MECHANISM, 110);
        if (p.contains("goat_horn") || p.endsWith(".horn")) return of(NoiseType.MECHANISM, 105);
        if (p.contains("firework") && p.contains("blast")) return of(NoiseType.EXPLOSION, 100);
        if (p.contains("bell")) return of(NoiseType.MECHANISM, 95);
        if (p.contains("anvil")) return of(NoiseType.BLOCK, 85);
        if (p.contains("roar") || p.contains("scream") || p.contains("sonic_boom")) return of(NoiseType.CREATURE, 90);

        // --- Déplacements
        if (p.endsWith(".step")) {
            return of(NoiseType.FOOTSTEP, p.contains("wool") || p.contains("moss") || p.contains("snow") ? 42 : 50);
        }
        if (p.contains("big_fall")) return of(NoiseType.FOOTSTEP, 64);
        if (p.contains("small_fall") || p.endsWith(".fall")) return of(NoiseType.FOOTSTEP, 56);
        if (p.contains("splash")) return of(NoiseType.FOOTSTEP, 60);
        if (p.contains("swim")) return of(NoiseType.FOOTSTEP, 50);

        // --- Mécanismes
        if (p.contains("iron_door") || p.contains("iron_trapdoor")) return of(NoiseType.MECHANISM, 66);
        if (p.contains("door") || p.contains("fence_gate")) return of(NoiseType.MECHANISM, 58);
        if (p.contains("piston")) return of(NoiseType.MECHANISM, 65);
        if (p.contains("note_block")) return of(NoiseType.MECHANISM, 75);
        if (p.contains("dispenser") || p.contains("dropper")) return of(NoiseType.MECHANISM, 55);
        if (p.contains("chest") || p.contains("barrel") || p.contains("shulker_box")) return of(NoiseType.MECHANISM, 50);
        if (p.contains("lever") || p.contains("button") || p.contains("pressure_plate")) return of(NoiseType.MECHANISM, 45);

        // --- Combat & projectiles
        if (p.contains("shield")) return of(NoiseType.COMBAT, 65);
        if (p.contains(".attack")) return of(NoiseType.COMBAT, 62);
        if (p.endsWith(".death")) return of(NoiseType.COMBAT, 70);
        if (p.endsWith(".hurt") || p.contains(".hurt_")) return of(NoiseType.COMBAT, 65);
        if (p.contains("arrow.hit") || p.contains("trident.hit")) return of(NoiseType.PROJECTILE, 52);
        if (p.contains("shoot") || p.contains("arrow") || p.contains("throw") || p.contains("crossbow")
                || p.contains("trident") || p.contains("snowball")) return of(NoiseType.PROJECTILE, 55);

        // --- Blocs
        if (p.startsWith("block.")) {
            if (p.endsWith(".break")) return of(NoiseType.BLOCK, 62);
            if (p.endsWith(".place")) return of(NoiseType.BLOCK, 52);
            if (p.endsWith(".hit")) return of(NoiseType.BLOCK, 48);
            return of(NoiseType.BLOCK, 50);
        }

        // --- Objets
        if (p.contains("item.break")) return of(NoiseType.ITEM, 55);
        if (p.contains("eat") || p.contains("drink") || p.contains("burp")) return of(NoiseType.ITEM, 40);
        if (p.contains("pickup")) return of(NoiseType.ITEM, 30);
        if (p.contains("equip") || p.contains("bucket") || p.contains("flintandsteel")) return of(NoiseType.ITEM, 44);
        if (p.startsWith("item.")) return of(NoiseType.ITEM, 45);

        // --- Créatures (grognements, bêlements…)
        if (p.startsWith("entity.player.")) return of(NoiseType.GENERIC, 45);
        if (p.startsWith("entity.")) return of(NoiseType.CREATURE, p.contains("ambient") ? 55 : 50);

        return of(category == SoundCategory.BLOCKS ? NoiseType.BLOCK : NoiseType.GENERIC, 48);
    }

    private static Loudness of(NoiseType type, float db) {
        return new Loudness(type, db);
    }
}
