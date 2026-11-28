package fr.miage.plottwist.investigation.domain.model;

import java.util.Objects;
import java.util.Set;

public record GroundTruth(Set<Declaration> lyingDeclarations) {
    public GroundTruth {
        Objects.requireNonNull(lyingDeclarations, "lyingDeclarations must not be null");
        lyingDeclarations = Set.copyOf(lyingDeclarations);
    }
}
