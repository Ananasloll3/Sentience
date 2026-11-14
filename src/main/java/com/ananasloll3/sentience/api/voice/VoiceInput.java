package com.ananasloll3.sentience.api.voice;

import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.ananasloll3.sentience.impl.debug.SentienceDebug;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entrée micro des joueurs, convertie en bruits de type {@link NoiseType#VOICE}.
 * Alimentée automatiquement par Simple Voice Chat s'il est installé ; n'importe quel autre mod
 * de voix peut appeler {@link #submit} (depuis n'importe quel thread) pour être pris en charge.
 */
public final class VoiceInput {

    /** En dessous : bruit du micro, souffle… on ignore. */
    public static final float NOISE_GATE_DBFS = -50f;
    /** Toutes les 4 ticks (200 ms) on agrège les paquets en un seul bruit. */
    private static final int FLUSH_INTERVAL = 4;
    /** Niveau d'une voix normale dans le monde. */
    private static final float NORMAL_SPEECH_DB = 60f;
    private static final float WHISPER_PENALTY_DB = 15f;

    private static final Map<UUID, Level> LEVELS = new ConcurrentHashMap<>();

    private VoiceInput() {
    }

    /**
     * Niveau d'un paquet audio, thread quelconque.
     *
     * @param dbfs       niveau RMS en dBFS (0 = saturation)
     * @param whispering le joueur chuchote (mode murmure du mod de voix)
     */
    public static void submit(UUID player, float dbfs, boolean whispering) {
        if (dbfs < NOISE_GATE_DBFS) {
            return;
        }
        Level level = LEVELS.computeIfAbsent(player, id -> new Level());
        synchronized (level) {
            level.peakDbfs = Math.max(level.peakDbfs, dbfs);
            level.whispering |= whispering;
        }
    }

    public static void forget(UUID player) {
        LEVELS.remove(player);
    }

    /** Thread serveur, appelé par Sentience. */
    public static void tick(MinecraftServer server) {
        if (LEVELS.isEmpty() || server.getTicks() % FLUSH_INTERVAL != 0) {
            return;
        }
        for (Map.Entry<UUID, Level> entry : LEVELS.entrySet()) {
            float peak;
            boolean whisper;
            Level level = entry.getValue();
            synchronized (level) {
                peak = level.peakDbfs;
                whisper = level.whispering;
                level.peakDbfs = Float.NEGATIVE_INFINITY;
                level.whispering = false;
            }
            if (peak == Float.NEGATIVE_INFINITY) {
                continue;
            }
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null || player.isSpectator()) {
                continue;
            }

            // Auto-calibration lente : chaque micro a son gain, on raisonne en relatif
            level.speechAverage += (peak - level.speechAverage) * 0.02f;
            level.speechAverage = MathHelper.clamp(level.speechAverage, -40f, -10f);

            float worldDb = NORMAL_SPEECH_DB + (peak - level.speechAverage) * 1.3f;
            if (whisper) {
                worldDb -= WHISPER_PENALTY_DB;
            }
            worldDb = MathHelper.clamp(worldDb, 25f, 100f);

            HearingSystem.report(player.getEntityWorld(), player.getEyePos(), worldDb, NoiseType.VOICE, player);
            SentienceDebug.onVoice(player, peak, worldDb);
        }
    }

    private static final class Level {
        float peakDbfs = Float.NEGATIVE_INFINITY;
        boolean whispering;
        float speechAverage = -24f;
    }
}
