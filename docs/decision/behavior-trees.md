# Behavior Trees

Un **Behavior Tree** décrit tout le comportement d'un mob sous forme d'arbre, comme dans Unreal :
des **sélecteurs** choisissent la branche la plus prioritaire, des **séquences** enchaînent des étapes,
des **décorateurs** posent des conditions et des **services** tournent en tâche de fond.

On peut les écrire **en JSON** dans un datapack (rechargés à chaud avec `/reload`) ou **en Java**.

```java
Agent.builder(this)
        .hearing(HearingProfile.DEFAULT)
        .sight(SightConfig.humanoid())
        .damage()
        .awareness(AwarenessProfile.DEFAULT)
        .behaviorTree(Identifier.of("monmod", "garde"), 1)   // (1)!
        .build();
```

1. L'arbre est exécuté par un goal de priorité 1 qui prend le **mouvement** et le **regard** du mob.
   Les goals plus prioritaires (nager…) passent avant ; l'arbre reprend ensuite depuis la racine.
   N'utilise pas `defaultGoals` en même temps : l'arbre les remplace.

## En JSON

Fichier `data/monmod/sentience/behavior_tree/garde.json` → identifiant `monmod:garde`.

```json
{
  "root": {
    "type": "selector",
    "children": [
      {
        "name": "combattre",
        "type": "attack",
        "target": "visible_target",
        "decorators": [ { "type": "sees_target", "abort": "both" } ],
        "services": [ { "type": "share_focus", "range": 32, "interval": 40 } ]
      },
      {
        "name": "aller voir",
        "type": "sequence",
        "decorators": [ { "type": "awareness", "at_least": "investigate", "abort": "lower" } ],
        "children": [
          { "type": "move_to", "target": "focus_position", "speed": 1.1 },
          { "type": "search", "radius": 6, "ticks": 160 },
          { "type": "give_up" }
        ]
      },
      { "type": "wander", "radius": 10, "speed": 0.7 }
    ]
  }
}
```

Chaque nœud a un `type` et accepte en plus :

| Champ | Rôle |
|---|---|
| `name` | nom affiché par `/sentience debug` |
| `decorators` | liste de décorateurs (le premier est le plus extérieur) |
| `services` | liste de services, avec `interval` en ticks (20 par défaut) |

Un identifiant sans espace de noms (`sequence`, `focus_position`) désigne celui de Sentience.
Les clés du Blackboard s'écrivent par leur identifiant : `focus_position`, `visible_target`,
`monmod:cachette`…

!!! tip "Modifier à chaud"
    Modifie le fichier, tape `/reload` : tous les agents recréent leur arbre immédiatement.
    Les erreurs sont écrites dans le journal du serveur avec le nom du fichier.
    Un datapack peut aussi **remplacer** l'arbre d'un mod en réutilisant son identifiant.

## Exécution

À chaque tick, l'arbre est parcouru depuis la racine. Chaque nœud renvoie **SUCCESS**, **FAILURE**
ou **RUNNING** (pas fini, il reprendra au tick suivant).

| Composite | Comportement |
|---|---|
| `sequence` | enfants dans l'ordre ; échoue dès qu'un enfant échoue |
| `selector` | enfants par priorité ; réussit dès qu'un enfant réussit |
| `random_selector` | comme `selector`, dans un ordre aléatoire |
| `parallel` | tous les enfants à la fois ; `"require": "all"` ou `"one"` |
| `subtree` | insère un autre arbre : `"tree": "monmod:combat"` |

### Interruptions (`abort`)

Une condition utilisée comme décorateur accepte `abort`, comme les *observer aborts* d'Unreal :

| `abort` | Effet |
|---|---|
| `none` | vérifiée seulement à l'entrée de la branche |
| `self` | la branche s'arrête dès que la condition devient fausse |
| `lower` | dès qu'elle devient vraie, elle interrompt une branche **moins prioritaire** en cours |
| `both` | les deux |

C'est ce qui rend l'arbre réactif : la patrouille est interrompue dès qu'un bruit fait monter l'alerte,
le combat s'arrête dès que la cible n'est plus visible.

## Référence

### Tâches

