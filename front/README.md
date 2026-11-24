# Frontend PlotTwist

Prototype navigable du **Bureau de l'enquêteur**, réalisé en HTML, CSS et
JavaScript natifs pour visualiser les quatre premières features avant que le
backend soit disponible. La démo utilise l'affaire fictive « La dernière
représentation » et permet de parcourir le réseau des suspects, la carte et la
chronologie, le carnet d'indices et le formulaire d'accusation.

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
- `api.js` : état de session persistant dans le navigateur et client HTTP
  centralisé sous `/api/v1`.
- `data.js` : fixture de démonstration remplaçable par les réponses du backend.
- `suspects-view.js`, `timeline-view.js`, `clues-view.js`,
  `verdict-view.js` : vues métier modulaires.
- `app.js` et `index.html` : navigation, assemblage du dashboard et interactions.

Les contrats et routes métier REST n'étant pas encore présents dans le dépôt,
la démo reste en mode fixture. `api.startSession()` illustre le raccordement à
`POST /api/v1/sessions` ; les autres appels doivent être alignés sur les DTOs et
routes validés par l'équipe avant intégration.
