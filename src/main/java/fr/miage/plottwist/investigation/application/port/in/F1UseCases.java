package fr.miage.plottwist.investigation.application.port.in;

import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import java.util.List;
import java.util.UUID;

public interface F1UseCases {
    List<SuspectProfile> findProfiles(String seed);

    SuspectProfile findProfile(String seed, UUID id);

    SuspectProfile createProfile(String seed, String name, String role, String biography);

    SubjectiveEvent createEvent(
            String seed,
            String title,
            String description,
            java.time.Instant occurredAt,
            SubjectiveEvent.SubjectType subjectType,
            UUID declaredBy,
            List<UUID> participants);

    List<SubjectiveEvent> findEvents(String seed);

    F1GraphSummary graphSummary(String seed);

    record F1GraphSummary(int suspectCount, int eventCount, int relationCount) {}
}
