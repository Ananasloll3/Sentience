# Debug et outils de test

## `/sentience debug`

Bascule l'affichage de debug (opérateurs) :

- **au-dessus de chaque agent** : niveau d'alerte, sens qui a donné le focus (👂 ouïe, 👁 vue, 👃 odorat,
  ⚔ douleur, ✋ toucher, 📣 radio), score, incertitude (±), jauge de vue, niveau de détail ;
- **particules** là où l'agent **croit** avoir entendu quelque chose (couleur selon le type de bruit) ;
- **pistes d'odeur** autour de toi : vert = trace, rouge = sang, gris = dans l'eau ;
- **niveau de ton micro** dans la barre d'action (avec Simple Voice Chat).

```
HUNT  👂 22.2 ±2.9  👁40%
```

Les vrais noms des mobs sont restaurés en quittant le debug, et un nom de debug sauvegardé par erreur
avec le monde disparaît au rechargement.

## `/sentience bruit <dB>`

Émet un bruit test à ta position.

## Outils du mod de démo

| Commande | Effet |
|---|---|
| `/sentiencetest marcheur <pos> <secondes> [accroupi]` | un faux joueur fait des allers-retours (il fait du bruit et laisse une odeur) |
| `/sentiencetest piste <de> <a>` | fabrique une piste d'odeur comme si quelqu'un venait de marcher de A à B |
| `/sentiencetest arene <nom>` | construit ou relance une arène de test devant toi |

!!! note "Limite du faux joueur"
    Le marcheur n'existe pas vraiment dans le monde : il fait du bruit et laisse une odeur,
    mais la **vue** ne peut pas le voir. Teste la vue sur toi-même ou sur des villageois.
