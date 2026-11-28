package fr.miage.plottwist.investigation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspectProfileJpaRepository extends JpaRepository<SuspectProfileEntity, UUID> {
    Optional<SuspectProfileEntity> findByIdAndSeed(UUID id, String seed);

    List<SuspectProfileEntity> findBySeed(String seed);
}
