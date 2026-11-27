package fr.miage.plottwist.investigation.domain.model;

import java.time.LocalTime;
import java.util.Objects;

public record Declaration(
        String suspectId,
        String placeId,
        LocalTime startTime,
        LocalTime endTime,
        boolean claimsAlone
) {
    public Declaration {
        Objects.requireNonNull(suspectId, "suspectId must not be null");
        Objects.requireNonNull(placeId, "placeId must not be null");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (suspectId.isBlank()) {
            throw new IllegalArgumentException("suspectId must not be blank");
        }
        if (placeId.isBlank()) {
            throw new IllegalArgumentException("placeId must not be blank");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
