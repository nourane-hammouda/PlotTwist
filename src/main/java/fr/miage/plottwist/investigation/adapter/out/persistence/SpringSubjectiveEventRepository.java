package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.SubjectiveEventRepository;
import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SpringSubjectiveEventRepository implements SubjectiveEventRepository {
    private final SubjectiveEventJpaRepository jpa;

    public SpringSubjectiveEventRepository(SubjectiveEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SubjectiveEvent save(SubjectiveEvent event) {
        jpa.save(new SubjectiveEventEntity(
                event.id(), event.seed(), event.title(), event.description(), event.occurredAt(),
                event.subjectType(), event.declaredBy(), event.participants()));
        return event;
    }

    @Override
    public List<SubjectiveEvent> findBySeed(String seed) {
        return jpa.findBySeed(seed).stream().map(entity -> new SubjectiveEvent(
                entity.getId(),
                entity.getSeed(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOccurredAt(),
                entity.getSubjectType(),
                entity.getDeclaredBy(),
                entity.getParticipants())).toList();
    }
}
