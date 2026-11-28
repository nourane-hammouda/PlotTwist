package fr.miage.plottwist.investigation.adapter.in.web;

import fr.miage.plottwist.investigation.application.port.in.PlaceQueryPort;
import fr.miage.plottwist.investigation.domain.exception.PlaceNotFoundException;
import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;
import fr.miage.plottwist.investigation.domain.model.SpatialGraph;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôleur REST — Feature F2 : Plan des lieux et topologie.
 *
 * <ul>
 *   <li>GET /api/v1/places           → liste toutes les pièces</li>
 *   <li>GET /api/v1/places/{id}      → détail d'une pièce + voisins</li>
 *   <li>GET /api/v1/places/topology  → graphe complet (nœuds + arêtes)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceQueryPort placeQueryPort;

    public PlaceController(PlaceQueryPort placeQueryPort) {
        this.placeQueryPort = placeQueryPort;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/places
    // -------------------------------------------------------------------------

    @GetMapping
    public List<PlaceResponse> listAllPlaces() {
        return placeQueryPort.listAllPlaces()
                .stream()
                .map(PlaceResponse::fromDomain)
                .toList();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/places/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDetailResponse> getPlace(@PathVariable String id) {
        try {
            Place place         = placeQueryPort.getPlaceById(PlaceId.of(id));
            SpatialGraph graph  = placeQueryPort.loadSpatialGraph();
            Set<PlaceResponse> adjacent = graph.getAdjacentPlaces(place.id())
                    .stream()
                    .map(PlaceResponse::fromDomain)
                    .collect(Collectors.toSet());
            return ResponseEntity.ok(new PlaceDetailResponse(PlaceResponse.fromDomain(place), adjacent));
        } catch (PlaceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/places/topology
    // -------------------------------------------------------------------------

    @GetMapping("/topology")
    public TopologyResponse getTopology() {
        SpatialGraph graph = placeQueryPort.loadSpatialGraph();

        List<PlaceResponse> nodes = graph.getAllPlaces()
                .stream()
                .map(PlaceResponse::fromDomain)
                .toList();

        // Reconstruire les arêtes : pour chaque pièce, parcourir ses voisins
        List<EdgeResponse> edges = graph.getAllPlaces().stream()
                .flatMap(place -> graph.getAdjacentPlaces(place.id()).stream()
                        .filter(neighbor -> neighbor.id().value().compareTo(place.id().value()) > 0)
                        .map(neighbor -> new EdgeResponse(
                                place.id().value(),
                                neighbor.id().value(),
                                graph.getDirectDistance(place.id(), neighbor.id())
                        )))
                .toList();

        return new TopologyResponse(nodes, edges, graph.isConnected());
    }

    // -------------------------------------------------------------------------
    // Gestion des erreurs
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadId(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // =========================================================================
    // DTOs (records Java — immuables, sans logique métier)
    // =========================================================================

    /**
     * Réponse simplifiée d'une pièce.
     */
    public record PlaceResponse(String id, String name, int floor, String type) {
        static PlaceResponse fromDomain(Place p) {
            return new PlaceResponse(p.id().value(), p.name(), p.floor(), p.type());
        }
    }

    /**
     * Réponse détaillée d'une pièce avec ses voisins.
     */
    public record PlaceDetailResponse(PlaceResponse place, Set<PlaceResponse> adjacentPlaces) {}

    /**
     * Une arête du graphe avec le poids (distance).
     */
    public record EdgeResponse(String fromId, String toId, double distanceMetres) {}

    /**
     * Réponse complète du graphe topologique.
     */
    public record TopologyResponse(List<PlaceResponse> nodes, List<EdgeResponse> edges, boolean connected) {}
}
