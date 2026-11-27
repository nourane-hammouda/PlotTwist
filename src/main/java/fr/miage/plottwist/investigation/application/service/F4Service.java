package fr.miage.plottwist.investigation.application.service;

import fr.miage.plottwist.investigation.application.port.in.F4UseCases;
import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository;
import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository.F4DeclarationRecord;
import fr.miage.plottwist.investigation.application.port.out.F4GroundTruthRepository;
import fr.miage.plottwist.investigation.domain.algo.AccusationValidator;
import fr.miage.plottwist.investigation.domain.algo.F4InvestigationGenerator;
import fr.miage.plottwist.investigation.domain.algo.InconsistencyDetector;
import fr.miage.plottwist.investigation.domain.exception.F4DeclarationNotFoundException;
import fr.miage.plottwist.investigation.domain.model.Accusation;
import fr.miage.plottwist.investigation.domain.model.Conflict;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import fr.miage.plottwist.investigation.domain.model.Declaration;
import fr.miage.plottwist.investigation.domain.model.GeneratedF4Scenario;
import fr.miage.plottwist.investigation.domain.model.GeneratedF4Scenario.GeneratedDeclaration;
import fr.miage.plottwist.investigation.domain.model.GroundTruth;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class F4Service implements F4UseCases {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String SECOND_SEED = "#B731";

    private final F4DeclarationRepository declarations;
    private final F4GroundTruthRepository groundTruth;
    private final F4InvestigationGenerator generator = new F4InvestigationGenerator();

    public F4Service(
            F4DeclarationRepository declarations,
            F4GroundTruthRepository groundTruth) {
        this.declarations = declarations;
        this.groundTruth = groundTruth;
    }

    @Override
    public List<String> findAvailableSeeds() {
        return declarations.findAvailableSeeds();
    }

    @Override
    public InvestigationView findInvestigation(String seed) {
        if (F4InvestigationGenerator.isGeneratedSeed(seed)) {
            return toInvestigationView(generator.generate(seed));
        }
        if (declarations.findAvailableSeeds().contains(seed)) {
            return new InvestigationView(
                    seed,
                    seed.equals(SECOND_SEED) ? "Les voix de la régie" : "La dernière représentation",
                    "Six témoignages sont consignés. Reliez les versions incompatibles et identifiez les mensonges.",
                    findConflicts(seed).size());
        }
        throw new IllegalArgumentException("Cette enquête n’existe pas.");
    }

    @Override
    public InvestigationView generateInvestigation() {
        String seed = F4InvestigationGenerator.GENERATED_SEED_PREFIX + UUID.randomUUID();
        return toInvestigationView(generator.generate(seed));
    }

    @Override
    public List<DeclarationView> findDeclarations(String seed) {
        return recordsForSeed(seed).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public List<ConflictView> findConflicts(String seed) {
        List<F4DeclarationRecord> records = recordsForSeed(seed);
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
    public DiscoveryView validateDiscovery(
            String seed,
            String firstDeclarationId,
            String secondDeclarationId,
            ConflictType conflictType,
            String designatedLiarDeclarationId) {
        List<ConflictView> conflicts = findConflicts(seed);
        ConflictView conflict = conflicts.stream()
                .filter(candidate -> samePair(
                        candidate,
                        firstDeclarationId,
                        secondDeclarationId))
                .findFirst()
                .orElse(null);
        boolean correct = conflict != null
                && conflict.type() == conflictType
                && validateAccusation(
                        seed,
                        firstDeclarationId,
                        secondDeclarationId,
                        designatedLiarDeclarationId);
        if (!correct) {
            return new DiscoveryView(
                    false,
                    null,
                    0,
                    conflicts.size(),
                    "Cette déduction ne correspond pas encore aux témoignages. Réexaminez les fiches.",
                    null);
        }

        int discoveryOrder = conflicts.indexOf(conflict) + 1;
        F4DeclarationRecord first = findRecord(seed, conflict.firstDeclarationId());
        F4DeclarationRecord second = findRecord(seed, conflict.secondDeclarationId());
        String discoveryId = conflict.type() + ":" + sortedPair(
                conflict.firstDeclarationId(),
                conflict.secondDeclarationId());
        return new DiscoveryView(
                true,
                discoveryId,
                discoveryOrder,
                conflicts.size(),
                discoveryText(
                        conflict,
                        first,
                        second,
                        designatedLiarDeclarationId,
                        discoveryOrder),
                conflict);
    }

    @Override
    public boolean validateAccusation(
            String seed,
            String firstDeclarationId,
            String secondDeclarationId,
            String designatedLiarDeclarationId) {
        Declaration first = findDeclaration(seed, firstDeclarationId);
        Declaration second = findDeclaration(seed, secondDeclarationId);
        Declaration designatedLiar = findDeclaration(seed, designatedLiarDeclarationId);

        Set<String> lyingDeclarationIds = F4InvestigationGenerator.isGeneratedSeed(seed)
                ? generator.generate(seed).lyingDeclarationIds()
                : groundTruth.findLyingDeclarationIdsBySeed(seed);
        Set<Declaration> lyingDeclarations = lyingDeclarationIds.stream()
                .map(id -> findDeclaration(seed, id))
                .collect(Collectors.toUnmodifiableSet());
        Accusation accusation = new Accusation(first, second, designatedLiar);
        return AccusationValidator.isCorrect(accusation, new GroundTruth(lyingDeclarations));
    }

    private List<F4DeclarationRecord> recordsForSeed(String seed) {
        if (!F4InvestigationGenerator.isGeneratedSeed(seed)) {
            return declarations.findBySeed(seed);
        }
        return generator.generate(seed).declarations().stream()
                .map(this::toRecord)
                .toList();
    }

    private F4DeclarationRecord toRecord(GeneratedDeclaration generated) {
        Declaration statement = generated.statement();
        return new F4DeclarationRecord(
                generated.id(),
                statement.suspectId(),
                generated.suspectName(),
                statement.placeId(),
                generated.placeName(),
                statement.startTime(),
                statement.endTime(),
                statement.claimsAlone());
    }

    private InvestigationView toInvestigationView(GeneratedF4Scenario scenario) {
        List<Declaration> statements = scenario.declarations().stream()
                .map(GeneratedDeclaration::statement)
                .toList();
        int discoveryCount = InconsistencyDetector.detect(statements).size();
        return new InvestigationView(
                scenario.seed(),
                scenario.title(),
                scenario.introduction(),
                discoveryCount);
    }

    private boolean samePair(ConflictView conflict, String firstId, String secondId) {
        return (conflict.firstDeclarationId().equals(firstId)
                && conflict.secondDeclarationId().equals(secondId)
                || conflict.firstDeclarationId().equals(secondId)
                && conflict.secondDeclarationId().equals(firstId));
    }

    private String sortedPair(String firstId, String secondId) {
        List<String> ids = new ArrayList<>(List.of(firstId, secondId));
        ids.sort(String::compareTo);
        return String.join("|", ids);
    }

    private String discoveryText(
            ConflictView conflict,
            F4DeclarationRecord first,
            F4DeclarationRecord second,
            String designatedLiarDeclarationId,
            int discoveryOrder) {
        F4DeclarationRecord liar = designatedLiarDeclarationId.equals(first.id()) ? first : second;
        return switch (conflict.type()) {
            case UBIQUITY -> "Découverte " + discoveryOrder + " : "
                    + liar.suspectName() + " est situé·e dans deux lieux pendant le chevauchement "
                    + conflict.overlapStartTime() + "–" + conflict.overlapEndTime()
                    + ". Sa déclaration est ajoutée au carnet comme mensongère.";
            case SOLITUDE_CONTREDITE -> {
                F4DeclarationRecord solitudeClaim = first.claimsAlone() ? first : second;
                F4DeclarationRecord companion = solitudeClaim.id().equals(first.id()) ? second : first;
                yield "Découverte " + discoveryOrder + " : l’affirmation de solitude de "
                        + solitudeClaim.suspectName() + " à " + solitudeClaim.placeName()
                        + " est contredite par " + companion.suspectName()
                        + ", présent·e au même endroit entre " + conflict.overlapStartTime()
                        + " et " + conflict.overlapEndTime() + ". La déclaration de "
                        + liar.suspectName() + " est ajoutée au carnet comme mensongère.";
            }
        };
    }

    private Declaration findDeclaration(String seed, String id) {
        return findRecord(seed, id).toDomain();
    }

    private F4DeclarationRecord findRecord(String seed, String id) {
        return recordsForSeed(seed).stream()
                .filter(record -> record.id().equals(id))
                .findFirst()
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
