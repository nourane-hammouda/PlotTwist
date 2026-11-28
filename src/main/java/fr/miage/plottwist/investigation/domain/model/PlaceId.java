package fr.miage.plottwist.investigation.domain.model;

/**
 * Identifiant immuable d'une pièce du lieu de l'enquête.
 *
 * <p>Utilise un record Java 21 pour garantir l'immutabilité,
 * l'égalité structurelle et un {@code toString()} lisible sans code boilerplate.</p>
 *
 * <p>Exemples : {@code PlaceId.of("salon")}, {@code PlaceId.of("bibliotheque")}.</p>
 */
public record PlaceId(String value) {

    /**
     * Crée un identifiant de pièce.
     *
     * @param value identifiant textuel non nul et non vide
     * @throws IllegalArgumentException si {@code value} est nul ou vide
     */
    public PlaceId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Un identifiant de pièce ne peut pas être vide.");
        }
        value = value.strip().toLowerCase();
    }

    /**
     * Méthode de fabrique pour plus de lisibilité dans le code appelant.
     *
     * @param value identifiant textuel
     * @return un nouveau {@code PlaceId}
     */
    public static PlaceId of(String value) {
        return new PlaceId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
