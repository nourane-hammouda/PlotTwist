package fr.miage.plottwist.investigation.application.port.out;

import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspectProfileRepository {
    SuspectProfile save(SuspectProfile profile);

    Optional<SuspectProfile> findByIdAndSeed(UUID id, String seed);

    List<SuspectProfile> findBySeed(String seed);
}
