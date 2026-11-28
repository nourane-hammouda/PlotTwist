package fr.miage.plottwist.investigation.application.service;

import fr.miage.plottwist.investigation.application.port.in.PlaceQueryPort;
import fr.miage.plottwist.investigation.application.port.out.PlacePersistencePort;
import fr.miage.plottwist.investigation.application.port.out.SpatialGraphPersistencePort;
import fr.miage.plottwist.investigation.domain.exception.PlaceNotFoundException;
import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;
import fr.miage.plottwist.investigation.domain.model.SpatialGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service applicatif de la feature F2 — Plan des lieux et topologie.
 * Orchestre les ports sans contenir de logique métier (celle-ci est dans le domaine).
 */
@Service
@Transactional(readOnly = true)
public class PlaceService implements PlaceQueryPort {

    private final PlacePersistencePort        placePersistencePort;
    private final SpatialGraphPersistencePort graphPersistencePort;

    public PlaceService(PlacePersistencePort placePersistencePort,
                        SpatialGraphPersistencePort graphPersistencePort) {
        this.placePersistencePort = placePersistencePort;
        this.graphPersistencePort = graphPersistencePort;
    }

    @Override
    public List<Place> listAllPlaces() {
        return placePersistencePort.findAll();
    }

    @Override
    public Place getPlaceById(PlaceId id) {
        return placePersistencePort.findById(id)
                .orElseThrow(() -> new PlaceNotFoundException(id.value()));
    }

    @Override
    public SpatialGraph loadSpatialGraph() {
        return graphPersistencePort.loadGraph();
    }
}
