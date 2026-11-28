package fr.miage.plottwist.investigation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les pièces.
 */
public interface PlaceRepository extends JpaRepository<PlaceEntity, String> {
}
