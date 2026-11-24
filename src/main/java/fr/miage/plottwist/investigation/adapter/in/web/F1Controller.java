package fr.miage.plottwist.investigation.adapter.in.web;

import fr.miage.plottwist.investigation.application.port.in.F1UseCases;
import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/f1")
public class F1Controller {
    private final F1UseCases useCases;

    public F1Controller(F1UseCases useCases) {
        this.useCases = useCases;
    }

    @GetMapping("/profiles")
    public List<SuspectProfileResponse> profiles(
            @RequestParam(defaultValue = "#AF42") String seed) {
        return useCases.findProfiles(seed).stream().map(SuspectProfileResponse::from).toList();
    }

    @GetMapping("/profiles/{id}")
    public SuspectProfileResponse profile(
            @RequestParam(defaultValue = "#AF42") String seed,
            @PathVariable UUID id) {
        return SuspectProfileResponse.from(useCases.findProfile(seed, id));
    }

    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public SuspectProfileResponse createProfile(
            @RequestParam(defaultValue = "#AF42") String seed,
            @Valid @RequestBody CreateProfileRequest request) {
        return SuspectProfileResponse.from(useCases.createProfile(
                seed, request.name(), request.role(), request.biography()));
    }

    @GetMapping("/events")
    public List<SubjectiveEventResponse> events(
            @RequestParam(defaultValue = "#AF42") String seed) {
        return useCases.findEvents(seed).stream().map(SubjectiveEventResponse::from).toList();
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectiveEventResponse createEvent(
            @RequestParam(defaultValue = "#AF42") String seed,
            @Valid @RequestBody CreateEventRequest request) {
        return SubjectiveEventResponse.from(useCases.createEvent(
                seed,
                request.title(),
                request.description(),
                request.occurredAt(),
                request.subjectType(),
                request.declaredBy(),
                request.participants()));
    }

    @GetMapping("/graph")
    public F1UseCases.F1GraphSummary graph(
            @RequestParam(defaultValue = "#AF42") String seed) {
        return useCases.graphSummary(seed);
    }

    public record CreateProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 120) String role,
            @NotBlank @Size(max = 1000) String biography) {}

    public record CreateEventRequest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 2000) String description,
            Instant occurredAt,
            SubjectiveEvent.SubjectType subjectType,
            UUID declaredBy,
            @Size(min = 1, max = 4) List<UUID> participants) {}

    public record SuspectProfileResponse(
            UUID id, String seed, String name, String role, String biography) {
        static SuspectProfileResponse from(SuspectProfile profile) {
            return new SuspectProfileResponse(
                    profile.id(), profile.seed(), profile.name(), profile.role(), profile.biography());
        }
    }

    public record SubjectiveEventResponse(
            UUID id,
            String seed,
            String title,
            String description,
            Instant occurredAt,
            SubjectiveEvent.SubjectType subjectType,
            UUID declaredBy,
            List<UUID> participants) {
        static SubjectiveEventResponse from(SubjectiveEvent event) {
            return new SubjectiveEventResponse(
                    event.id(), event.seed(), event.title(), event.description(), event.occurredAt(),
                    event.subjectType(), event.declaredBy(), event.participants());
        }
    }
}
