# Frontend PlotTwist

Prototype navigable du **Bureau de l'enquêteur**, réalisé en HTML, CSS et
JavaScript natifs pour visualiser les premières features avant que le backend
soit disponible. La démo utilise l'affaire fictive « La dernière
représentation » et permet de parcourir le réseau des suspects, la carte et la
chronologie, le carnet d'indices et le formulaire d'accusation. L'onglet
Vue d'ensemble met en avant les conflits F4 et l'onglet Interrogatoire propose
un tableau d'enquête interactif : relier deux fiches, épingler MENSONGE sur
l'une d'elles et recevoir un verdict simulé par la fixture.

## Lancer la démo

Ouvrir `index.html` dans un navigateur récent. Comme les fichiers JavaScript
utilisent des modules ES, certains navigateurs exigent un serveur HTTP local.
Depuis la racine du dépôt, par exemple :

```bash
python3 -m http.server 8000
```

Puis ouvrir `http://localhost:8000/front/`.

## Organisation

- `style.css` : design system partagé, responsive et sombre.
- `f4-board.css` : styles isolés du tableau F4, avec des classes préfixées
  `f4-`.
- `api.js` : état de session persistant et accès F4 derrière un mode mock
  commutable vers les routes `/api/v1/f4`.
- `data.js` : fixture de démonstration remplaçable par les réponses du backend.
- `suspects-view.js`, `timeline-view.js`, `clues-view.js`,
  `verdict-view.js` : vues métier modulaires.
- `app.js` et `index.html` : navigation, assemblage du dashboard et interactions.

Les contrats et routes métier REST n'étant pas encore présents dans le dépôt,
la démo reste en mode fixture. `api.startSession()` illustre le raccordement à
`POST /api/v1/sessions` ; les autres appels doivent être alignés sur les DTOs et
routes validés par l'équipe avant intégration.
La démo F4 ne recalcule pas les conflits : sa fixture contient les conflits et
les verdicts attendus. La détection et la validation métier restent du ressort
du domaine Java et du futur backend.
Le bouton « Nouvelle affaire » est un emplacement de démonstration : aucune
route de génération d'affaire n'est encore implémentée.
