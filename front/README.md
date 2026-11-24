# Frontend PlotTwist

Prototype navigable du **Bureau de l'enquêteur**, réalisé en HTML, CSS et
JavaScript natifs. L'onglet des suspects est connecté à la feature F1 du
backend et affiche les profils ainsi que les récits subjectifs. Les autres vues
restent basées sur la fixture locale « La dernière représentation ».

## Lancer l'application

Depuis la racine du dépôt, démarrer Spring Boot :

```bash
mvn spring-boot:run
```

Puis ouvrir `http://localhost:8080/`. Le backend sert le front et fournit les
endpoints F1. Un serveur statique, par exemple `python3 -m http.server 8000`,
peut afficher le tableau de bord, mais l'onglet des suspects nécessite le
backend sur la même origine.

## Organisation

- `style.css` : design system partagé, responsive et sombre.
- `api.js` : état de session persistant dans le navigateur et client HTTP
  centralisé sous `/api/v1`.
- `data.js` : fixture locale utilisée par les vues encore en mode prototype.
- `suspects-view.js`, `timeline-view.js`, `clues-view.js`,
  `verdict-view.js` : vues métier modulaires.
- `app.js` et `index.html` : navigation, assemblage du dashboard et interactions.

L'onglet des suspects consomme `GET /api/v1/f1/profiles`,
`GET /api/v1/f1/events` et `GET /api/v1/f1/graph` avec la graine `#AF42`.
