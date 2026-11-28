package fr.miage.plottwist.investigation.application.service;

import fr.miage.plottwist.investigation.application.port.in.F4UseCases;
import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository;
import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository.F4DeclarationRecord;
import fr.miage.plottwist.investigation.application.port.out.F4GroundTruthRepository;
import fr.miage.plottwist.investigation.config.F4Fixture;
import fr.miage.plottwist.investigation.domain.algo.AccusationValidator;
import fr.miage.plottwist.investigation.domain.algo.InconsistencyDetector;
import fr.miage.plottwist.investigation.domain.exception.F4DeclarationNotFoundException;
import fr.miage.plottwist.investigation.domain.model.Accusation;
import fr.miage.plottwist.investigation.domain.model.Conflict;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import fr.miage.plottwist.investigation.domain.model.Declaration;
import fr.miage.plottwist.investigation.domain.model.GroundTruth;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class F4Service implements F4UseCases {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final F4DeclarationRepository declarations;
    private final F4GroundTruthRepository groundTruth;

    public F4Service(
            F4DeclarationRepository declarations,
            F4GroundTruthRepository groundTruth) {
        this.declarations = declarations;
        this.groundTruth = groundTruth;
    }

    @Override
    public List<DeclarationView> findDeclarations() {
        return declarations.findBySeed(F4Fixture.SEED).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public List<ConflictView> findConflicts() {
        List<F4DeclarationRecord> records = declarations.findBySeed(F4Fixture.SEED);
        Map<Declaration, String> idsByDeclaration = new IdentityHashMap<>();
        List<Declaration> domainDeclarations = records.stream()
                .map(record -> {
                    Declaration declaration = record.toDomain();
                    idsByDeclaration.put(declaration, record.id());
                    return declaration;
                })
                .toList();

        return InconsistencyDetector.detect(domainDeclarations).stream()
                .map(conflict -> toView(conflict, idsByDeclaration))
                .toList();
    }

    @Override
    public boolean validateAccusation(
            String firstDeclarationId,
            String secondDeclarationId,
            String designatedLiarDeclarationId) {
        Declaration first = findDeclaration(firstDeclarationId);
        Declaration second = findDeclaration(secondDeclarationId);
        Declaration designatedLiar = findDeclaration(designatedLiarDeclarationId);

        Set<String> lyingDeclarationIds = groundTruth.findLyingDeclarationIdsBySeed(F4Fixture.SEED);
        Set<Declaration> lyingDeclarations = lyingDeclarationIds.stream()
                .map(this::findDeclaration)
                .collect(Collectors.toUnmodifiableSet());
        Accusation accusation = new Accusation(first, second, designatedLiar);
        return AccusationValidator.isCorrect(accusation, new GroundTruth(lyingDeclarations));
    }

    private Declaration findDeclaration(String id) {
        return findRecord(id).toDomain();
    }

    private F4DeclarationRecord findRecord(String id) {
        return declarations.findByIdAndSeed(id, F4Fixture.SEED)
                .orElseThrow(() -> new F4DeclarationNotFoundException(id));
    }

    private DeclarationView toView(F4DeclarationRecord record) {
        return new DeclarationView(
                record.id(),
                record.suspectId(),
                record.suspectName(),
                record.placeId(),
                record.placeName(),
                record.startTime().format(TIME_FORMAT),
                record.endTime().format(TIME_FORMAT),
                record.claimsAlone());
    }

    private ConflictView toView(Conflict conflict, Map<Declaration, String> idsByDeclaration) {
        String firstId = idsByDeclaration.get(conflict.first());
        String secondId = idsByDeclaration.get(conflict.second());
        if (firstId == null || secondId == null) {
            throw new IllegalStateException("A detected F4 conflict has no declaration identifier.");
        }

        LocalTime overlapStart = conflict.first().startTime().isAfter(conflict.second().startTime())
                ? conflict.first().startTime()
                : conflict.second().startTime();
        LocalTime overlapEnd = conflict.first().endTime().isBefore(conflict.second().endTime())
                ? conflict.first().endTime()
                : conflict.second().endTime();

        return new ConflictView(
                conflict.type(),
                firstId,
                secondId,
                overlapStart.format(TIME_FORMAT),
                overlapEnd.format(TIME_FORMAT),
                explanationFor(conflict.type()));
    }

    private String explanationFor(ConflictType type) {
        return switch (type) {
            case UBIQUITY ->
                    "Le même suspect est déclaré dans deux lieux différents pendant ce chevauchement.";
            case SOLITUDE_CONTREDITE ->
                    "Un suspect affirme être seul, mais une autre déclaration signale un autre suspect au même lieu pendant ce chevauchement.";
        };
    }
}
