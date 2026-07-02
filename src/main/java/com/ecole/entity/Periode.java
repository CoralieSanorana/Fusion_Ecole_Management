package com.ecole.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodes")
public class Periode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annee_scolaire_id")
    private Long anneeScolaireId;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(length = 20)
    private String type = "trimestre";

    @Column(nullable = false)
    private Integer ordre;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_publication_notes")
    private LocalDate datePublicationNotes;

    @Column(name = "est_cloturee")
    private Boolean estCloturee = false;

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnneeScolaireId() {
        return anneeScolaireId;
    }

    public void setAnneeScolaireId(Long anneeScolaireId) {
        this.anneeScolaireId = anneeScolaireId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDate getDatePublicationNotes() {
        return datePublicationNotes;
    }

    public void setDatePublicationNotes(LocalDate datePublicationNotes) {
        this.datePublicationNotes = datePublicationNotes;
    }

    public Boolean getEstCloturee() {
        return estCloturee;
    }

    public void setEstCloturee(Boolean estCloturee) {
        this.estCloturee = estCloturee;
    }
}