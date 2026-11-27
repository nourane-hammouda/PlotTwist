package fr.miage.plottwist.investigation.domain.model;

import java.util.Objects;

public record Accusation(
        Declaration firstDeclaration,
        Declaration secondDeclaration,
        Declaration designatedLiar
) {
    public Accusation {
        Objects.requireNonNull(firstDeclaration, "firstDeclaration must not be null");
        Objects.requireNonNull(secondDeclaration, "secondDeclaration must not be null");
        Objects.requireNonNull(designatedLiar, "designatedLiar must not be null");

        if (firstDeclaration.equals(secondDeclaration)) {
            throw new IllegalArgumentException("An accusation requires two distinct declarations");
        }
        if (!designatedLiar.equals(firstDeclaration) && !designatedLiar.equals(secondDeclaration)) {
            throw new IllegalArgumentException("The designated liar must be one of the selected declarations");
        }
    }
}
