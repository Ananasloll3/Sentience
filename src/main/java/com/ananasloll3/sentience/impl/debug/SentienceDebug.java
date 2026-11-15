package com.ananasloll3.sentience.impl.debug;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.awareness.Awareness;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseType;
import com.ananasloll3.sentience.api.perception.Stimulus;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.ananasloll3.sentience.api.perception.hearing.PerceivedNoise;
import com.ananasloll3.sentience.api.perception.scent.ScentSense;
import com.ananasloll3.sentience.api.perception.sight.SightSense;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;
import com.ananasloll3.sentience.api.behavior.BehaviorTreeComponent;
import com.ananasloll3.sentience.api.query.QueryResult;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Visualisation pour régler les IA (/sentience debug) :
 * état de chaque agent au-dessus de sa tête, particules là où il CROIT avoir perçu quelque chose,
 * niveau de micro dans la barre d'action. Les vrais noms des mobs sont restaurés en sortie de debug.
 */
public final class SentienceDebug {

    /** Marqueur invisible posé sur les noms de debug, pour les reconnaître après une sauvegarde. */
    private static final String DEBUG_MARKER = "sentience:debug";

    private static boolean enabled;

    private SentienceDebug() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    // ------------------------------------------------------------------ ouïe & voix

    public static void onNoiseHeard(Entity listener, PerceivedNoise noise) {
        if (!enabled || !(listener.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }
        DustParticleEffect dust = new DustParticleEffect(colorOf(noise.event().type()), 1.6f);
        Vec3d p = noise.estimatedPos();
        double spread = Math.min(noise.errorRadius(), 6.0) * 0.35;
        world.spawnParticles(dust, p.x, p.y, p.z, 8, spread, 0.2, spread, 0.0);
    }

    public static void onVoice(ServerPlayerEntity player, float dbfs, float worldDb) {
        if (!enabled) {
            return;
        }
        player.sendMessage(Text.literal(String.format("🎤 micro %.0f dBFS → %.0f dB dans le monde", dbfs, worldDb))
                .formatted(Formatting.AQUA), true);
    }

    // ------------------------------------------------------------------ nom des agents

    /** Ce qu'on a remplacé sur le mob, pour le restaurer. */
    public static final class NameState {
        boolean active;
        @Nullable
        Text savedName;
        boolean savedVisible;
    }

    /** Un nom de debug sauvegardé avec le monde ne doit pas survivre au rechargement. */
    public static void clearStaleName(MobEntity mob) {
        Text name = mob.getCustomName();
        if (name != null && DEBUG_MARKER.equals(name.getStyle().getInsertion())) {
            mob.setCustomName(null);
            mob.setCustomNameVisible(false);
        }
    }

    public static void updateAgentName(Agent agent, NameState state) {
        MobEntity mob = agent.mob();
        if (enabled) {
            if (!state.active) {
                state.savedName = mob.getCustomName();
                state.savedVisible = mob.isCustomNameVisible();
                state.active = true;
            }
            mob.setCustomName(describe(agent));
            mob.setCustomNameVisible(true);
        } else if (state.active) {
            mob.setCustomName(state.savedName);
            mob.setCustomNameVisible(state.savedVisible);
            state.active = false;
            state.savedName = null;
        }
    }

    private static Text describe(Agent agent) {
        Awareness awareness = agent.awarenessState();
        Formatting color = switch (awareness) {
            case IDLE -> Formatting.GRAY;
            case SUSPICIOUS -> Formatting.YELLOW;
            case INVESTIGATE -> Formatting.GOLD;
            case HUNT -> Formatting.RED;
        };
        MutableText text = Text.literal(awareness.name()).formatted(color, Formatting.BOLD)
                .styled(style -> style.withInsertion(DEBUG_MARKER));

        long now = agent.mob().getEntityWorld().getTime();
        Stimulus focus = agent.awareness() != null ? agent.awareness().focus() : null;
        if (focus != null) {
            text.append(Text.literal(String.format("  %s %.1f ±%.1f", senseIcon(focus.getLastSense()),
                    focus.getScore(now), focus.getErrorRadius())).formatted(Formatting.WHITE));
        }
        PerceptionComponent perception = agent.perception();
        SightSense sight = perception == null ? null : perception.sense(SightSense.class);
        if (sight != null && sight.getHighestDetection() > 0f) {
            text.append(Text.literal(String.format("  👁%.0f%%", sight.getHighestDetection() * 100f))
                    .formatted(Formatting.AQUA));
        }
        BehaviorTreeComponent tree = agent.find(BehaviorTreeComponent.class);
        if (tree != null) {
            List<String> path = tree.activePath();
            if (!path.isEmpty()) {
                text.append(Text.literal("  ▶ " + path.getLast()).formatted(Formatting.LIGHT_PURPLE));
            }
        }
        if (agent.thinkInterval() > 1) {
            text.append(Text.literal("  LOD/" + agent.thinkInterval()).formatted(Formatting.DARK_GRAY));
        }
        return text;
    }

    // ------------------------------------------------------------------ requêtes d'environnement

    /** Montre les points évalués : verts = retenus (le meilleur en gros), gris = éliminés. */
    public static void onQuery(ServerWorld world, List<Vec3d> evaluated, QueryResult result) {
        if (!enabled) {
            return;
        }
        java.util.Set<Vec3d> kept = new java.util.HashSet<>();
        result.points().forEach(p -> kept.add(p.pos()));
        DustParticleEffect rejected = new DustParticleEffect(0x777777, 0.8f);
        DustParticleEffect retained = new DustParticleEffect(0x55FF55, 1.0f);
        for (Vec3d p : evaluated) {
            world.spawnParticles(kept.contains(p) ? retained : rejected, p.x, p.y + 0.2, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
        Vec3d best = result.best();
        if (best != null) {
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, best.x, best.y + 0.5, best.z, 12, 0.25, 0.4, 0.25, 0.0);
        }
    }

    private static String senseIcon(SenseType sense) {
        if (sense.equals(SenseType.HEARING)) return "👂";
        if (sense.equals(SenseType.SIGHT)) return "👁";
        if (sense.equals(SenseType.DAMAGE)) return "⚔";
        if (sense.equals(SenseType.TOUCH)) return "✋";
        if (sense.equals(ScentSense.SCENT)) return "👃";
        if (sense.equals(SenseType.REPORT)) return "📣";
        return "•";
    }

    private static int colorOf(NoiseType type) {
        return switch (type) {
            case VOICE -> 0x33CCFF;
            case FOOTSTEP -> 0xFFFFFF;
            case COMBAT -> 0xFF2222;
            case BLOCK -> 0xC08040;
            case MECHANISM -> 0xFFCC00;
            case PROJECTILE -> 0xFF88FF;
            case EXPLOSION -> 0xFF6600;
            case ITEM -> 0x88FF88;
            case CREATURE -> 0x777777;
            case GENERIC -> 0xBBBBBB;
        };
    }
}
