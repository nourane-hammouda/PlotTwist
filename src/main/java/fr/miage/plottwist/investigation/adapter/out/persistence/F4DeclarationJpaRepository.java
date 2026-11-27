package fr.miage.plottwist.investigation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface F4DeclarationJpaRepository extends JpaRepository<F4DeclarationEntity, String> {
    @Query("select distinct declaration.seed from F4DeclarationEntity declaration order by declaration.seed")
    List<String> findAvailableSeeds();

    List<F4DeclarationEntity> findBySeedOrderByIdAsc(String seed);

    Optional<F4DeclarationEntity> findByIdAndSeed(String id, String seed);
}
