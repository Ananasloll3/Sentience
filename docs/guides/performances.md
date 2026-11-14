# Performances

Sentience est conçu pour des dizaines d'agents par monde.

| Mécanisme | Effet |
|---|---|
| **Niveau de détail** | un agent loin des joueurs réfléchit 2 à 20 fois moins souvent, sans perdre en réactivité |
| **Bruits regroupés** | les bruits sont distribués une fois par tick ; les doublons (même source, même type) sont fusionnés ; 256 bruits max par tick |
| **Ouïe en deux temps** | le parcours des blocs (occlusion) n'est calculé que si le bruit serait audible sans obstacle |
| **Bruit de fond en cache** | recalculé toutes les secondes par auditeur, décalé d'un auditeur à l'autre |
| **Vue espacée** | une vérification toutes les 3 à 4 ticks par agent |
| **Pistes par chunk** | une recherche d'odeur ne parcourt que les chunks autour du nez |
| **Aucun coût sans agent** | si aucun agent n'écoute dans un monde, les bruits sont ignorés immédiatement |

## Conseils

- Garde des portées raisonnables (`withMaxRange`, `withRange`) : ce sont elles qui bornent le travail.
- Un agent purement décoratif n'a pas besoin de `defaultGoals`.
- Dans tes composants, utilise `deltaTicks` et un compte à rebours plutôt que de tout faire à chaque tick.
