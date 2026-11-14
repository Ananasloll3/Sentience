package com.ananasloll3.sentience.impl.voice;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.voice.VoiceInput;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin Simple Voice Chat (entrypoint « voicechat »). Chargé uniquement si le mod est installé.
 * Décode la voix de chaque joueur côté serveur et en mesure le volume.
 */
public final class SimpleVoiceChatPlugin implements VoicechatPlugin {

    private final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return Sentience.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onDisconnect);
        Sentience.LOGGER.info("Simple Voice Chat détecté : les créatures entendront les micros.");
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null) {
            return;
        }
        UUID id = sender.getPlayer().getUuid();
        byte[] data = event.getPacket().getOpusEncodedData();
        if (data == null || data.length == 0) {
            return; // fin de transmission
        }

        // Opus garde un état entre paquets : un décodeur par joueur
        OpusDecoder decoder = decoders.computeIfAbsent(id, u -> {
            Sentience.LOGGER.info("Voix reçue pour le joueur {} : le micro est écouté.", u);
            return event.getVoicechat().createDecoder();
        });
        short[] pcm;
        synchronized (decoder) {
            pcm = decoder.decode(data);
        }
        VoiceInput.submit(id, toDbfs(pcm), event.getPacket().isWhispering());
    }

    private void onDisconnect(PlayerDisconnectedEvent event) {
        OpusDecoder decoder = decoders.remove(event.getPlayerUuid());
        if (decoder != null) {
            decoder.close();
        }
        VoiceInput.forget(event.getPlayerUuid());
    }

    /** Niveau RMS en dBFS (0 = saturation, -∞ = silence). */
    static float toDbfs(short[] pcm) {
        if (pcm == null || pcm.length == 0) {
            return Float.NEGATIVE_INFINITY;
        }
        double sum = 0;
        for (short s : pcm) {
            double v = s / 32768.0;
            sum += v * v;
        }
        double rms = Math.sqrt(sum / pcm.length);
        return rms <= 1.0E-6 ? Float.NEGATIVE_INFINITY : (float) (20.0 * Math.log10(rms));
    }
}
