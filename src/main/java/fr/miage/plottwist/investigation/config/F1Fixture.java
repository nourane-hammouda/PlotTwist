package fr.miage.plottwist.investigation.config;

import fr.miage.plottwist.investigation.application.port.in.F1UseCases;
import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class F1Fixture {
    public static final String SEED = "#AF42";

    @Bean
    CommandLineRunner loadF1Fixture(F1UseCases useCases) {
        return args -> {
            if (!useCases.findProfiles(SEED).isEmpty()) {
                return;
            }

            SuspectProfile lena = useCases.createProfile(SEED,
                    "Lena Voss", "Victime", "Historienne, invitée à la soirée.");
            SuspectProfile marc = useCases.createProfile(SEED,
                    "Marc Diallo", "Gardien", "Gardien de nuit du domaine.");
            SuspectProfile ines = useCases.createProfile(SEED,
                    "Inès Laurent", "Chroniqueuse", "Journaliste locale.");
            SuspectProfile theo = useCases.createProfile(SEED,
                    "Théo Martin", "Technicien", "Technicien réseau.");

            createEvent(useCases, lena.id(), List.of(lena.id(), marc.id()),
                    "Lena affirme être restée seule", "Lena dit n'avoir rencontré personne.", "ALIBI");
            createEvent(useCases, marc.id(), List.of(marc.id(), ines.id()),
                    "Marc voit Inès dans la galerie", "Marc déclare avoir croisé Inès à 21 h.", "ACTIVITY");
            createEvent(useCases, ines.id(), List.of(ines.id(), theo.id(), lena.id()),
                    "Inès évoque une conversation", "Inès affirme avoir parlé avec Lena et Théo.", "RELATION");
            createEvent(useCases, theo.id(), List.of(theo.id()),
                    "Théo rappelle son passage réseau", "Théo dit avoir vérifié le réseau dans sa loge.", "LOCATION");
        };
    }

    private void createEvent(
            F1UseCases useCases,
            UUID declaredBy,
            List<UUID> participants,
            String title,
            String description,
            String subjectType) {
        useCases.createEvent(
                SEED,
                title,
                description,
                Instant.parse("2026-11-24T20:30:00Z"),
                SubjectiveEvent.SubjectType.valueOf(subjectType),
                declaredBy,
                participants);
    }
}
