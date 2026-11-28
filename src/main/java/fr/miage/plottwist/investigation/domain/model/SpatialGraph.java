package fr.miage.plottwist.investigation.domain.model;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Graphe connexe des pièces du manoir : nœuds = pièces, arêtes = passages pondérés par la distance.
 * Domaine pur — aucune dépendance Spring ou JPA.
 */
public class SpatialGraph {

    private final Graph<Place, DefaultWeightedEdge> graph;

    public SpatialGraph() {
        this.graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Ajoute une pièce au graphe (sans arête).
     *
     * @param place la pièce à ajouter
     * @throws IllegalArgumentException si la pièce est nulle ou déjà présente
     */
    public void addPlace(Place place) {
        if (place == null) {
            throw new IllegalArgumentException("La pièce ne peut pas être nulle.");
        }
        if (graph.containsVertex(place)) {
            throw new IllegalArgumentException("La pièce existe déjà : " + place.id());
        }
        graph.addVertex(place);
    }

    /**
     * Ajoute un passage entre deux pièces avec une distance en mètres.
     *
     * @param from            pièce de départ
     * @param to              pièce d'arrivée
     * @param distanceMetres  distance en mètres (doit être strictement positive)
     * @throws IllegalArgumentException si l'une des pièces est absente du graphe
     * @throws IllegalStateException    si la distance est négative ou nulle
     */
    public void addDoorway(Place from, Place to, double distanceMetres) {
        if (distanceMetres <= 0) {
            throw new IllegalStateException(
                    "La distance doit être strictement positive, reçu : " + distanceMetres);
        }
        if (!graph.containsVertex(from)) {
            throw new IllegalArgumentException("Pièce source absente du graphe : " + from.id());
        }
        if (!graph.containsVertex(to)) {
            throw new IllegalArgumentException("Pièce destination absente du graphe : " + to.id());
        }
        DefaultWeightedEdge edge = graph.addEdge(from, to);
        if (edge != null) {
            graph.setEdgeWeight(edge, distanceMetres);
        }
    }

    /**
     * Vérifie que le graphe est connexe (toutes les pièces sont accessibles depuis n'importe quelle autre).
     *
     * @return {@code true} si le graphe est connexe (ou vide), {@code false} sinon
     */
    public boolean isConnected() {
        if (graph.vertexSet().isEmpty()) {
            return true;
        }
        return new ConnectivityInspector<>(graph).isConnected();
    }

    /**
     * Retourne les pièces directement adjacentes à la pièce identifiée par {@code id}.
     *
     * @param id identifiant de la pièce centrale
     * @return ensemble des pièces voisines (vide si aucune)
     * @throws IllegalArgumentException si aucune pièce ne correspond à cet identifiant
     */
    public Set<Place> getAdjacentPlaces(PlaceId id) {
        Place place = findOrThrow(id);
        return graph.edgesOf(place).stream()
                .map(edge -> {
                    Place source = graph.getEdgeSource(edge);
                    Place target = graph.getEdgeTarget(edge);
                    return source.equals(place) ? target : source;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Retourne la distance directe entre deux pièces (poids de l'arête).
     *
     * @param a identifiant de la première pièce
     * @param b identifiant de la deuxième pièce
     * @return distance en mètres
     * @throws IllegalArgumentException si l'une des pièces est introuvable ou s'il n'existe pas de passage direct
     */
    public double getDirectDistance(PlaceId a, PlaceId b) {
        Place placeA = findOrThrow(a);
        Place placeB = findOrThrow(b);
        DefaultWeightedEdge edge = graph.getEdge(placeA, placeB);
        if (edge == null) {
            throw new IllegalArgumentException(
                    "Aucun passage direct entre " + a.value() + " et " + b.value());
        }
        return graph.getEdgeWeight(edge);
    }

    /**
     * Retourne toutes les pièces du graphe.
     */
    public Set<Place> getAllPlaces() {
        return Set.copyOf(graph.vertexSet());
    }

    /**
     * Retourne le nombre de pièces dans le graphe.
     */
    public int placeCount() {
        return graph.vertexSet().size();
    }

    // -------------------------------------------------------------------------
    // Méthodes internes
    // -------------------------------------------------------------------------

    private Place findOrThrow(PlaceId id) {
        return graph.vertexSet().stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pièce introuvable dans le graphe : " + id.value()));
    }
}
