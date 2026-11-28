package fr.miage.plottwist.investigation.domain.algo;

import fr.miage.plottwist.investigation.domain.model.Conflict;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import fr.miage.plottwist.investigation.domain.model.Declaration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class InconsistencyDetector {
    private InconsistencyDetector() {
    }

    public static List<Conflict> detect(List<Declaration> declarations) {
        Objects.requireNonNull(declarations, "declarations must not be null");
        List<Declaration> sorted = new ArrayList<>(List.copyOf(declarations));
        sorted.sort(Comparator.comparing(Declaration::startTime));

        List<Declaration> active = new ArrayList<>();
        List<Conflict> conflicts = new ArrayList<>();

        for (Declaration current : sorted) {
            active.removeIf(declaration -> !declaration.endTime().isAfter(current.startTime()));
            for (Declaration inProgress : active) {
                detectConflict(inProgress, current, conflicts);
            }
            active.add(current);
        }

        return List.copyOf(conflicts);
    }

    private static void detectConflict(
            Declaration first,
            Declaration second,
            List<Conflict> conflicts
    ) {
        boolean sameSuspect = first.suspectId().equals(second.suspectId());
        boolean samePlace = first.placeId().equals(second.placeId());

        if (sameSuspect && !samePlace) {
            conflicts.add(new Conflict(ConflictType.UBIQUITY, first, second));
        } else if (!sameSuspect && samePlace
                && (first.claimsAlone() || second.claimsAlone())) {
            conflicts.add(new Conflict(ConflictType.SOLITUDE_CONTREDITE, first, second));
        }
    }
}
