package fr.miage.plottwist.investigation.application.port.out;

import fr.miage.plottwist.investigation.domain.model.SubjectiveEvent;
import java.util.List;

public interface SubjectiveEventRepository {
    SubjectiveEvent save(SubjectiveEvent event);

    List<SubjectiveEvent> findBySeed(String seed);
}
