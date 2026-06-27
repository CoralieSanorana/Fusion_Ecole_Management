package com.ecole.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "absences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"seance_id", "etudiant_id"})
})
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seance_id")
    private Long seanceId;

    @Column(name = "etudiant_id")
    private Long etudiantId;

    private String type = "non_justifiee";

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "justificatif_url")
    private String justificatifUrl;

    @Column(name = "saisi_par")
    private Long saisiPar;

    @Column(name = "valide_par")
    private Long validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeanceId() {
        return seanceId;
    }

    public void setSeanceId(Long seanceId) {
        this.seanceId = seanceId;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getJustificatifUrl() {
        return justificatifUrl;
    }

    public void setJustificatifUrl(String justificatifUrl) {
        this.justificatifUrl = justificatifUrl;
    }

    public Long getSaisiPar() {
        return saisiPar;
    }

    public void setSaisiPar(Long saisiPar) {
        this.saisiPar = saisiPar;
    }

    public Long getValidePar() {
        return validePar;
    }

    public void setValidePar(Long validePar) {
        this.validePar = validePar;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
