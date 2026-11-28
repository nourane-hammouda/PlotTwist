package fr.miage.plottwist.investigation.domain.algo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.miage.plottwist.investigation.domain.model.Conflict;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import fr.miage.plottwist.investigation.domain.model.Declaration;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InconsistencyDetectorTest {
    @Test
    void detectsUbiquityWhenOneSuspectClaimsDifferentPlacesAtOverlappingTimes() {
        Declaration first = declaration("s1", "salon", "21:00", "21:30", false);
        Declaration second = declaration("s1", "cuisine", "21:20", "21:40", false);

        List<Conflict> conflicts = InconsistencyDetector.detect(List.of(first, second));

        assertThat(conflicts)
                .extracting(Conflict::type)
                .containsExactly(ConflictType.UBIQUITY);
    }

    @Test
    void detectsContradictedSolitudeWhenAnotherSuspectIsAtTheSamePlace() {
        Declaration alone = declaration("s1", "salon", "21:00", "21:30", true);
        Declaration other = declaration("s2", "salon", "21:10", "21:20", false);

        List<Conflict> conflicts = InconsistencyDetector.detect(List.of(alone, other));

        assertThat(conflicts)
                .extracting(Conflict::type)
                .containsExactly(ConflictType.SOLITUDE_CONTREDITE);
    }

    @Test
    void doesNotDetectConflictWhenTimeSlotsOnlyTouch() {
        Declaration first = declaration("s1", "salon", "21:00", "21:10", false);
        Declaration second = declaration("s1", "cuisine", "21:10", "21:20", false);

        assertThat(InconsistencyDetector.detect(List.of(first, second))).isEmpty();
    }

    private static Declaration declaration(
            String suspectId,
            String placeId,
            String start,
            String end,
            boolean claimsAlone
    ) {
        return new Declaration(
                suspectId,
                placeId,
                LocalTime.parse(start),
                LocalTime.parse(end),
                claimsAlone
        );
    }
}
