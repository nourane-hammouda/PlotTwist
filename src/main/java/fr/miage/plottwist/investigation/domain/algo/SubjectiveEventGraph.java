package fr.miage.plottwist.investigation.domain.algo;

import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jgrapht.graph.Multigraph;

public class SubjectiveEventGraph {
    private final Multigraph<UUID, UUID> graph =
            new Multigraph<>(UUID::randomUUID, UUID::randomUUID, false);
    private final Map<UUID, UUID> relationToEvent = new HashMap<>();

    public void addEvent(SubjectiveEvent event) {
        event.participants().forEach(graph::addVertex);

        List<UUID> participants = event.participants();
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                UUID relationId = UUID.randomUUID();
                graph.addEdge(participants.get(i), participants.get(j), relationId);
                relationToEvent.put(relationId, event.id());
            }
        }
    }

    public int participantCount() {
        return graph.vertexSet().size();
    }

    public int eventRelationCount() {
        return graph.edgeSet().size();
    }

    public boolean isDeclaredByInEvent(UUID suspectId, UUID eventId) {
        return graph.edgesOf(suspectId).stream()
                .map(relationToEvent::get)
                .anyMatch(eventId::equals);
    }
}
