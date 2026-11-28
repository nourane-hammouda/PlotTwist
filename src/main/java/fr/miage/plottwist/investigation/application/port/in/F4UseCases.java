package fr.miage.plottwist.investigation.application.port.in;

import fr.miage.plottwist.investigation.domain.model.ConflictType;
import java.util.List;

public interface F4UseCases {
    List<DeclarationView> findDeclarations();

    List<ConflictView> findConflicts();

    boolean validateAccusation(
            String firstDeclarationId,
            String secondDeclarationId,
            String designatedLiarDeclarationId);

    record DeclarationView(
            String id,
            String suspectId,
            String suspectName,
            String placeId,
            String placeName,
            String startTime,
            String endTime,
            boolean claimsAlone) {}

    record ConflictView(
            ConflictType type,
            String firstDeclarationId,
            String secondDeclarationId,
            String overlapStartTime,
            String overlapEndTime,
            String explanation) {}
}
