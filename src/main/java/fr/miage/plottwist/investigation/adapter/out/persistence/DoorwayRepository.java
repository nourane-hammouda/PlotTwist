package fr.miage.plottwist.investigation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les passages entre pièces.
 */
public interface DoorwayRepository extends JpaRepository<DoorwayEntity, DoorwayEntity.DoorwayId> {
}
