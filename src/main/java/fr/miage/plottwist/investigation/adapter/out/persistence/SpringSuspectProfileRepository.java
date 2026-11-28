package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.SuspectProfileRepository;
import fr.miage.plottwist.investigation.domain.model.SuspectProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SpringSuspectProfileRepository implements SuspectProfileRepository {
    private final SuspectProfileJpaRepository jpa;

    public SpringSuspectProfileRepository(SuspectProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SuspectProfile save(SuspectProfile profile) {
        SuspectProfileEntity entity = new SuspectProfileEntity(
                profile.id(), profile.seed(), profile.name(), profile.role(), profile.biography());
        jpa.save(entity);
        return profile;
    }

    @Override
    public Optional<SuspectProfile> findByIdAndSeed(UUID id, String seed) {
        return jpa.findByIdAndSeed(id, seed).map(this::toDomain);
    }

    @Override
    public List<SuspectProfile> findBySeed(String seed) {
        return jpa.findBySeed(seed).stream().map(this::toDomain).toList();
    }

    private SuspectProfile toDomain(SuspectProfileEntity entity) {
        return new SuspectProfile(
                entity.getId(), entity.getSeed(), entity.getName(), entity.getRole(), entity.getBiography());
    }
}
