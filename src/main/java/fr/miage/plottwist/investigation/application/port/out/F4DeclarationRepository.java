package fr.miage.plottwist.investigation.application.port.out;

import fr.miage.plottwist.investigation.domain.model.Declaration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface F4DeclarationRepository {
    List<F4DeclarationRecord> findBySeed(String seed);

    Optional<F4DeclarationRecord> findByIdAndSeed(String id, String seed);

    record F4DeclarationRecord(
            String id,
            String suspectId,
            String suspectName,
            String placeId,
            String placeName,
            LocalTime startTime,
            LocalTime endTime,
            boolean claimsAlone) {
        public Declaration toDomain() {
            return new Declaration(suspectId, placeId, startTime, endTime, claimsAlone);
        }
    }
}