| Type | Paramètres (défaut) |
|---|---|
| `wait` | `ticks` (20), `deviation` (0) |
| `move_to` | `target` (focus_position), `speed` (1), `acceptance` (1.5), `timeout` (0 = aucun) |
| `wander` | `radius` (8), `speed` (0.8) |
| `search` | `target` (focus_position), `radius` (6), `ticks` (200), `speed` (0.8) |
| `flee` | `from` (focus_position), `distance` (12), `speed` (1.3) |
| `look_at` | `target` (focus_position), `ticks` (0 = un tick) |
| `attack` | `target` (visible_target), `speed` (1.3), `cooldown` (20) — corps à corps |
| `run_query` | `query`, `result` (query_result) — voir [requêtes d'environnement](eqs.md) |
| `set` | `key`, `value` ou `from` (copie une autre clé) |
| `clear` | `key` |
| `play_sound` | `sound`, `volume` (1), `pitch` (1) |
| `emit_noise` | `db` (80), `noise` (generic) — entendu par les agents |
| `share_focus` | `range` (32), `same_type` (true) |
| `give_up` | oublie le stimulus en cours |
| `alert` | `level` : monte l'alerte au moins à ce niveau |
| `set_attack_target` | `target` : cible vanilla, pour les goals vanilla |
| `succeed`, `fail` | réussit / échoue |
| `check` | `condition` : réussit si la condition est vraie |

Une condition peut aussi servir directement de nœud : `{ "type": "sees_target" }`.

`move_to`, `look_at`, `attack`… acceptent une clé contenant une position (`Vec3d`), un bloc (`BlockPos`),
une entité (son UUID) ou un stimulus.

### Conditions

Utilisables comme décorateurs (avec `abort`), dans `check`, ou dans `not` / `all` / `any`.
Toutes acceptent `"negate": true`.

| Type | Paramètres |
|---|---|
| `awareness` | `at_least`, `is` ou `below` : `idle`, `suspicious`, `investigate`, `hunt` |
| `sees_target` | une cible est vue avec certitude |
| `is_set` | `key` |
| `equals` | `key`, `value` |
| `alive` | `target` (visible_target) |
| `health` | `below` ou `above` (fraction de la vie max) |
| `distance` | `target`, `below` et/ou `above` |
| `light` | `below` et/ou `above` (0-15) |
| `is_night`, `is_day`, `in_water` | — |
| `chance` | `chance` (0.5) |
| `not` | `condition` |
| `all`, `any` | `conditions` |

### Décorateurs

En plus des conditions :

| Type | Paramètres |
|---|---|
| `cooldown` | `ticks` (100) : échoue tant que la branche a fini il y a moins de `ticks` |
| `time_limit` | `ticks` (200) : interrompt et échoue au-delà |
| `repeat` | `count` (-1 = sans fin) : relance tant que l'enfant réussit |
| `invert`, `force_success`, `force_failure` | modifient le résultat |

Ils sont aussi utilisables comme nœuds avec un `child`.

### Services

| Type | Paramètres |
|---|---|
| `run_query` | `query`, `result` : rafraîchit un point régulièrement |
| `share_focus` | `range`, `same_type` |
| `copy` | `from`, `to` |
| `look_at` | `target` |

## En Java

```java
public static final BehaviorTree GARDE = BehaviorTrees.register(Identifier.of("monmod", "garde"), () ->
        Bt.selector(
                Bt.attack(BlackboardKeys.VISIBLE_TARGET, 1.3, 20)
                        .guard(Bt.seesTarget(), BtAbort.BOTH)
                        .service(40, ctx -> AgentComms.shareFocus(ctx.agent(), 32, other -> true)),
                Bt.sequence(
                        Bt.moveTo(BlackboardKeys.FOCUS_POSITION, 1.1),
                        Bt.search(BlackboardKeys.FOCUS_POSITION, 6, 160, 0.8),
                        Bt.giveUp())
                        .guard(Bt.awarenessAtLeast(Awareness.INVESTIGATE), BtAbort.LOWER),
                Bt.wander(10, 0.7)));

Agent.builder(this)...behaviorTree(GARDE, 1).build();
```

La fabrique est appelée **une fois par agent** : chaque mob a ses propres nœuds.
Un arbre JSON de même identifiant remplace l'arbre Java.

### Écrire une tâche

```java
public class HowlTask extends BtNode {
    private int remaining;

    @Override
    protected void onStart(BtContext ctx) {
        remaining = 40;
        ctx.mob().playSound(SoundEvents.ENTITY_WOLF_HOWL, 3f, 0.8f);
    }

    @Override
    protected BtStatus update(BtContext ctx) {
        remaining -= ctx.deltaTicks();
        return remaining <= 0 ? BtStatus.SUCCESS : BtStatus.RUNNING;
    }

    @Override
    protected void onStop(BtContext ctx, boolean aborted) {
        // nettoyage (aborted = interrompue par une branche plus prioritaire)
    }
}
```

Pour la rendre disponible en JSON :

```java
BtRegistry.registerNode(Identifier.of("monmod", "howl"), json -> HowlTask::new);
BtRegistry.registerCondition(Identifier.of("monmod", "full_moon"),
        json -> ctx -> ctx.world().getMoonPhase() == 0);
BtRegistry.registerService(Identifier.of("monmod", "sniff"),
        json -> ctx -> ctx.mob().playSound(SoundEvents.ENTITY_WOLF_SNIFF, 1f, 1f));
```

Les paramètres JSON se lisent avec `SentienceJson` (`number`, `integer`, `string`, `key`…).
Les conditions et services doivent être **sans état** : une seule instance est partagée par tous les agents.

## Debug

`/sentience debug` affiche la tâche en cours au-dessus de chaque agent : `HUNT 👂 22.2 ±2.9 ▶ attack`.
