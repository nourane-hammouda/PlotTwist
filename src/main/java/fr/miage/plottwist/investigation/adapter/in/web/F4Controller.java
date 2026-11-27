package fr.miage.plottwist.investigation.adapter.in.web;

import fr.miage.plottwist.investigation.application.port.in.F4UseCases;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/f4")
public class F4Controller {
    private final F4UseCases useCases;

    public F4Controller(F4UseCases useCases) {
        this.useCases = useCases;
    }

    @GetMapping("/declarations")
    public List<F4UseCases.DeclarationView> getDeclarations() {
        return useCases.findDeclarations();
    }

    @GetMapping("/conflicts")
    public List<F4UseCases.ConflictView> getConflicts() {
        return useCases.findConflicts();
    }

    @PostMapping("/accusations")
    public AccusationResponse postAccusation(@Valid @RequestBody AccusationRequest request) {
        boolean correct = useCases.validateAccusation(
                request.firstDeclarationId(),
                request.secondDeclarationId(),
                request.designatedLiarDeclarationId());
        return new AccusationResponse(correct);
    }

    public record AccusationRequest(
            @NotBlank String firstDeclarationId,
            @NotBlank String secondDeclarationId,
            @NotBlank String designatedLiarDeclarationId) {}

    public record AccusationResponse(boolean correct) {}
}
