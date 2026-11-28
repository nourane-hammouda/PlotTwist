package fr.miage.plottwist.investigation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entité JPA représentant la table {@code doorway} (passage entre deux pièces).
 * Clé primaire composite : (place_a_id, place_b_id).
 */
@Entity
@Table(name = "doorway")
public class DoorwayEntity {

    @EmbeddedId
    private DoorwayId id;

    @Column(name = "distance_metres", nullable = false)
    private double distanceMetres;

    // Constructeur JPA
    protected DoorwayEntity() {}

    public DoorwayEntity(String placeAId, String placeBId, double distanceMetres) {
        this.id              = new DoorwayId(placeAId, placeBId);
        this.distanceMetres  = distanceMetres;
    }

    public DoorwayId getId()             { return id; }
    public double    getDistanceMetres() { return distanceMetres; }
    public String    getPlaceAId()       { return id.placeAId; }
    public String    getPlaceBId()       { return id.placeBId; }

    // -------------------------------------------------------------------------
    // Clé primaire composite
    // -------------------------------------------------------------------------

    @Embeddable
    public static class DoorwayId implements Serializable {

        @Column(name = "place_a_id", length = 100)
        private String placeAId;

        @Column(name = "place_b_id", length = 100)
        private String placeBId;

        protected DoorwayId() {}

        public DoorwayId(String placeAId, String placeBId) {
            this.placeAId = placeAId;
            this.placeBId = placeBId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DoorwayId that)) return false;
            return Objects.equals(placeAId, that.placeAId)
                    && Objects.equals(placeBId, that.placeBId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(placeAId, placeBId);
        }
    }
}
