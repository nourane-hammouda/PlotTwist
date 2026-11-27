package fr.miage.plottwist.investigation.application.port.in;

import fr.miage.plottwist.investigation.domain.model.ConflictType;
import java.util.List;

public interface F4UseCases {
    List<String> findAvailableSeeds();

    InvestigationView findInvestigation(String seed);

    InvestigationView generateInvestigation();

    List<DeclarationView> findDeclarations(String seed);

    List<ConflictView> findConflicts(String seed);

    boolean validateAccusation(
            String seed,
            String firstDeclarationId,
            String secondDeclarationId,
            String designatedLiarDeclarationId);

    DiscoveryView validateDiscovery(
            String seed,
            String firstDeclarationId,
            String secondDeclarationId,
            ConflictType conflictType,
            String designatedLiarDeclarationId);

    record InvestigationView(String seed, String title, String introduction, int discoveryCount) {}

    record DiscoveryView(
            boolean correct,
            String discoveryId,
            int discoveryOrder,
            int discoveryCount,
            String discoveryText,
            ConflictView conflict) {}

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
