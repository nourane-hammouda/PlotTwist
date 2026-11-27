package fr.miage.plottwist.investigation.domain.algo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import fr.miage.plottwist.investigation.domain.model.Conflict;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import fr.miage.plottwist.investigation.domain.model.GeneratedF4Scenario;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class F4InvestigationGeneratorTest {
    private final F4InvestigationGenerator generator = new F4InvestigationGenerator();

    @Test
    void createsRepeatableCasesWithBothDiscoverableConflictTypes() {
        for (int index = 0; index < 25; index++) {
            String seed = F4InvestigationGenerator.GENERATED_SEED_PREFIX + UUID.randomUUID();

            GeneratedF4Scenario scenario = generator.generate(seed);
            List<Conflict> conflicts = InconsistencyDetector.detect(
                    scenario.declarations().stream()
                            .map(GeneratedF4Scenario.GeneratedDeclaration::statement)
                            .toList());

            assertThat(scenario).isEqualTo(generator.generate(seed));
            assertThat(scenario.declarations()).hasSize(6);
            assertThat(conflicts)
                    .extracting(Conflict::type)
                    .containsExactlyInAnyOrder(
                            ConflictType.UBIQUITY,
                            ConflictType.SOLITUDE_CONTREDITE);
            assertThat(scenario.lyingDeclarationIds()).containsExactlyInAnyOrder("d1", "d3");
        }
    }

    @Test
    void rejectsSeedsThatAreNotGeneratedInvestigationIds() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> generator.generate("#AUTO-not-a-uuid"));
    }
}
