package fr.miage.plottwist.investigation.domain.model;

import java.util.Objects;
import java.util.UUID;

public record SuspectProfile(UUID id, String seed, String name, String role, String biography) {
    public SuspectProfile {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(seed, "seed is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(biography, "biography is required");
    }
}
