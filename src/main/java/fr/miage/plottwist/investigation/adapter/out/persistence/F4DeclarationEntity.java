package fr.miage.plottwist.investigation.adapter.out.persistence;

import fr.miage.plottwist.investigation.application.port.out.F4DeclarationRepository.F4DeclarationRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "f4_declaration")
public class F4DeclarationEntity {
    @Id
    private String id;

    @Column(nullable = false, length = 16)
    private String seed;

    @Column(name = "suspect_id", nullable = false, length = 32)
    private String suspectId;

    @Column(name = "suspect_name", nullable = false, length = 120)
    private String suspectName;

    @Column(name = "place_id", nullable = false, length = 32)
    private String placeId;

    @Column(name = "place_name", nullable = false, length = 120)
    private String placeName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "claims_alone", nullable = false)
    private boolean claimsAlone;

    protected F4DeclarationEntity() {}

    public F4DeclarationRecord toRecord() {
        return new F4DeclarationRecord(
                id,
                suspectId,
                suspectName,
                placeId,
                placeName,
                startTime,
                endTime,
                claimsAlone);
    }
}
