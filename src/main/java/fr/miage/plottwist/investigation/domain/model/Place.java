package fr.miage.plottwist.investigation.domain.model;

/**
 * Représente une pièce du lieu de l'enquête (manoir, théâtre, etc.).
 *
 * <p>Objet du domaine pur : aucune dépendance vers Spring ou JPA.
 * Utilisé directement par {@link SpatialGraph} comme nœud du graphe.</p>
 *
 * @param id    identifiant unique de la pièce
 * @param name  nom affiché au joueur (ex : "Grande Bibliothèque")
 * @param floor étage où se trouve la pièce (0 = rez-de-chaussée)
 * @param type  catégorie de la pièce (salon, cuisine, bureau…)
 */
public record Place(PlaceId id, String name, int floor, String type) {

    /**
     * Validation à la construction.
     */
    public Place {
        if (id == null)   throw new IllegalArgumentException("L'identifiant de la pièce est obligatoire.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Le nom de la pièce est obligatoire.");
        if (floor < 0)    throw new IllegalArgumentException("L'étage ne peut pas être négatif.");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Le type de la pièce est obligatoire.");
        name = name.strip();
        type = type.strip().toLowerCase();
    }

    /**
     * Méthode de fabrique courte pour le code de test et la génération.
     */
    public static Place of(String id, String name, int floor, String type) {
        return new Place(PlaceId.of(id), name, floor, type);
    }

    @Override
    public String toString() {
        return name + " [" + id + ", étage " + floor + ", " + type + "]";
    }
}
