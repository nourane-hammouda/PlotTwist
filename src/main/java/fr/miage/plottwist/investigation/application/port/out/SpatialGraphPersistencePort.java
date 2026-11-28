package fr.miage.plottwist.investigation.application.port.out;

import fr.miage.plottwist.investigation.domain.model.SpatialGraph;

/**
 * Port sortant : chargement du graphe spatial depuis la persistance.
 */
public interface SpatialGraphPersistencePort {

    /**
     * Charge le graphe complet (pièces + passages) depuis la base de données.
     *
     * @return un {@link SpatialGraph} initialisé avec toutes les pièces et arêtes
     */
    SpatialGraph loadGraph();
}
