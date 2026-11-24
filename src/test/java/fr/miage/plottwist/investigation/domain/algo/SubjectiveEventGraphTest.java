package fr.miage.plottwist.investigation.domain.algo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubjectiveEventGraphTest {
    @Test
    void rejectsAnEventOutsideTheOneToFourArityRange() {
        UUID declarer = UUID.randomUUID();

        assertThatThrownBy(() -> new SubjectiveEvent(
                UUID.randomUUID(), "#AF42", "Impossible", null, Instant.now(),
                SubjectiveEvent.SubjectType.ALIBI, declarer, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildsAParallelEdgeForEveryPairOfParticipants() {
        UUID lena = UUID.randomUUID();
        UUID marc = UUID.randomUUID();
        UUID ines = UUID.randomUUID();
        SubjectiveEvent event = new SubjectiveEvent(
                UUID.randomUUID(), "#AF42", "Conversation", null, Instant.now(),
                SubjectiveEvent.SubjectType.RELATION, lena, List.of(lena, marc, ines));
        SubjectiveEventGraph graph = new SubjectiveEventGraph();

        graph.addEvent(event);

        assertThat(graph.participantCount()).isEqualTo(3);
        assertThat(graph.eventRelationCount()).isEqualTo(3);
    }
}
