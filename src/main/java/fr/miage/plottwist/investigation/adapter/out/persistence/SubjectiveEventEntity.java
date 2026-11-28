package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subjective_event")
public class SubjectiveEventEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 16)
    private String seed;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 32)
    private SubjectiveEvent.SubjectType subjectType;

    @Column(name = "declared_by", nullable = false)
    private UUID declaredBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "subjective_event_participant",
            joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "suspect_id", nullable = false)
    private List<UUID> participants = new ArrayList<>();

    protected SubjectiveEventEntity() {}

    public SubjectiveEventEntity(
            UUID id, String seed, String title, String description, Instant occurredAt,
            SubjectiveEvent.SubjectType subjectType, UUID declaredBy, List<UUID> participants) {
        this.id = id;
        this.seed = seed;
        this.title = title;
        this.description = description;
        this.occurredAt = occurredAt;
        this.subjectType = subjectType;
        this.declaredBy = declaredBy;
        this.participants = new ArrayList<>(participants);
    }

    public UUID getId() { return id; }
    public String getSeed() { return seed; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Instant getOccurredAt() { return occurredAt; }
    public SubjectiveEvent.SubjectType getSubjectType() { return subjectType; }
    public UUID getDeclaredBy() { return declaredBy; }
    public List<UUID> getParticipants() { return participants; }
}
