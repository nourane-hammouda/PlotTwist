# PlotTwist

Application de génération et de résolution d’enquêtes policières interactives.

Projet de **Master 1 MIAGE Apprentissage**. Le dépôt principal est [nourane-hammouda/PlotTwist](https://github.com/nourane-hammouda/PlotTwist). Le projet est en phase de lancement ; le dossier R1 présente le périmètre, l’architecture prévue et les choix restant à valider.

## Dossier de lancement

- [Dossier de lancement — source Markdown](doc/dossier-lancement.md)
- [Dossier de lancement — PDF](doc/dossier-lancement.pdf)

Le dossier évoluera avec les livraisons du projet. Pour le recompiler après modification  :

```bash
make -C doc dossier
```

Cette commande nécessite **Pandoc** et **WeasyPrint**.

## Le projet

PlotTwist génère une enquête à partir de personnages, de lieux, de relations, d’événements et d’indices. Le joueur explore, interroge les suspects, puis formule une accusation.

L’objectif est de produire des enquêtes **cohérentes et résolubles**, et non de simplement afficher des histoires préécrites. Le moteur devra notamment respecter les contraintes de chronologie et de déplacement, éviter les contradictions entre témoignages et indices, et laisser plusieurs pistes plausibles au début de la partie.

## Équipe et comptes développeurs

Chaque membre contribue avec son propre compte développeur afin que les commits et les revues soient attribuables.

| Membre | Compte GitHub |
|---|---|
| Nourane Hammouda | [@nourane-hammouda](https://github.com/nourane-hammouda) |
| Chaïma Mabrouki | [@Chaimambk](https://github.com/Chaimambk) |
| Ornela Mothu | [@ornelamt](https://github.com/ornelamt) |
| Zaineb BenMahmoud | [@zainebbmn](https://github.com/zainebbmn) |

## Architecture envisagée

L’architecture suit le flux **front → API REST → back Java → base de données**. Le front communique avec le back uniquement au moyen de l’API REST.

```text
Front web
    │ HTTP / JSON
API REST — Spring Boot 3 / Spring MVC / OpenAPI
    │ cas d’utilisation
Back métier — Java 21, architecture hexagonale
    │ adaptateur de persistance
Base de données — PostgreSQL / Flyway
```

| Couche / besoin | Choix envisagé |
|---|---|
| Front | Web : HTML/CSS/JavaScript natif ou React/Vue, à décider |
| API REST | Spring Boot 3, Spring MVC, OpenAPI / Swagger ; endpoints sous `/api/v1`, erreurs RFC 9457 |
| Back | Java 21, architecture hexagonale ; JGraphT pour les graphes de relations et de lieux |
| Persistance | PostgreSQL, Spring Data JPA, Flyway ; une migration par feature |
| Tests et qualité | JUnit 5, Mockito, AssertJ, ArchUnit, Spring Boot Test, Testcontainers, JaCoCo ; tests unitaires, d’architecture, d’intégration et de validation métier |
| Livraison | GitHub Actions ; Docker Compose et conteneurisation prévues en v0.2.0 |

Le choix du framework front et le découpage fin du modèle métier seront validés sur les premières features. La répartition nominative des 12 features et le socle préalable v0.0 sont détaillés dans le [dossier de lancement](doc/dossier-lancement.md). `GameSession` est le pivot du parcours joueur, sans remplacer les entités métier distinctes comme `Investigation`, les événements, les suspects et les indices.

Un **squelette initial de packages Java** est présent dans `src/main/java/fr/miage/plottwist/`. Il sépare le domaine, les cas d’utilisation et les adaptateurs techniques selon une organisation inspirée de l’architecture hexagonale. Les packages ne contiennent pas encore de fonctionnalités métier ; le projet ne dispose pas encore de `pom.xml` ni d’un backend exécutable.

## Structure du dépôt

La structure distingue le backend, le futur frontend du jeu, le site de présentation et la documentation. Les répertoires encore vides sont conservés avec un fichier `.gitkeep` ; ils ne signifient pas que leur contenu est déjà implémenté.

```text
.
├── .github/
│   └── workflows/                 CI à mettre en place dans le socle v0.0
├── doc/                            dossier de lancement et diagrammes
├── front/                          future interface du jeu
├── site/                           future page de présentation du projet
└── src/
    ├── main/
    │   ├── java/fr/miage/plottwist/
    │   │                               packages du backend
    │   └── resources/db/migration/  futures migrations Flyway
    └── test/
        ├── java/fr/miage/plottwist/ tests Java
        └── resources/               fixtures et ressources de test
```

```text
fr.miage.plottwist/
├── api/                          points d’entrée techniques
├── config/                       configuration transverse
└── investigation/
    ├── domain/                   modèles et règles métier
    │   ├── model/
    │   ├── algo/
    │   └── exception/
    ├── application/              cas d’utilisation et ports
    │   ├── port/in/
    │   ├── port/out/
    │   └── service/
    ├── adapter/
    │   ├── in/web/               API REST
    │   └── out/persistence/      persistance
    └── config/                   assemblage du module
```

## Scénario principal

1. Le joueur démarre une nouvelle enquête.
2. Le moteur génère une enquête et vérifie sa cohérence.
3. Le joueur explore les lieux, collecte des indices et interroge les suspects.
4. Le joueur formule une accusation ; l’application compare sa proposition à la solution et explique le résultat.

## Versions envisagées

| Version cible | Objectif |
|---|---|
| **v0.0** | Socle technique commun et CI, préalable aux features et hors quota des 12 |
| **v0.1.0** | 4 features : graphe de suspects, chronologie, placement d’indices et accusation de base |
| **v0.2.0** | 4 features : choix coupable-mobile, trajets et alibis, scoring des fausses pistes, interrogatoire structuré |
| **v1.0.0** | 4 features : vérification de résolubilité, détection spatio-temporelle, aide par gain d’information, explication de l’accusation |

Chaque version comprend quatre features attribuées nominativement et développées de bout en bout. Une fixture et un contrat API par feature permettent de la tester et de la démontrer sans attendre les autres membres ; les objectifs détaillés et critères figurent dans le [dossier de lancement](doc/dossier-lancement.md).

## Démarrage et tests

Le dépôt est actuellement en phase de lancement : il contient le README, le dossier de lancement et un premier squelette de packages backend, mais pas encore d’implémentation applicative, de configuration Maven ni de scripts de démarrage et de tests. Les commandes de lancement seront ajoutées avec la mise en place du projet Java.

## Usage de l’intelligence artificielle

L’IA peut assister certaines tâches dans le respect de plafonds par compétence ; les décisions métier restent humaines et chaque usage est tracé et vérifié. Aucun secret ni donnée personnelle ne doit être transmis à un assistant. La politique, les objectifs et le suivi de la part de l’IA sont décrits dans le [dossier de lancement](doc/dossier-lancement.md).

## Licence

La licence du projet reste à définir.
