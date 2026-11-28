package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository;
import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository.F4DeclarationRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SpringF4DeclarationRepository implements F4DeclarationRepository {
    private final F4DeclarationJpaRepository jpa;

    public SpringF4DeclarationRepository(F4DeclarationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<F4DeclarationRecord> findBySeed(String seed) {
        return jpa.findBySeedOrderByIdAsc(seed).stream()
                .map(F4DeclarationEntity::toRecord)
                .toList();
    }

    @Override
    public Optional<F4DeclarationRecord> findByIdAndSeed(String id, String seed) {
        return jpa.findByIdAndSeed(id, seed).map(F4DeclarationEntity::toRecord);
    }
}
