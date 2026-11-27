package fr.miage.plottwist.investigation.domain.model;

import java.util.Objects;

public record Conflict(ConflictType type, Declaration first, Declaration second) {
    public Conflict {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");

        if (first.equals(second)) {
            throw new IllegalArgumentException("A conflict requires two distinct declarations");
        }

        boolean overlaps = first.startTime().isBefore(second.endTime())
                && second.startTime().isBefore(first.endTime());
        if (!overlaps) {
            throw new IllegalArgumentException("Conflicting declarations must overlap");
        }

        boolean sameSuspect = first.suspectId().equals(second.suspectId());
        boolean samePlace = first.placeId().equals(second.placeId());
        boolean validType = switch (type) {
            case UBIQUITY -> sameSuspect && !samePlace;
            case SOLITUDE_CONTREDITE -> !sameSuspect && samePlace
                    && (first.claimsAlone() || second.claimsAlone());
        };
        if (!validType) {
            throw new IllegalArgumentException("Declarations do not satisfy the conflict type");
        }
    }
}
