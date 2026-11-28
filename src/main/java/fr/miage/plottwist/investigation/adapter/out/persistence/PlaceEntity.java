package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.domain.model.Place;
import fr.miage.plottwist.investigation.domain.model.PlaceId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entité JPA représentant la table {@code place}.
 * Adaptateur sortant — couche persistence uniquement, pas de logique métier.
 */
@Entity
@Table(name = "place")
public class PlaceEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    // Constructeur JPA
    protected PlaceEntity() {}

    public PlaceEntity(String id, String name, int floor, String type) {
        this.id    = id;
        this.name  = name;
        this.floor = floor;
        this.type  = type;
    }

    /** Convertit l'entité JPA en objet domaine. */
    public Place toDomain() {
        return Place.of(PlaceId.of(id), name, floor, type);
    }

    /** Crée une entité JPA à partir d'un objet domaine. */
    public static PlaceEntity fromDomain(Place place) {
        return new PlaceEntity(
                place.id().value(),
                place.name(),
                place.floor(),
                place.type()
        );
    }

    // Accesseurs (requis par JPA)
    public String getId()    { return id; }
    public String getName()  { return name; }
    public int    getFloor() { return floor; }
    public String getType()  { return type; }
}
