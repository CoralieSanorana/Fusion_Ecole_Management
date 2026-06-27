package com.ecole.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "evenements")
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etablissement_id")
    private Long etablissementId;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;

    @Column(name = "est_recurrente")
    private Boolean estRecurrente = false;

    @Column(name = "type_recurrence")
    private String typeRecurrence;

    @Column(name = "jour_recurrence")
    private Integer jourRecurrence;

    @Column(name = "mois_recurrence")
    private Integer moisRecurrence;

    @Column(name = "duree_jours")
    private Integer dureeJours;

    @Column(name = "heure_debut_defaut")
    private LocalTime heureDebutDefaut;

    @Column(name = "heure_fin_defaut")
    private LocalTime heureFinDefaut;

    @Column(name = "annule_cours")
    private Boolean annuleCours = false;

    @Column(name = "concerne_toute_ecole")
    private Boolean concerneTouteEcole = true;

    @Column(name = "concerne_matiere_id")
    private Long concerneMatiereId;

    @Column(name = "cree_par")
    private Long creePar;

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

    public Long getEtablissementId() {
        return etablissementId;
    }

    public void setEtablissementId(Long etablissementId) {
        this.etablissementId = etablissementId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getEstRecurrente() {
        return estRecurrente;
    }

    public void setEstRecurrente(Boolean estRecurrente) {
        this.estRecurrente = estRecurrente;
    }

    public String getTypeRecurrence() {
        return typeRecurrence;
    }

    public void setTypeRecurrence(String typeRecurrence) {
        this.typeRecurrence = typeRecurrence;
    }

    public Integer getJourRecurrence() {
        return jourRecurrence;
    }

    public void setJourRecurrence(Integer jourRecurrence) {
        this.jourRecurrence = jourRecurrence;
    }

    public Integer getMoisRecurrence() {
        return moisRecurrence;
    }

    public void setMoisRecurrence(Integer moisRecurrence) {
        this.moisRecurrence = moisRecurrence;
    }

    public Integer getDureeJours() {
        return dureeJours;
    }

    public void setDureeJours(Integer dureeJours) {
        this.dureeJours = dureeJours;
    }

    public LocalTime getHeureDebutDefaut() {
        return heureDebutDefaut;
    }

    public void setHeureDebutDefaut(LocalTime heureDebutDefaut) {
        this.heureDebutDefaut = heureDebutDefaut;
    }

    public LocalTime getHeureFinDefaut() {
        return heureFinDefaut;
    }

    public void setHeureFinDefaut(LocalTime heureFinDefaut) {
        this.heureFinDefaut = heureFinDefaut;
    }

    public Boolean getAnnuleCours() {
        return annuleCours;
    }

    public void setAnnuleCours(Boolean annuleCours) {
        this.annuleCours = annuleCours;
    }

    public Boolean getConcerneTouteEcole() {
        return concerneTouteEcole;
    }

    public void setConcerneTouteEcole(Boolean concerneTouteEcole) {
        this.concerneTouteEcole = concerneTouteEcole;
    }

    public Long getConcerneMatiereId() {
        return concerneMatiereId;
    }

    public void setConcerneMatiereId(Long concerneMatiereId) {
        this.concerneMatiereId = concerneMatiereId;
    }

    public Long getCreePar() {
        return creePar;
    }

    public void setCreePar(Long creePar) {
        this.creePar = creePar;
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
