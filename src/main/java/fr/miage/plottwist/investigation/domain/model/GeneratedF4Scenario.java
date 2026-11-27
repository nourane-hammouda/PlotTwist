package fr.miage.plottwist.investigation.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GeneratedF4Scenario(
        String seed,
        String title,
        String introduction,
        List<GeneratedDeclaration> declarations,
        Set<String> lyingDeclarationIds
) {
    public GeneratedF4Scenario {
        Objects.requireNonNull(seed, "seed must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(introduction, "introduction must not be null");
        Objects.requireNonNull(declarations, "declarations must not be null");
        Objects.requireNonNull(lyingDeclarationIds, "lyingDeclarationIds must not be null");
        if (seed.isBlank() || title.isBlank() || introduction.isBlank()) {
            throw new IllegalArgumentException("Scenario text and seed must not be blank");
        }

        declarations = List.copyOf(declarations);
        lyingDeclarationIds = Set.copyOf(lyingDeclarationIds);
        Set<String> declarationIds = declarations.stream()
                .map(GeneratedDeclaration::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!declarationIds.containsAll(lyingDeclarationIds)) {
            throw new IllegalArgumentException("Every lying declaration must belong to the scenario");
        }
    }

    public record GeneratedDeclaration(
            String id,
            String suspectName,
            String placeName,
            Declaration statement
    ) {
        public GeneratedDeclaration {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(suspectName, "suspectName must not be null");
            Objects.requireNonNull(placeName, "placeName must not be null");
            Objects.requireNonNull(statement, "statement must not be null");
            if (id.isBlank() || suspectName.isBlank() || placeName.isBlank()) {
                throw new IllegalArgumentException("Generated declaration fields must not be blank");
            }
        }
    }
}
