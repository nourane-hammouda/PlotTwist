package fr.miage.plottwist.investigation.domain.algo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.miage.plottwist.investigation.domain.model.Accusation;
import fr.miage.plottwist.investigation.domain.model.Declaration;
import fr.miage.plottwist.investigation.domain.model.GroundTruth;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AccusationValidatorTest {
    @Test
    void acceptsAccusationWhenThereIsAConflictAndTheDesignatedDeclarationIsFalse() {
        Declaration falseAlibi = declaration("s1", "salon", "21:00", "21:30", true);
        Declaration contradictingAlibi = declaration("s2", "salon", "21:10", "21:20", false);
        Accusation accusation = new Accusation(falseAlibi, contradictingAlibi, falseAlibi);
        GroundTruth groundTruth = new GroundTruth(Set.of(falseAlibi));

        assertThat(AccusationValidator.isCorrect(accusation, groundTruth)).isTrue();
    }

    @Test
    void rejectsAccusationWhenTheWrongDeclarationIsDesignated() {
        Declaration falseAlibi = declaration("s1", "salon", "21:00", "21:30", true);
        Declaration contradictingAlibi = declaration("s2", "salon", "21:10", "21:20", false);
        Accusation accusation = new Accusation(falseAlibi, contradictingAlibi, contradictingAlibi);
        GroundTruth groundTruth = new GroundTruth(Set.of(falseAlibi));

        assertThat(AccusationValidator.isCorrect(accusation, groundTruth)).isFalse();
    }

    @Test
    void rejectsAccusationWhenTheDeclarationsDoNotConflict() {
        Declaration falseAlibi = declaration("s1", "salon", "21:00", "21:10", false);
        Declaration otherAlibi = declaration("s2", "cuisine", "21:10", "21:20", false);
        Accusation accusation = new Accusation(falseAlibi, otherAlibi, falseAlibi);
        GroundTruth groundTruth = new GroundTruth(Set.of(falseAlibi));

        assertThat(AccusationValidator.isCorrect(accusation, groundTruth)).isFalse();
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
