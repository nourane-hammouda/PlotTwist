# Frontend PlotTwist

Prototype navigable du **Bureau de l'enquêteur**, réalisé en HTML, CSS et
JavaScript natifs. La démo utilise l'affaire fictive « La dernière
représentation » et permet de parcourir le réseau des suspects, la carte et la
chronologie, le carnet d'indices et le formulaire d'accusation. L'onglet
Vue d'ensemble met en avant les conflits F4 et l'onglet Interrogatoire propose
un tableau d'enquête interactif : relier deux fiches, désigner un mensonge et
recevoir le verdict booléen du backend F4.

## Lancer la démo

Le tableau F4 utilise l'API Spring Boot. Depuis la racine du dépôt, lancer :

```bash
mvn spring-boot:run
```

Puis ouvrir `http://localhost:8080/`.

## Organisation

- `style.css` : design system partagé, responsive et sombre.
- `f4-board.css` : styles isolés du tableau F4, avec des classes préfixées
  `f4-`.
- `api.js` : état de session persistant et appels aux routes F4.
- `data.js` : données statiques des vues de démonstration.
- `suspects-view.js`, `timeline-view.js`, `clues-view.js`,
  `verdict-view.js` : vues métier modulaires.
- `app.js` et `index.html` : navigation, assemblage du dashboard et interactions.

F4 utilise la fixture fixe `#AF42`. Le tableau lit les déclarations avec
`GET /api/v1/f4/declarations` et valide les accusations avec
`POST /api/v1/f4/accusations`. `GET /api/v1/f4/conflicts` reste disponible côté
backend, mais le front ne le charge pas afin de ne pas révéler les réponses.
