package com.ananasloll3.sentience.api.behavior;

import com.ananasloll3.sentience.api.behavior.node.ConditionDecorator;
import com.ananasloll3.sentience.api.behavior.node.Cooldown;
import com.ananasloll3.sentience.api.behavior.node.ServiceNode;
import com.ananasloll3.sentience.api.behavior.node.TimeLimit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Nœud de Behavior Tree. Chaque agent possède ses propres instances (un nœud garde son état
 * d'exécution) : un arbre se décrit donc par une fabrique, voir {@link BehaviorTree}.
 *
 * <p>Pour écrire une tâche, redéfinis {@link #update(BtContext)} et, au besoin,
 * {@link #onStart(BtContext)} et {@link #onStop(BtContext, boolean)}.</p>
 */
public abstract class BtNode {

    @Nullable
    private String name;
    private boolean active;

    /** Exécute le nœud pour ce tick. Appelle {@link #onStart} à l'entrée et {@link #onStop} à la sortie. */
    public final BtStatus tick(BtContext ctx) {
        if (!active) {
            active = true;
            onStart(ctx);
        }
        BtStatus status = update(ctx);
        if (status != BtStatus.RUNNING && active) {
            active = false;
            onStop(ctx, false);
        }
        return status;
    }

    /** Interrompt le nœud s'il est en cours (une branche plus prioritaire prend la main). */
    public final void abort(BtContext ctx) {
        if (active) {
            active = false;
            onStop(ctx, true);
        }
    }

    public final boolean isActive() {
        return active;
    }

    protected void onStart(BtContext ctx) {
    }

    protected abstract BtStatus update(BtContext ctx);

    /** @param aborted {@code true} si le nœud a été interrompu avant de finir */
    protected void onStop(BtContext ctx, boolean aborted) {
    }

    /**
     * Ce nœud demande-t-il à interrompre une branche moins prioritaire en cours ?
     * Utilisé par les sélecteurs (décorateur {@code condition} avec {@code abort: lower}).
     */
    public boolean wantsToPreempt(BtContext ctx) {
        return false;
    }

    /**
     * Ce nœud échouerait-il forcément s'il démarrait maintenant (cooldown en cours…) ?
     * Empêche une condition {@code abort: lower} d'interrompre une branche pour rien.
     */
    public boolean isBlocked(BtContext ctx) {
        return false;
    }

    /** Ajoute au chemin les noms des nœuds actifs, de la racine à la feuille (debug). */
    public void describeActive(List<String> path) {
        if (active) {
            path.add(name());
        }
    }

    public String name() {
        if (name != null) {
            return name;
        }
        String simple = getClass().getSimpleName();
        return simple.isEmpty() ? "node" : simple.substring(0, 1).toLowerCase(Locale.ROOT) + simple.substring(1);
    }

    // ------------------------------------------------------------------ décorateurs (Java)

    /** N'entre dans ce nœud que si la condition est vraie ; voir {@link BtAbort}. */
    public BtNode guard(BtCondition condition, BtAbort abort) {
        return new ConditionDecorator(this, condition, abort);
    }

    public BtNode guard(BtCondition condition) {
        return guard(condition, BtAbort.NONE);
    }

    /** Échoue tant que le nœud a fini il y a moins de {@code ticks}. */
    public BtNode cooldown(int ticks) {
        return new Cooldown(this, ticks);
    }

    /** Échoue si le nœud tourne plus de {@code ticks}. */
    public BtNode timeLimit(int ticks) {
        return new TimeLimit(this, ticks);
    }

    /** Exécute un service toutes les {@code interval} ticks tant que ce nœud est actif. */
    public BtNode service(int interval, BtService service) {
        return new ServiceNode(this, java.util.List.of(new ServiceNode.Entry(service, interval)));
    }

    /** Nom affiché par le debug. */
    public BtNode named(@Nullable String name) {
        this.name = name;
        return this;
    }
}
