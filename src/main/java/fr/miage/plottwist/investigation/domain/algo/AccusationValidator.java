package fr.miage.plottwist.investigation.domain.algo;

import fr.miage.plottwist.investigation.domain.model.Accusation;
import fr.miage.plottwist.investigation.domain.model.GroundTruth;

import java.util.List;
import java.util.Objects;

public final class AccusationValidator {
    private AccusationValidator() {
    }

    public static boolean isCorrect(Accusation accusation, GroundTruth groundTruth) {
        Objects.requireNonNull(accusation, "accusation must not be null");
        Objects.requireNonNull(groundTruth, "groundTruth must not be null");

        boolean declarationsConflict = !InconsistencyDetector.detect(List.of(
                accusation.firstDeclaration(),
                accusation.secondDeclaration()
        )).isEmpty();
        boolean designatedLiarIsInGroundTruth =
                groundTruth.lyingDeclarations().contains(accusation.designatedLiar());

        return declarationsConflict && designatedLiarIsInGroundTruth;
    }
}
