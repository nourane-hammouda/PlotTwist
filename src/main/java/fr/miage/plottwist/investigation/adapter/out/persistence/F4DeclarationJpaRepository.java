package fr.miage.plottwist.investigation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface F4DeclarationJpaRepository extends JpaRepository<F4DeclarationEntity, String> {
    List<F4DeclarationEntity> findBySeedOrderByIdAsc(String seed);

    Optional<F4DeclarationEntity> findByIdAndSeed(String id, String seed);
}
