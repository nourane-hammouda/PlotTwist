package fr.miage.plottwist.investigation.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpatialGraph — domaine F2")
class SpatialGraphTest {

    private SpatialGraph graph;

    // Pièces de test
    private Place salon;
    private Place cuisine;
    private Place bureau;
    private Place cave;
    private Place grenier;

    @BeforeEach
    void setUp() {
        graph = new SpatialGraph();

        salon   = Place.of(PlaceId.of("salon"),   "Grand Salon",    0, "reception");
        cuisine = Place.of(PlaceId.of("cuisine"),  "Cuisine",        0, "service");
        bureau  = Place.of(PlaceId.of("bureau"),   "Bureau",         1, "prive");
        cave    = Place.of(PlaceId.of("cave"),     "Cave",          -1, "stockage");
        grenier = Place.of(PlaceId.of("grenier"),  "Grenier",        2, "stockage");
    }

    // -------------------------------------------------------------------------
    // Connexité
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Un manoir avec 5 pièces toutes reliées est connexe")
    void manoirConnecte_cinqPieces() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addPlace(bureau);
        graph.addPlace(cave);
        graph.addPlace(grenier);

        graph.addDoorway(salon,   cuisine, 5.0);
        graph.addDoorway(salon,   bureau,  12.0);
        graph.addDoorway(cuisine, cave,    3.5);
        graph.addDoorway(bureau,  grenier, 8.0);

        assertThat(graph.isConnected()).isTrue();
    }

    @Test
    @DisplayName("Ajouter une pièce isolée rend le graphe non connexe")
    void pieceIsolee_grapheNonConnexe() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addPlace(bureau); // isolée

        graph.addDoorway(salon, cuisine, 5.0);

        assertThat(graph.isConnected()).isFalse();
    }

    @Test
    @DisplayName("Un graphe vide est considéré connexe")
    void grapheVide_estConnexe() {
        assertThat(graph.isConnected()).isTrue();
    }

    @Test
    @DisplayName("Un graphe avec une seule pièce est connexe")
    void grapheUnePiece_estConnexe() {
        graph.addPlace(salon);
        assertThat(graph.isConnected()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Distance négative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Une distance négative lève IllegalStateException")
    void distanceNegative_leveException() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);

        assertThatIllegalStateException()
                .isThrownBy(() -> graph.addDoorway(salon, cuisine, -1.0))
                .withMessageContaining("-1.0");
    }

    @Test
    @DisplayName("Une distance nulle lève IllegalStateException")
    void distanceNulle_leveException() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);

        assertThatIllegalStateException()
                .isThrownBy(() -> graph.addDoorway(salon, cuisine, 0.0));
    }

    // -------------------------------------------------------------------------
    // Voisins
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAdjacentPlaces retourne les bons voisins directs")
    void getAdjacentPlaces_retourneVoisinsCorrects() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addPlace(bureau);
        graph.addPlace(cave);

        graph.addDoorway(salon, cuisine, 5.0);
        graph.addDoorway(salon, bureau,  12.0);
        graph.addDoorway(cuisine, cave,  3.5);

        Set<Place> voisinsSalon = graph.getAdjacentPlaces(PlaceId.of("salon"));
        assertThat(voisinsSalon)
                .containsExactlyInAnyOrder(cuisine, bureau)
                .doesNotContain(cave);
    }

    @Test
    @DisplayName("getAdjacentPlaces fonctionne dans les deux sens (graphe non orienté)")
    void getAdjacentPlaces_bidirectionnel() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addDoorway(salon, cuisine, 5.0);

        assertThat(graph.getAdjacentPlaces(PlaceId.of("cuisine")))
                .containsExactly(salon);
    }

    @Test
    @DisplayName("getAdjacentPlaces lève une exception si la pièce est inconnue")
    void getAdjacentPlaces_pieceInconnue_leveException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> graph.getAdjacentPlaces(PlaceId.of("inexistant")));
    }

    // -------------------------------------------------------------------------
    // Distance directe
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getDirectDistance retourne la distance correcte")
    void getDirectDistance_retourneDistanceCorrecte() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addDoorway(salon, cuisine, 7.5);

        assertThat(graph.getDirectDistance(PlaceId.of("salon"), PlaceId.of("cuisine")))
                .isEqualTo(7.5);
    }

    @Test
    @DisplayName("getDirectDistance est symétrique")
    void getDirectDistance_symetrique() {
        graph.addPlace(salon);
        graph.addPlace(bureau);
        graph.addDoorway(salon, bureau, 10.0);

        assertThat(graph.getDirectDistance(PlaceId.of("bureau"), PlaceId.of("salon")))
                .isEqualTo(10.0);
    }

    @Test
    @DisplayName("getDirectDistance lève une exception si pas de passage direct")
    void getDirectDistance_sansAreteDirecte_leveException() {
        graph.addPlace(salon);
        graph.addPlace(cave);
        // pas d'arête entre salon et cave

        assertThatIllegalArgumentException()
                .isThrownBy(() -> graph.getDirectDistance(PlaceId.of("salon"), PlaceId.of("cave")));
    }

    // -------------------------------------------------------------------------
    // placeCount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("placeCount retourne le bon nombre de pièces")
    void placeCount_retourneNombreCorrect() {
        graph.addPlace(salon);
        graph.addPlace(cuisine);
        graph.addPlace(bureau);

        assertThat(graph.placeCount()).isEqualTo(3);
    }
}
