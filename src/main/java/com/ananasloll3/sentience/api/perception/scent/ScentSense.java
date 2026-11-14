package com.ananasloll3.sentience.api.perception.scent;

import com.ananasloll3.sentience.Sentience;
import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.perception.PerceptionComponent;
import com.ananasloll3.sentience.api.perception.SenseModule;
import com.ananasloll3.sentience.api.perception.SenseType;
import com.ananasloll3.sentience.api.perception.StimulusInput;
import com.ananasloll3.sentience.api.rules.ScentRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Odorat : renifle les pistes autour de lui et signale la trace la plus fraîche, selon les
 * {@link ScentRules} de l'agent. En la suivant, l'agent remonte la piste pas à pas jusqu'à sa source.
 * L'odeur ne traverse pas les murs épais.
 *
 * @param range       distance de flair (blocs)
 * @param sensitivity score d'une trace parfaitement fraîche
 * @param filter      pistes qui l'intéressent
 */
public record ScentSense(double range, float sensitivity, Predicate<ScentTrails.ScentPoint> filter) implements SenseModule {

    public static final SenseType SCENT = new SenseType(Sentience.id("scent"));
    private static final int CHECK_INTERVAL = 10;
    private static final int MAX_WALL_CHECKS = 6;

    /** Chien de chasse : 8 blocs, suit toutes les pistes. */
    public static ScentSense hound() {
        return new ScentSense(8.0, 12f, p -> true);
    }

    public static ScentSense of(double range, float sensitivity) {
        return new ScentSense(range, sensitivity, p -> true);
    }

    /** Ne suit que certaines pistes (ex. {@code ScentTrails.fromType(EntityType.PLAYER)}). */
    public ScentSense only(Predicate<ScentTrails.ScentPoint> predicate) {
        return new ScentSense(range, sensitivity, filter.and(predicate));
    }

    /** Ne suit que les joueurs. */
    public ScentSense playersOnly() {
        return only(ScentTrails.fromType(EntityType.PLAYER));
    }

    @Override
    public SenseType type() {
        return SCENT;
    }

    @Override
    public void tick(Agent agent, PerceptionComponent perception, ServerWorld world, int deltaTicks) {
        if ((world.getTime() + agent.mob().getId()) % CHECK_INTERVAL >= deltaTicks) {
            return;
        }
        Vec3d nose = agent.mob().getEntityPos().add(0, 0.5, 0);
        ScentRules rules = agent.rules().scent();

        // Candidats : intensité perçue par CET agent (règles + modificateurs)
        List<Candidate> candidates = new ArrayList<>();
        for (ScentTrails.ScentPoint point : ScentTrails.near(world, nose, range, rules)) {
            if (point.source().equals(agent.mob().getUuid()) || !filter.test(point)) {
                continue;
            }
            float intensity = ScentTrails.intensity(world, point, rules) * rules.modifierFactor(agent, point);
            if (intensity > 0.05f) {
                candidates.add(new Candidate(point, intensity));
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        // La plus forte d'abord ; à force égale, la plus récente (= devant, dans le sens de la marche)
        candidates.sort(Comparator.comparingDouble((Candidate c) -> -c.intensity)
                .thenComparingLong(c -> -c.point.tick()));
        int checks = 0;
        for (Candidate c : candidates) {
            if (rules.maxWallBlocks >= 0 && checks++ < MAX_WALL_CHECKS
                    && wallsBetween(world, nose, c.point.pos().add(0, 0.3, 0), rules.maxWallBlocks) > rules.maxWallBlocks) {
                continue;
            }
            if (checks > MAX_WALL_CHECKS) {
                return;
            }
            // Bout de piste : la trace la plus fraîche est déjà sous son nez, elle ne le relance plus.
            // Le stimulus vieillit, l'agent fouille autour puis abandonne.
            if (c.point.pos().squaredDistanceTo(agent.mob().getEntityPos()) < 2.25) {
                return;
            }
            String kind = ScentTrails.BLOOD.equals(c.point.kind()) ? "blood" : "scent";
            perception.report(new StimulusInput(SCENT, c.point.pos(), 1.0, c.intensity * sensitivity,
                    c.intensity * 100f, kind, c.point.source()));
            return;
        }
    }

    /** Nombre de blocs pleins (cubes opaques) traversés, en s'arrêtant dès qu'on dépasse la limite. */
    private static int wallsBetween(ServerWorld world, Vec3d from, Vec3d to, int limit) {
        Vec3d delta = to.subtract(from);
        double length = delta.length();
        int steps = (int) Math.ceil(length / 0.4);
        int walls = 0;
        BlockPos last = null;
        for (int i = 1; i < steps; i++) {
            BlockPos pos = BlockPos.ofFloored(from.add(delta.multiply((double) i / steps)));
            if (pos.equals(last)) {
                continue;
            }
            last = pos;
            BlockState state = world.getBlockState(pos);
            if (state.isOpaqueFullCube() && ++walls > limit) {
                return walls;
            }
        }
        return walls;
    }

    private record Candidate(ScentTrails.ScentPoint point, float intensity) {
    }
}
