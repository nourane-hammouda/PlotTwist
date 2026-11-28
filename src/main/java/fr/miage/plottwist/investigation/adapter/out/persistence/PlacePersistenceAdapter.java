package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.PlacePersistencePort;
import fr.miage.plottwist.investigation.application.port.out.SpatialGraphPersistencePort;
import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;
import fr.miage.plottwist.investigation.domain.model.SpatialGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptateur sortant JPA — implémente les deux ports de persistence de la feature F2.
 */
@Component
public class PlacePersistenceAdapter implements PlacePersistencePort, SpatialGraphPersistencePort {

    private final PlaceRepository   placeRepository;
    private final DoorwayRepository doorwayRepository;

    public PlacePersistenceAdapter(PlaceRepository placeRepository,
                                   DoorwayRepository doorwayRepository) {
        this.placeRepository   = placeRepository;
        this.doorwayRepository = doorwayRepository;
    }

    // -------------------------------------------------------------------------
    // PlacePersistencePort
    // -------------------------------------------------------------------------

    @Override
    public List<Place> findAll() {
        return placeRepository.findAll()
                .stream()
                .map(PlaceEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Place> findById(PlaceId id) {
        return placeRepository.findById(id.value())
                .map(PlaceEntity::toDomain);
    }

    @Override
    public void save(Place place) {
        placeRepository.save(PlaceEntity.fromDomain(place));
    }

    // -------------------------------------------------------------------------
    // SpatialGraphPersistencePort
    // -------------------------------------------------------------------------

    @Override
    public SpatialGraph loadGraph() {
        SpatialGraph graph = new SpatialGraph();

        // 1. Ajouter toutes les pièces
        List<PlaceEntity> placeEntities = placeRepository.findAll();
        List<Place> places = placeEntities.stream()
                .map(PlaceEntity::toDomain)
                .toList();
        places.forEach(graph::addPlace);

        // 2. Construire une map id → Place pour retrouver les objets domaine
        java.util.Map<String, Place> byId = places.stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.id().value(), p -> p));

        // 3. Ajouter tous les passages
        doorwayRepository.findAll().forEach(doorway -> {
            Place a = byId.get(doorway.getPlaceAId());
            Place b = byId.get(doorway.getPlaceBId());
            if (a != null && b != null) {
                graph.addDoorway(a, b, doorway.getDistanceMetres());
            }
        });

        return graph;
    }
}
