package fr.miage.plottwist.investigation.application.port.out;

import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;

import java.util.List;
import java.util.Optional;

/**
 * Port sortant : accès à la persistance des pièces.
 * Implémenté par l'adaptateur JPA (couche persistence).
 */
public interface PlacePersistencePort {

    /** Récupère toutes les pièces stockées. */
    List<Place> findAll();

    /** Récupère une pièce par son identifiant, ou vide si absente. */
    Optional<Place> findById(PlaceId id);

    /** Persiste une pièce (création ou mise à jour). */
    void save(Place place);
}
