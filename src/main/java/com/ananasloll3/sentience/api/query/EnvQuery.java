package com.ananasloll3.sentience.api.query;

import com.ananasloll3.sentience.api.agent.Agent;
import com.ananasloll3.sentience.impl.debug.SentienceDebug;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Requête d'environnement (l'EQS d'Unreal) : génère des points autour d'une cible, les filtre et
 * les note avec des tests, puis renvoie les meilleurs. Sert à trouver une cachette, un abri,
 * un point pour contourner, une direction de fuite…
 *
 * <pre>{@code
 * EnvQuery hide = EnvQuery.builder(QueryGenerator.ring(QueryTarget.QUERIER, 4, 16, 3, 12))
 *     .test(QueryTest.visibleFrom(QueryTarget.THREAT).atMost(0))         // caché de la menace
 *     .test(QueryTest.distance(QueryTarget.THREAT).preferHigh(1))       // loin d'elle
 *     .test(QueryTest.distance(QueryTarget.QUERIER).preferLow(1))       // mais pas trop loin de moi
 *     .pathable()
 *     .build();
 * Vec3d spot = hide.run(agent).best();
 * }</pre>
 */
public final class EnvQuery {

    /** Nombre maximal de chemins calculés pour vérifier les meilleurs points. */
    private static final int MAX_PATH_CHECKS = 6;

    private final QueryGenerator generator;
    private final List<QueryTest> tests;
    private final boolean pathable;

    private EnvQuery(QueryGenerator generator, List<QueryTest> tests, boolean pathable) {
        this.generator = generator;
        this.tests = List.copyOf(tests);
        this.pathable = pathable;
    }

    public static Builder builder(QueryGenerator generator) {
        return new Builder(generator);
    }

    public QueryResult run(Agent agent) {
        if (!(agent.mob().getEntityWorld() instanceof ServerWorld world)) {
            return QueryResult.EMPTY;
        }
        List<Vec3d> points = generator.generate(agent, world);
        int n = points.size();
        if (n == 0) {
            return QueryResult.EMPTY;
        }
        double[] scores = new double[n];
        boolean[] alive = new boolean[n];
        java.util.Arrays.fill(alive, true);

        for (QueryTest test : tests) {
            ToDoubleFunction<Vec3d> measure = test.measure().prepare(agent, world);
            if (measure == null) {
                return QueryResult.EMPTY;
            }
            double[] values = new double[n];
            double lo = Double.MAX_VALUE;
            double hi = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!alive[i]) continue;
                double v = measure.applyAsDouble(points.get(i));
                values[i] = v;
                if (v < test.min() || v > test.max()) {
                    alive[i] = false;
                    continue;
                }
                lo = Math.min(lo, v);
                hi = Math.max(hi, v);
            }
            if (test.weight() != 0 && hi > lo) {
                for (int i = 0; i < n; i++) {
                    if (!alive[i]) continue;
                    double norm = (values[i] - lo) / (hi - lo);
                    scores[i] += test.weight() * (test.preferLow() ? 1 - norm : norm);
                }
            }
        }

        List<QueryResult.ScoredPoint> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (alive[i]) {
                result.add(new QueryResult.ScoredPoint(points.get(i), scores[i]));
            }
        }
        result.sort(Comparator.comparingDouble(QueryResult.ScoredPoint::score).reversed());

        if (pathable) {
            int checks = Math.min(MAX_PATH_CHECKS, result.size());
            int found = -1;
            for (int i = 0; i < checks; i++) {
                Path path = agent.mob().getNavigation().findPathTo(BlockPos.ofFloored(result.get(i).pos()), 0);
                if (path != null && path.reachesTarget()) {
                    found = i;
                    break;
                }
            }
            if (found < 0) {
                result.clear();
            } else {
                result.subList(0, found).clear();
            }
        }
        QueryResult out = new QueryResult(List.copyOf(result));
        SentienceDebug.onQuery(world, points, out);
        return out;
    }

    public static final class Builder {

        private final QueryGenerator generator;
        private final List<QueryTest> tests = new ArrayList<>();
        private boolean pathable;

        private Builder(QueryGenerator generator) {
            this.generator = generator;
        }

        public Builder test(QueryTest test) {
            tests.add(test);
            return this;
        }

        /** Ne garde que des points atteignables (vérifié sur les meilleurs seulement). */
        public Builder pathable() {
            this.pathable = true;
            return this;
        }

        public EnvQuery build() {
            return new EnvQuery(generator, tests, pathable);
        }
    }
}
