package fr.miage.plottwist.investigation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suspect_profile")
public class SuspectProfileEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 16)
    private String seed;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String role;

    @Column(nullable = false, length = 1000)
    private String biography;

    protected SuspectProfileEntity() {}

    public SuspectProfileEntity(UUID id, String seed, String name, String role, String biography) {
        this.id = id;
        this.seed = seed;
        this.name = name;
        this.role = role;
        this.biography = biography;
    }

    public UUID getId() { return id; }
    public String getSeed() { return seed; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getBiography() { return biography; }
}
