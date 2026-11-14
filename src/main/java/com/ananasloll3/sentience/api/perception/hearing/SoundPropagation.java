package com.ananasloll3.sentience.api.perception.hearing;

import com.ananasloll3.sentience.api.rules.HearingRules;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * Physique simplifiée de la propagation du son :
 * divergence sphérique (-6 dB par doublement de distance), absorption de l'air,
 * occlusion par les blocs traversés et localisation imparfaite de la source.
 */
public final class SoundPropagation {

    private SoundPropagation() {
    }

    /** Intensité à {@code distance} blocs, sans obstacle. */
    public static float attenuate(float sourceDb, double distance, HearingRules rules) {
        double d = Math.max(distance, 1.0);
        return (float) (sourceDb - 20.0 * Math.log10(d) - d * rules.airAbsorptionPerBlock);
    }

    /**
     * Calcule ce que perçoit un auditeur. Renvoie {@code null} si le bruit n'est pas entendu.
     *
     * @param sourceDb  intensité à la source, après les modificateurs des règles de l'auditeur
     * @param ear       position de l'oreille
     * @param ambientDb niveau du bruit de fond à l'oreille (masquage)
     * @param rules     règles physiques de l'auditeur
     */
    public static PerceivedNoise perceive(NoiseEvent event, float sourceDb, Vec3d ear, HearingProfile profile,
                                          float ambientDb, Random random, HearingRules rules) {
        double distance = ear.distanceTo(event.pos());
        if (distance > profile.maxRange()) {
            return null;
        }

        float floor = Math.max(profile.thresholdDb(), ambientDb);
        float raw = attenuate(sourceDb, distance, rules);
        if (raw <= floor) {
            return null; // inaudible même sans obstacle : on évite le raycast
        }

        float occlusion = occlusion(event.world(), event.pos(), ear, rules);
        float perceived = raw - occlusion;
        if (perceived <= floor) {
            return null;
        }

        float margin = perceived - floor;
        Vec3d estimate = estimatePosition(event.pos(), ear, distance, margin, occlusion, profile.localizationSkill(), random);
        double error = localizationError(distance, margin, occlusion, profile.localizationSkill());
        return new PerceivedNoise(event, estimate, perceived, margin, distance, occlusion, error);
    }

    /**
     * Incertitude de localisation. On localise bien la direction, mal la distance ;
     * un son faible ou passé à travers un mur est beaucoup plus flou.
     */
    public static double localizationError(double distance, float margin, float occlusion, double skill) {
        double blur = (1.0 + occlusion / 15.0) / (1.0 + margin / 10.0) / Math.max(skill, 0.1);
        return Math.max(0.5, distance * 0.30 * blur);
    }

    private static Vec3d estimatePosition(Vec3d source, Vec3d ear, double distance, float margin, float occlusion,
                                          double skill, Random random) {
        double blur = (1.0 + occlusion / 15.0) / (1.0 + margin / 10.0) / Math.max(skill, 0.1);
        double radialSigma = Math.max(0.3, distance * 0.30 * blur);   // erreur sur la distance
        double lateralSigma = Math.max(0.3, distance * 0.12 * blur);  // erreur sur la direction
        double verticalSigma = Math.min(radialSigma, 2.0);

        Vec3d toSource = new Vec3d(source.x - ear.x, 0, source.z - ear.z);
        Vec3d radial = toSource.lengthSquared() < 1.0E-4 ? new Vec3d(1, 0, 0) : toSource.normalize();
        Vec3d lateral = new Vec3d(-radial.z, 0, radial.x);

        double r = clampedGaussian(random) * radialSigma;
        double l = clampedGaussian(random) * lateralSigma;
        double v = clampedGaussian(random) * verticalSigma;
        return source.add(radial.multiply(r)).add(lateral.multiply(l)).add(0, v, 0);
    }

    private static double clampedGaussian(Random random) {
        return MathHelper.clamp(random.nextGaussian(), -2.0, 2.0);
    }

    /**
     * Atténuation due aux blocs entre la source et l'oreille.
     * Parcours voxel exact (Amanatides & Woo) : chaque bloc traversé compte une seule fois.
     */
    public static float occlusion(ServerWorld world, Vec3d from, Vec3d to, HearingRules rules) {
        Vec3d delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-3) {
            return 0f;
        }
        Vec3d dir = delta.multiply(1.0 / length);

        int x = MathHelper.floor(from.x);
        int y = MathHelper.floor(from.y);
        int z = MathHelper.floor(from.z);
        int endX = MathHelper.floor(to.x);
        int endY = MathHelper.floor(to.y);
        int endZ = MathHelper.floor(to.z);

        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        double tDeltaX = dir.x == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dir.x);
        double tDeltaY = dir.y == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dir.y);
        double tDeltaZ = dir.z == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dir.z);

        double tMaxX = dir.x == 0 ? Double.MAX_VALUE : ((stepX > 0 ? (x + 1 - from.x) : (from.x - x)) * tDeltaX);
        double tMaxY = dir.y == 0 ? Double.MAX_VALUE : ((stepY > 0 ? (y + 1 - from.y) : (from.y - y)) * tDeltaY);
        double tMaxZ = dir.z == 0 ? Double.MAX_VALUE : ((stepZ > 0 ? (z + 1 - from.z) : (from.z - z)) * tDeltaZ);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        float total = 0f;
        int guard = 0;

        while (guard++ < 256) {
            // On avance d'abord : le bloc de la source n'atténue pas son propre son
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                if (tMaxX > length) break;
                x += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                if (tMaxY > length) break;
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                if (tMaxZ > length) break;
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
            if (x == endX && y == endY && z == endZ) {
                break; // bloc de l'oreille
            }

            pos.set(x, y, z);
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            total += blockAttenuation(world, pos, rules);
            if (total >= rules.maxOcclusionDb) {
                return rules.maxOcclusionDb;
            }
        }
        return total;
    }

    /** Atténuation d'un seul bloc, en dB : d'abord les règles custom, puis les matériaux intégrés. */
    public static float blockAttenuation(ServerWorld world, BlockPos pos, HearingRules rules) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return 0f;
        }
        for (HearingRules.BlockAcoustics acoustics : rules.blockAcoustics) {
            Float custom = acoustics.attenuation(world, pos, state);
            if (custom != null) {
                return custom;
            }
        }

        FluidState fluid = state.getFluidState();
        float fluidLoss = 0f;
        if (!fluid.isEmpty()) {
            fluidLoss = fluid.isIn(FluidTags.WATER) ? rules.waterDb : rules.lavaBlockDb;
        }

        // Laine, tapis… : absorbants acoustiques (même tag que les vibrations vanilla)
        if (state.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) {
            return rules.muffledDb;
        }
        // Portes, trappes, portillons : quasi transparents une fois ouverts
        if (state.contains(Properties.OPEN)) {
            return state.get(Properties.OPEN) ? rules.openDoorDb : rules.closedDoorDb;
        }
        if (state.isIn(BlockTags.LEAVES)) {
            return rules.leavesDb;
        }
        if (!state.blocksMovement()) {
            return fluidLoss; // herbe, fleurs, torches… (et l'eau seule)
        }
        if (state.isOpaqueFullCube()) {
            float hardness = state.getHardness(world, pos);
            if (hardness < 0) {
                return rules.unbreakableDb;
            }
            return rules.solidBaseDb + Math.min(hardness, 50f) * rules.hardnessFactor;
        }
        return rules.thinSolidDb + fluidLoss; // verre, barrières, dalles…
    }
}
