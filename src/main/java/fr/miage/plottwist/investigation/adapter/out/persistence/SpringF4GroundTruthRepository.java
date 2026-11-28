package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.F4GroundTruthRepository;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class SpringF4GroundTruthRepository implements F4GroundTruthRepository {
    private final F4GroundTruthJpaRepository jpa;

    public SpringF4GroundTruthRepository(F4GroundTruthJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Set<String> findLyingDeclarationIdsBySeed(String seed) {
        return jpa.findDeclarationIdsBySeed(seed);
    }
}
