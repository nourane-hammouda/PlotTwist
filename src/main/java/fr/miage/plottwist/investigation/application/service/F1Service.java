package fr.miage.plottwist.investigation.application.service;

import fr.miage.plottwist.investigation.application.port.in.F1UseCases;
import fr.miage.plottwist.investigation.application.port.out.SubjectiveEventRepository;
import fr.miage.plottwist.investigation.application.port.out.SuspectProfileRepository;
import fr.miage.plottwist.investigation.domain.algo.SubjectiveEventGraph;
import fr.miage.plottwist.investigation.domain.exception.ResourceNotFoundException;
import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class F1Service implements F1UseCases {
    private final SuspectProfileRepository profiles;
    private final SubjectiveEventRepository events;

    public F1Service(SuspectProfileRepository profiles, SubjectiveEventRepository events) {
        this.profiles = profiles;
        this.events = events;
    }

    @Override
    public List<SuspectProfile> findProfiles(String seed) {
        return profiles.findBySeed(seed);
    }

    @Override
    public SuspectProfile findProfile(String seed, UUID id) {
        return profiles.findByIdAndSeed(id, seed)
                .orElseThrow(() -> new ResourceNotFoundException("Suspect profile not found: " + id));
    }

    @Override
    public SuspectProfile createProfile(String seed, String name, String role, String biography) {
        return profiles.save(new SuspectProfile(UUID.randomUUID(), seed, name, role, biography));
    }

    @Override
    public SubjectiveEvent createEvent(
            String seed,
            String title,
            String description,
            Instant occurredAt,
            SubjectiveEvent.SubjectType subjectType,
            UUID declaredBy,
            List<UUID> participants) {
        findProfile(seed, declaredBy);
        for (UUID participant : participants) {
            findProfile(seed, participant);
        }
        SubjectiveEvent event = new SubjectiveEvent(
                UUID.randomUUID(), seed, title, description, occurredAt, subjectType, declaredBy, participants);
        return events.save(event);
    }

    @Override
    public List<SubjectiveEvent> findEvents(String seed) {
        return events.findBySeed(seed);
    }

    @Override
    public F1GraphSummary graphSummary(String seed) {
        SubjectiveEventGraph graph = new SubjectiveEventGraph();
        List<SubjectiveEvent> seedEvents = findEvents(seed);
        seedEvents.forEach(graph::addEvent);
        int suspectCount = profiles.findBySeed(seed).size();
        return new F1GraphSummary(suspectCount, seedEvents.size(), graph.eventRelationCount());
    }
}
