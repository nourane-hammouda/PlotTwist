package fr.miage.plottwist.investigation.adapter.out.persistence;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface F4GroundTruthJpaRepository extends JpaRepository<F4GroundTruthEntity, String> {
    @Query("""
            select truth.declarationId
            from F4GroundTruthEntity truth
            where truth.seed = :seed
            order by truth.declarationId
            """)
    Set<String> findDeclarationIdsBySeed(@Param("seed") String seed);
}
