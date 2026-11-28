package fr.miage.plottwist.investigation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectiveEventJpaRepository extends JpaRepository<SubjectiveEventEntity, UUID> {
    List<SubjectiveEventEntity> findBySeed(String seed);
}
