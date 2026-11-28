# Frontend PlotTwist

Prototype navigable du **Bureau de l'enquêteur**, réalisé en HTML, CSS et
JavaScript natifs. La démo utilise l'affaire fictive « La dernière
représentation » et permet de parcourir le réseau des suspects, la carte et la
chronologie, le carnet d'indices et le formulaire d'accusation. L'onglet
Vue d'ensemble met en avant les conflits F4 et l'onglet Interrogatoire propose
un tableau d'enquête interactif : relier deux fiches, désigner un mensonge et
recevoir le verdict booléen du backend F4. L'onglet des suspects est connecté
à la feature F1 du backend et affiche les profils ainsi que les récits
subjectifs.

## Lancer l'application

Le tableau F4 et l'onglet des suspects utilisent l'API Spring Boot. Depuis la
racine du dépôt, lancer :

```bash
mvn spring-boot:run
```

Puis ouvrir `http://localhost:8080/`. Le backend sert le front et fournit les
endpoints F1 et F4.

## Organisation

- `style.css` : design system partagé, responsive et sombre.
- `f4-board.css` : styles isolés du tableau F4, avec des classes préfixées
  `f4-`.
- `api.js` : état de session persistant dans le navigateur et client HTTP
  centralisé sous `/api/v1` pour les routes F1 et F4.
- `data.js` : fixture locale utilisée par les vues encore en mode prototype.
- `suspects-view.js`, `timeline-view.js`, `clues-view.js`,
  `verdict-view.js` : vues métier modulaires.
- `app.js` et `index.html` : navigation, assemblage du dashboard et interactions.

F4 utilise la fixture fixe `#AF42`. Le tableau lit les déclarations avec
`GET /api/v1/f4/declarations` et valide les accusations avec
`POST /api/v1/f4/accusations`. `GET /api/v1/f4/conflicts` reste disponible côté
backend, mais le front ne le charge pas afin de ne pas révéler les réponses.

L'onglet des suspects consomme `GET /api/v1/f1/profiles`,
`GET /api/v1/f1/events` et `GET /api/v1/f1/graph` avec la graine `#AF42`.
