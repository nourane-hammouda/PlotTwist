package fr.miage.plottwist.investigation.application.port.in;

import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;
import fr.miage.plottwist.investigation.domain.model.SpatialGraph;

import java.util.List;

/**
 * Port entrant : cas d'utilisation exposés par la feature F2 (Plan des lieux).
 * Implémenté par {@link fr.miage.plottwist.investigation.application.service.PlaceService}.
 */
public interface PlaceQueryPort {

    /** Retourne la liste de toutes les pièces du manoir. */
    List<Place> listAllPlaces();

    /**
     * Retourne une pièce par son identifiant.
     *
     * @throws fr.miage.plottwist.investigation.domain.exception.PlaceNotFoundException si introuvable
     */
    Place getPlaceById(PlaceId id);

    /** Charge le graphe spatial complet (pièces + passages pondérés). */
    SpatialGraph loadSpatialGraph();
}
