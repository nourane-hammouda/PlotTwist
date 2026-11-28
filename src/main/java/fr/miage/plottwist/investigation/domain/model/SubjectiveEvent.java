package fr.miage.plottwist.investigation.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record SubjectiveEvent(
        UUID id,
        String seed,
        String title,
        String description,
        Instant occurredAt,
        SubjectType subjectType,
        UUID declaredBy,
        List<UUID> participants) {

    public SubjectiveEvent {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(seed, "seed is required");
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        Objects.requireNonNull(subjectType, "subjectType is required");
        Objects.requireNonNull(declaredBy, "declaredBy is required");
        LinkedHashSet<UUID> uniqueParticipants = new LinkedHashSet<>(Objects.requireNonNull(participants, "participants is required"));
        if (uniqueParticipants.size() < 1 || uniqueParticipants.size() > 4) {
            throw new IllegalArgumentException("An event must involve between 1 and 4 distinct participants");
        }
        if (!uniqueParticipants.contains(declaredBy)) {
            throw new IllegalArgumentException("The declaring suspect must be one of the participants");
        }
        participants = List.copyOf(new ArrayList<>(uniqueParticipants));
    }

    public enum SubjectType {
        LOCATION, ACTIVITY, RELATION, ALIBI
    }
}
