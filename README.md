# PlotTwist

Application de génération et de résolution d’enquêtes policières interactives.

Projet de **Master 1 MIAGE Apprentissage**. Le dépôt principal est [nourane-hammouda/PlotTwist](https://github.com/nourane-hammouda/PlotTwist). Le projet est en phase de lancement ; le dossier R1 présente le périmètre, l’architecture prévue et les choix restant à valider.

## Dossier de lancement

- [Dossier de lancement — source Markdown](doc/dossier-lancement.md)
- [Dossier de lancement — PDF](doc/dossier-lancement.pdf)

Le dossier évoluera avec les livraisons du projet. Pour le recompiler après modification :

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

Le front communique avec le back uniquement au moyen de l’API REST.

```text
Front web (framework à choisir)
       │ HTTP / JSON
API REST (Spring Boot / Spring MVC)
       │
Back métier (Java 21)
       │ JPA
Base de données (PostgreSQL)
```

| Couche / besoin | Technologie envisagée |
|---|---|
| Front | Application web ; React, Vue.js ou Thymeleaf à choisir |
| API REST et back | Java 21, Spring Boot 3, Spring MVC, Maven |
| Persistance | PostgreSQL, Spring Data JPA / Hibernate, Flyway |
| Algorithmes de graphes | JGraphT |
| Tests | JUnit 5, Mockito, AssertJ, Testcontainers |
| Qualité et livraison | JaCoCo, GitHub Actions |
| Conteneurisation | Docker Compose, prévue pour la v0.2.0 |

Les technologies et le détail du modèle métier restent à confirmer lors des premières features. La répartition nominative des axes fonctionnels n’a pas encore été décidée.

## Scénario principal

1. Le joueur démarre une nouvelle enquête.
2. Le moteur génère une enquête et vérifie sa cohérence.
3. Le joueur explore les lieux, collecte des indices et interroge les suspects.
4. Le joueur formule une accusation ; l’application compare sa proposition à la solution et explique le résultat.

## Versions envisagées

| Version cible | Objectif |
|---|---|
| **v0.1.0** | Boucle jouable minimale : génération, chronologie, indices et accusation |
| **v0.2.0** | Approfondissement des contraintes, déplacements et interrogatoires ; conteneurisation |
| **v1.0.0** | Validation de la résolubilité, détection de contradictions, aide au joueur et stabilisation |

Chaque itération prévoit une feature algorithmique par membre, développée de bout en bout. La démarche retenue est : **idéation → back → tests → API REST → front**. Les objectifs détaillés et les scénarios figurent dans le [dossier de lancement](doc/dossier-lancement.md).

## Démarrage et tests

Le dépôt est actuellement en phase de lancement : il contient le README et le dossier de lancement, mais pas encore le code applicatif ni les scripts de démarrage et de tests. Les commandes de lancement seront ajoutées dès que le squelette de l’application sera intégré.

## Usage de l’intelligence artificielle

L’IA peut assister la conception, l’explication, les tests et la documentation ; chaque membre reste responsable du code livré et doit pouvoir l’expliquer. Aucun secret ni donnée personnelle ne doit être transmis à un assistant. Le protocole détaillé et le suivi de la part de l’IA sont décrits dans le [dossier de lancement](doc/dossier-lancement.md).

## Licence

La licence du projet reste à définir.
