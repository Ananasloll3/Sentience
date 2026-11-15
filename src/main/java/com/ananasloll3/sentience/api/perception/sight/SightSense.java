package com.ananasloll3.sentience.api.perception.sight;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.blackboard.BlackboardKeys;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseModule;
import com.ananasloll3.sentience.api.perception.SenseType;
import com.ananasloll3.sentience.api.perception.StimulusInput;
import com.ananasloll3.sentience.api.rules.SightRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Vue : cône de vision net + périphérique, ligne de vue réelle (raycast, à travers le verre), lumière, accroupi,
 * invisibilité, mouvement. Chaque cible remplit une jauge de détection, comme dans un jeu
 * d'infiltration : un coup d'œil donne un stimulus flou, une détection complète une position exacte.
 */
public final class SightSense implements SenseModule {

    /** Score d'une cible pleinement repérée (≥ seuil de chasse par défaut). */
    public static final float SEEN_SCORE = 30f;
    /** Jauge à partir de laquelle l'agent « croit avoir vu quelque chose ». */
    public static final float GLIMPSE_LEVEL = 0.35f;

    private final SightConfig config;
    private final Map<UUID, Float> detection = new HashMap<>();
    private int cooldown;
    private float lastBestVisibility;

    public SightSense(SightConfig config) {
        this.config = config;
    }

    @Override
    public SenseType type() {
        return SenseType.SIGHT;
    }

    public SightConfig config() {
        return config;
    }

    /** Jauge de détection (0..1) d'une cible. */
    public float getDetection(UUID target) {
        return detection.getOrDefault(target, 0f);
    }

    /** Jauge la plus remplie, pour le debug. */
    public float getHighestDetection() {
        float max = 0f;
        for (float v : detection.values()) {
            max = Math.max(max, v);
        }
        return max;
    }

