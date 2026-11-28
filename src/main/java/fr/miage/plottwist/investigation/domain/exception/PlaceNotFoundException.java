package fr.miage.plottwist.investigation.domain.exception;

/**
 * Exception levée quand une pièce demandée est introuvable dans le système.
 */
public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException(String placeId) {
        super("Pièce introuvable : " + placeId);
    }
}
