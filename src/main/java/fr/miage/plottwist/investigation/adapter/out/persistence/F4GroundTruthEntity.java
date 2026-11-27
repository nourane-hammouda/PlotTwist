package fr.miage.plottwist.investigation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "f4_ground_truth_lying_declaration")
public class F4GroundTruthEntity {
    @Id
    private String id;

    @Column(nullable = false, length = 16)
    private String seed;

    @Column(name = "declaration_id", nullable = false, length = 32)
    private String declarationId;

    protected F4GroundTruthEntity() {}
}
