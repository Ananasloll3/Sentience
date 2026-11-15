package com.ananasloll3.sentience.api.query;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.api.perception.sight.SightSense;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

/**
 * Test d'une requête : mesure une valeur pour chaque point, puis
 * <ul>
 *   <li><b>filtre</b> les points hors de [{@code min}, {@code max}] ;</li>
 *   <li><b>note</b> les points restants (valeur normalisée × {@code weight}, inversée si {@code preferLow}).</li>
 * </ul>
 * Un poids de 0 fait un filtre pur.
 */
public record QueryTest(Measure measure, double min, double max, double weight, boolean preferLow) {

    /** Prépare la mesure pour une exécution (résout les cibles une seule fois). */
    @FunctionalInterface
    public interface Measure {
        /** @return la fonction de mesure, ou {@code null} si une cible manque (la requête échoue) */
        @Nullable
        ToDoubleFunction<Vec3d> prepare(Agent agent, ServerWorld world);
    }

    public static QueryTest of(Measure measure) {
        return new QueryTest(measure, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, false);
    }

    public QueryTest between(double min, double max) {
        return new QueryTest(measure, min, max, weight, preferLow);
    }

    public QueryTest atLeast(double min) {
        return between(min, max);
    }

    public QueryTest atMost(double max) {
        return between(min, max);
    }

    /** Préfère les grandes valeurs. */
    public QueryTest preferHigh(double weight) {
        return new QueryTest(measure, min, max, weight, false);
    }

    /** Préfère les petites valeurs. */
    public QueryTest preferLow(double weight) {
        return new QueryTest(measure, min, max, weight, true);
    }

    // ------------------------------------------------------------------ mesures intégrées

    /** Distance entre le point et la cible. */
    public static QueryTest distance(QueryTarget target) {
        return of((agent, world) -> {
            Vec3d t = target.resolve(agent, world);
            return t == null ? null : p -> p.distanceTo(t);
        });
    }

    /** 1 si la cible voit le point (à hauteur de tête), 0 sinon. Avec {@code atMost(0)} : points cachés. */
    public static QueryTest visibleFrom(QueryTarget target) {
        return of((agent, world) -> {
            Vec3d eye = target.resolveEye(agent, world);
            var rules = agent.rules().sight();
            return eye == null ? null : p -> SightSense.hasLineOfSight(world, eye, p.add(0, 1.5, 0), rules) ? 1 : 0;
        });
    }

    /**
     * Cosinus de l'angle, vu depuis {@code from}, entre la direction du point et celle de {@code to}
     * (1 = même direction, 0 = de côté, -1 = à l'opposé). Utile pour contourner une cible.
     */
    public static QueryTest dot(QueryTarget from, QueryTarget to) {
        return of((agent, world) -> {
            Vec3d a = from.resolve(agent, world);
            Vec3d b = to.resolve(agent, world);
            if (a == null || b == null) {
                return null;
            }
            Vec3d ref = b.subtract(a).multiply(1, 0, 1);
            if (ref.lengthSquared() < 1e-4) {
                return p -> 0;
            }
            Vec3d dir = ref.normalize();
            return p -> {
                Vec3d v = p.subtract(a).multiply(1, 0, 1);
                return v.lengthSquared() < 1e-4 ? 0 : v.normalize().dotProduct(dir);
            };
        });
    }

    /** Lumière au point (0 à 15). */
    public static QueryTest light() {
        return of((agent, world) -> p -> world.getLightLevel(BlockPos.ofFloored(p).up()));
    }

    /** 1 si un bloc solide protège le point du côté de la cible (à hauteur de tête), 0 sinon. */
    public static QueryTest cover(QueryTarget threat) {
        return of((agent, world) -> {
            Vec3d t = threat.resolve(agent, world);
            if (t == null) {
                return null;
            }
            return p -> {
                Vec3d dir = t.subtract(p).multiply(1, 0, 1);
                if (dir.lengthSquared() < 1e-4) {
                    return 0;
                }
                Vec3d step = dir.normalize();
                for (double d = 0.8; d <= 1.6; d += 0.4) {
                    BlockPos head = BlockPos.ofFloored(p.add(step.multiply(d)).add(0, 1.2, 0));
                    if (!world.getBlockState(head).getCollisionShape(world, head).isEmpty()) {
                        return 1;
                    }
                }
                return 0;
            };
        });
    }

    /** Valeur aléatoire entre 0 et 1, pour départager des points équivalents. */
    public static QueryTest random() {
        return of((agent, world) -> p -> agent.mob().getRandom().nextDouble());
    }
}
