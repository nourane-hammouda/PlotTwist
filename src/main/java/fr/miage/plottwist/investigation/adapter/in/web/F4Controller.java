package fr.miage.plottwist.investigation.adapter.in.web;

import fr.miage.plottwist.investigation.application.port.in.F4UseCases;
import fr.miage.plottwist.investigation.config.F4Fixture;
import fr.miage.plottwist.investigation.domain.model.ConflictType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/f4")
public class F4Controller {
    private final F4UseCases useCases;

    public F4Controller(F4UseCases useCases) {
        this.useCases = useCases;
    }

    @GetMapping("/seeds")
    public List<String> getAvailableSeeds() {
        return useCases.findAvailableSeeds();
    }

    @GetMapping("/case")
    public F4UseCases.InvestigationView getInvestigation(
            @RequestParam(defaultValue = F4Fixture.SEED) String seed) {
        return useCases.findInvestigation(seed);
    }

    @PostMapping("/generate")
    public F4UseCases.InvestigationView generateInvestigation() {
        return useCases.generateInvestigation();
    }

    @GetMapping("/declarations")
    public List<F4UseCases.DeclarationView> getDeclarations(
            @RequestParam(defaultValue = F4Fixture.SEED) String seed) {
        return useCases.findDeclarations(seed);
    }

    @GetMapping("/conflicts")
    public List<F4UseCases.ConflictView> getConflicts(
            @RequestParam(defaultValue = F4Fixture.SEED) String seed) {
        return useCases.findConflicts(seed);
    }

    @PostMapping("/accusations")
    public AccusationResponse postAccusation(@Valid @RequestBody AccusationRequest request) {
        boolean correct = useCases.validateAccusation(
                request.seed(),
                request.firstDeclarationId(),
                request.secondDeclarationId(),
                request.designatedLiarDeclarationId());
        return new AccusationResponse(correct);
    }

    @PostMapping("/discoveries")
    public F4UseCases.DiscoveryView postDiscovery(@Valid @RequestBody DiscoveryRequest request) {
        return useCases.validateDiscovery(
                request.seed(),
                request.firstDeclarationId(),
                request.secondDeclarationId(),
                request.conflictType(),
                request.designatedLiarDeclarationId());
    }

    public record DiscoveryRequest(
            String seed,
            @NotBlank String firstDeclarationId,
            @NotBlank String secondDeclarationId,
            @NotNull ConflictType conflictType,
            @NotBlank String designatedLiarDeclarationId) {
        public DiscoveryRequest {
            if (seed == null || seed.isBlank()) {
                seed = F4Fixture.SEED;
            }
        }
    }

    public record AccusationRequest(
            String seed,
            @NotBlank String firstDeclarationId,
            @NotBlank String secondDeclarationId,
            @NotBlank String designatedLiarDeclarationId) {
        public AccusationRequest {
            if (seed == null || seed.isBlank()) {
                seed = F4Fixture.SEED;
            }
        }
    }

    public record AccusationResponse(boolean correct) {}
}
