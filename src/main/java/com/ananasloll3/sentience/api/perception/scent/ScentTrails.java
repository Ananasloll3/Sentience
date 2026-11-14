package com.ananasloll3.sentience.api.perception.scent;

import com.ananasloll3.sentience.api.rules.ScentRules;
import com.ananasloll3.sentience.impl.debug.SentienceDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Pistes olfactives du monde. Les joueurs (et les types d'entités enregistrés) laissent un point
 * d'odeur toutes les {@value #DEPOSIT_INTERVAL} ticks ; une créature blessée laisse du sang.
 * Les points sont enregistrés une seule fois avec leurs conditions (eau, accroupi, sang) ;
 * chaque agent les interprète avec SES {@link ScentRules}.
 *
 * <p>Stockage par chunk : une recherche ne parcourt que les chunks autour du nez.</p>
 */
public final class ScentTrails {

    public static final String TRAIL = "trail";
    public static final String BLOOD = "blood";
    public static final String CUSTOM = "custom";

    /** Force d'une goutte de sang (une trace normale vaut 1). */
    public static final float BLOOD_STRENGTH = 2.5f;
    /** En dessous de cette fraction de vie, une créature saigne en marchant. */
    public static final float BLEEDING_HEALTH = 0.4f;
    public static final int DEPOSIT_INTERVAL = 10;

    /**
     * Un point d'odeur.
     *
     * @param strength   1 = trace normale, plus = plus forte (sang)
     * @param sourceType type de l'émetteur, {@code null} pour une odeur custom
     * @param kind       {@link #TRAIL}, {@link #BLOOD}, {@link #CUSTOM} ou une valeur propre à un mod
     */
    public record ScentPoint(Vec3d pos, long tick, float strength, UUID source, @Nullable EntityType<?> sourceType,
                             String kind, boolean inWater, boolean sneaking) {
    }

    private static final Map<ServerWorld, Map<Long, Deque<ScentPoint>>> CHUNKS = new HashMap<>();
    /** Règles permissives pour l'affichage debug (on veut tout voir, y compris dans l'eau). */
    private static final ScentRules DEBUG_RULES = new ScentRules.Builder().waterBreaksTrail(false).build();
    private static final Set<EntityType<?>> EMITTER_TYPES = new HashSet<>();

    private ScentTrails() {
    }

    // ------------------------------------------------------------------ émetteurs

    /**
     * Fait laisser une piste à un type d'entité en plus des joueurs (villageois, animaux, mobs…).
     * Additif : chaque mod ajoute ses types sans retirer ceux des autres.
     */
    public static void addEmitter(EntityType<?> type) {
        EMITTER_TYPES.add(type);
    }

    public static boolean isEmitter(Entity entity) {
        return entity instanceof ServerPlayerEntity || EMITTER_TYPES.contains(entity.getType());
    }

    // ------------------------------------------------------------------ dépôt

    /** Odeur custom à une position (appât, cadavre…). */
    public static void deposit(ServerWorld world, Vec3d pos, float strength, UUID source) {
        deposit(world, new ScentPoint(pos, world.getTime(), strength, source, null, CUSTOM, false, false));
    }

    /** Odeur laissée par une entité, à sa position actuelle. */
    public static void deposit(ServerWorld world, Entity entity, float strength, String kind) {
        deposit(world, new ScentPoint(entity.getEntityPos(), world.getTime(), strength, entity.getUuid(),
                entity.getType(), kind, entity.isTouchingWater(), entity.isSneaking()));
    }

    /** Dépôt brut (le tick peut être dans le passé, pour fabriquer une piste). */
    public static void deposit(ServerWorld world, ScentPoint point) {
        long key = ChunkPos.toLong(MathHelper.floor(point.pos().x) >> 4, MathHelper.floor(point.pos().z) >> 4);
        CHUNKS.computeIfAbsent(world, w -> new HashMap<>())
                .computeIfAbsent(key, k -> new ArrayDeque<>())
                .addLast(point);
    }

    // ------------------------------------------------------------------ lecture

    /** Points d'odeur dans un rayon, encore perceptibles selon les règles données. */
    public static List<ScentPoint> near(ServerWorld world, Vec3d pos, double radius, ScentRules rules) {
        List<ScentPoint> out = new ArrayList<>();
        Map<Long, Deque<ScentPoint>> chunks = CHUNKS.get(world);
        if (chunks == null) {
            return out;
        }
        double r2 = radius * radius;
        long now = world.getTime();
        int minX = MathHelper.floor(pos.x - radius) >> 4;
        int maxX = MathHelper.floor(pos.x + radius) >> 4;
        int minZ = MathHelper.floor(pos.z - radius) >> 4;
        int maxZ = MathHelper.floor(pos.z + radius) >> 4;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                Deque<ScentPoint> points = chunks.get(ChunkPos.toLong(cx, cz));
                if (points == null) {
                    continue;
                }
                for (ScentPoint p : points) {
                    if (rules.waterBreaksTrail && p.inWater()) {
                        continue;
                    }
                    if (now - p.tick() <= rules.lifetimeTicks && p.pos().squaredDistanceTo(pos) <= r2) {
                        out.add(p);
                    }
                }
            }
        }
        return out;
    }

    /** Intensité restante d'un point selon les règles (pluie, accroupi, sang, âge). */
    public static float intensity(ServerWorld world, ScentPoint point, ScentRules rules) {
        double age = world.getTime() - point.tick();
        double lifetime = rules.lifetimeTicks;
        if (world.hasRain(BlockPos.ofFloored(point.pos()).up())) {
            lifetime *= rules.rainLifetimeFactor;
        }
        float strength = point.strength();
        if (point.sneaking()) strength *= rules.sneakStrength;
        if (BLOOD.equals(point.kind())) strength *= rules.bloodFactor;
        return (float) Math.max(0.0, strength * (1.0 - age / Math.max(1.0, lifetime)));
    }

    // ------------------------------------------------------------------ cycle (appelé par Sentience)

    public static void tick(MinecraftServer server) {
        if (server.getTicks() % DEPOSIT_INTERVAL != 0) {
            return;
        }
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                emit(world, player);
            }
            if (!EMITTER_TYPES.isEmpty()) {
                for (Entity entity : world.iterateEntities()) {
                    if (!(entity instanceof ServerPlayerEntity) && EMITTER_TYPES.contains(entity.getType())) {
                        emit(world, entity);
                    }
                }
            }
            cleanup(world);
            if (SentienceDebug.isEnabled()) {
                showTrails(world);
            }
        }
    }

    private static void emit(ServerWorld world, Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()
                || (entity instanceof ServerPlayerEntity p && p.getAbilities().flying)) {
            return;
        }
        deposit(world, entity, 1f, TRAIL);
        if (entity instanceof LivingEntity living && living.getHealth() <= living.getMaxHealth() * BLEEDING_HEALTH) {
            deposit(world, entity, BLOOD_STRENGTH * 0.6f, BLOOD);
        }
    }

    /** Appelé par Sentience quand une entité émettrice est blessée. */
    public static void onDamaged(LivingEntity entity) {
        if (entity.getEntityWorld() instanceof ServerWorld world && isEmitter(entity)) {
            deposit(world, entity, BLOOD_STRENGTH, BLOOD);
        }
    }

    private static void cleanup(ServerWorld world) {
        Map<Long, Deque<ScentPoint>> chunks = CHUNKS.get(world);
        if (chunks == null) {
            return;
        }
        long now = world.getTime();
        for (Iterator<Deque<ScentPoint>> it = chunks.values().iterator(); it.hasNext(); ) {
            Deque<ScentPoint> points = it.next();
            // Les points d'un chunk ne sont pas forcément dans l'ordre (dépôts custom datés) : on filtre tout
            points.removeIf(p -> now - p.tick() > ScentRules.MAX_RETENTION_TICKS);
            if (points.isEmpty()) {
                it.remove();
            }
        }
    }

    /** Debug : pistes récentes autour des joueurs (vert = trace, rouge = sang, gris = dans l'eau). */
    private static void showTrails(ServerWorld world) {
        DustParticleEffect trail = new DustParticleEffect(0x55FF55, 0.8f);
        DustParticleEffect blood = new DustParticleEffect(0xCC0000, 1.0f);
        DustParticleEffect water = new DustParticleEffect(0x8888AA, 0.6f);
        long now = world.getTime();
        for (ServerPlayerEntity player : world.getPlayers()) {
            for (ScentPoint p : near(world, player.getEntityPos(), 24, DEBUG_RULES)) {
                if (now - p.tick() > 1200) {
                    continue;
                }
                DustParticleEffect effect = BLOOD.equals(p.kind()) ? blood : p.inWater() ? water : trail;
                world.spawnParticles(player, effect, false, false, p.pos().x, p.pos().y + 0.1, p.pos().z, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void clearAll() {
        CHUNKS.clear();
    }

    /** Filtre utilitaire : seulement les pistes d'un type d'entité. */
    public static Predicate<ScentPoint> fromType(EntityType<?> type) {
        return p -> p.sourceType() == type;
    }
}
