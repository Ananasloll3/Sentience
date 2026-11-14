package com.ananasloll3.sentience.api.perception.hearing;

import com.ananasloll3.sentience.api.rules.HearingRules;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Bruit de fond à une position. Un bruit n'est entendu que s'il dépasse ce niveau :
 * la pluie, une cascade ou la lave permettent donc de se cacher.
 */
public final class AmbientNoise {

    private AmbientNoise() {
    }

    /** Niveau de bruit de fond à une position, selon les règles de l'auditeur. */
    public static float levelAt(ServerWorld world, BlockPos pos, HearingRules rules) {
        float level = world.isSkyVisible(pos) ? rules.surfaceDb : rules.caveDb;

        if (world.hasRain(pos.up())) {
            float rain = world.isThundering() ? rules.thunderstormDb : rules.rainDb;
            if (rain > 0f) {
                level = combine(level, rain);
            }
        }

        // Échantillonnage grossier des fluides proches (eau qui coule, lave)
        int flowingWater = 0;
        int lava = 0;
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dx = -4; dx <= 4; dx += 2) {
            for (int dy = -2; dy <= 2; dy += 2) {
                for (int dz = -4; dz <= 4; dz += 2) {
                    m.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    FluidState fluid = world.getFluidState(m);
                    if (fluid.isEmpty()) {
                        continue;
                    }
                    if (fluid.isIn(FluidTags.LAVA)) {
                        lava++;
                    } else if (!fluid.isStill()) {
                        flowingWater++;
                    }
                }
            }
        }
        if (flowingWater >= 3 && rules.flowingWaterDb > 0f) {
            level = combine(level, rules.flowingWaterDb + Math.min(flowingWater, 20) * 0.6f);
        }
        if (lava >= 2 && rules.lavaDb > 0f) {
            level = combine(level, rules.lavaDb + Math.min(lava, 20) * 0.4f);
        }

        for (HearingRules.AmbientSource source : rules.ambientSources) {
            float extra = source.level(world, pos);
            if (!Float.isNaN(extra) && extra > 0f) {
                level = combine(level, extra);
            }
        }
        return level;
    }

    /** Somme énergétique de deux niveaux en dB (60 dB + 60 dB = 63 dB). */
    public static float combine(float a, float b) {
        return (float) (10.0 * Math.log10(Math.pow(10, a / 10.0) + Math.pow(10, b / 10.0)));
    }
}