    @Override
    public void tick(Agent agent, PerceptionComponent perception, ServerWorld world, int deltaTicks) {
        cooldown -= deltaTicks;
        if (cooldown > 0) {
            return;
        }
        int elapsed = config.checkInterval() - cooldown; // ticks réellement écoulés depuis le dernier check
        cooldown = config.checkInterval();

        MobEntity mob = agent.mob();
        SightRules rules = agent.rules().sight();
        Vec3d eye = mob.getEyePos();
        Vec3d look = Vec3d.fromPolar(mob.getPitch(), mob.getHeadYaw());
        UUID bestVisible = null;
        float bestDetection = 0f;
        lastBestVisibility = 0f;

        Map<UUID, Float> seenThisCheck = new HashMap<>();
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class,
                mob.getBoundingBox().expand(config.range()),
                e -> e != mob && e.isAlive() && config.targets().test(e))) {

            float visibility = visibility(world, mob, eye, look, target, rules);
            lastBestVisibility = Math.max(lastBestVisibility, visibility);
            float level = detection.getOrDefault(target.getUuid(), 0f);

            if (visibility > 0.02f) {
                double distance = eye.distanceTo(target.getEyePos());
                // Tout près et visible : repérage instantané
                float gain = distance < rules.instantDetectionDistance ? 1f : visibility * elapsed / Math.max(1, config.detectTicks());
                level = Math.min(1f, level + gain);
                report(perception, target, level, visibility);
                if (level >= 1f && level >= bestDetection) {
                    bestDetection = level;
                    bestVisible = target.getUuid();
                }
            } else {
                level -= (float) elapsed / (config.detectTicks() * 3f);
            }
            if (level > 0f) {
                seenThisCheck.put(target.getUuid(), level);
            }
        }

        // Les cibles hors de portée voient leur jauge redescendre aussi
        for (Iterator<Map.Entry<UUID, Float>> it = detection.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Float> e = it.next();
            if (!seenThisCheck.containsKey(e.getKey())) {
                float decayed = e.getValue() - (float) elapsed / (config.detectTicks() * 3f);
                if (decayed > 0f) {
                    seenThisCheck.put(e.getKey(), decayed);
                }
            }
        }
        detection.clear();
        detection.putAll(seenThisCheck);
        agent.blackboard().set(BlackboardKeys.VISIBLE_TARGET, bestVisible);
    }

    private void report(PerceptionComponent perception, LivingEntity target, float level, float visibility) {
        if (level >= 1f) {
            perception.report(new StimulusInput(SenseType.SIGHT, target.getEntityPos(), 0.3,
                    SEEN_SCORE, visibility * 100f, "seen", target.getUuid()));
        } else if (level >= GLIMPSE_LEVEL) {
            // « J'ai cru voir quelque chose » : position et identité incertaines
            double error = 1.0 + (1.0 - level) * 6.0;
            Vec3d guess = target.getEntityPos().add(
                    (target.getRandom().nextDouble() - 0.5) * error,
                    0,
                    (target.getRandom().nextDouble() - 0.5) * error);
            perception.report(new StimulusInput(SenseType.SIGHT, guess, error,
                    level * 12f, visibility * 100f, "glimpse", null));
        }
    }

    /** Visibilité 0..1 d'une cible : angle × ligne de vue × lumière × discrétion × distance. */
    private float visibility(ServerWorld world, MobEntity mob, Vec3d eye, Vec3d look, LivingEntity target,
                             SightRules rules) {
        Vec3d targetEye = target.getEyePos();
        Vec3d toTarget = targetEye.subtract(eye);
        double distance = toTarget.length();
        if (distance > config.range() || distance < 1.0E-3) {
            return 0f;
        }

        // Angle : vision nette au centre, floue en périphérie, rien derrière
        double cos = look.dotProduct(toTarget.multiply(1.0 / distance));
        double angle = Math.toDegrees(Math.acos(MathHelper.clamp(cos, -1.0, 1.0)));
        float angleFactor;
        if (angle <= config.fovDegrees() / 2f) {
            angleFactor = 1f;
        } else if (angle <= config.peripheralDegrees() / 2f) {
            angleFactor = rules.peripheralFactor;
        } else {
            return 0f;
        }

        // Ligne de vue : yeux et centre du corps
        float los = 0f;
        if (canSee(world, mob, eye, targetEye, rules)) los += 0.6f;
        if (canSee(world, mob, eye, target.getBoundingBox().getCenter(), rules)) los += 0.4f;
        if (los == 0f) {
            return 0f;
        }

        float light = lightFactor(world, target, rules);
        float stealth = rules.stealthFactor(mob, target);
        if (target.isSneaking()) stealth *= rules.sneakFactor;
        if (!config.seesInvisible() && target.hasStatusEffect(StatusEffects.INVISIBILITY)) stealth *= rules.invisibilityFactor;
        if (target.getVelocity().horizontalLengthSquared() > 0.003) stealth *= rules.movementFactor; // le mouvement attire l'œil

        float distanceFactor = (float) (1.0 - 0.7 * (distance / config.range()) * (distance / config.range()));

        if (rules.glowingReveals && (target.isGlowing() || target.hasStatusEffect(StatusEffects.GLOWING))) {
            light = 1f;
            stealth = Math.max(stealth, 1f);
        }
        return MathHelper.clamp(angleFactor * los * light * stealth * distanceFactor, 0f, 1f);
    }

    private float lightFactor(ServerWorld world, LivingEntity target, SightRules rules) {
        if ((rules.fireReveals && target.isOnFire())
                || (rules.lightInHandReveals && (holdsLight(target.getMainHandStack()) || holdsLight(target.getOffHandStack())))) {
            return 1f;
        }
        int light = world.getLightLevel(target.getBlockPos().up());
        float base = rules.darknessFloor + (1f - rules.darknessFloor) * (light / 15f);
        return Math.max(base, config.darkVision());
    }

    private static boolean holdsLight(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock().getDefaultState().getLuminance() > 0;
    }

    /**
     * Ligne de vue selon des règles de vue (le verre, les vitres et les barreaux laissent passer le regard).
     * Utilisé aussi par les requêtes d'environnement.
     */
    public static boolean hasLineOfSight(ServerWorld world, Vec3d from, Vec3d to, SightRules rules) {
        return canSee(world, null, from, to, rules);
    }

    /** Ligne de vue : verre, vitres et barreaux laissent passer le regard, les fluides non. */
    private static boolean canSee(ServerWorld world, MobEntity mob, Vec3d from, Vec3d to, SightRules rules) {
        Boolean blocked = BlockView.raycast(from, to, world, (w, pos) -> {
            BlockState state = w.getBlockState(pos);
            if (state.isAir() || rules.seeThrough.test(state)) {
                return null;
            }
            VoxelShape shape = state.getCollisionShape(w, pos);
            if (shape.isEmpty()) {
                return null; // herbe, fleurs, torches…
            }
            return w.raycastBlock(from, to, pos, shape, state) != null ? Boolean.TRUE : null;
        }, w -> Boolean.FALSE);
        return !blocked;
    }

    /** Meilleure visibilité mesurée au dernier check (0..1), pour le debug. */
    public float getLastBestVisibility() {
        return lastBestVisibility;
    }
}
