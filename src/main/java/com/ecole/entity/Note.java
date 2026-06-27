package com.ecole.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etudiant_id")
    private Long etudiantId;

    @Column(name = "affectation_id")
    private Long affectationId;

    @Column(name = "periode_id")
    private Long periodeId;

    @Column(name = "type_evaluation")
    private String typeEvaluation;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal valeur;

    @Column(precision = 5, scale = 2)
    private BigDecimal sur = new BigDecimal("20.00");

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "saisi_par")
    private Long saisiPar;

    @Column(name = "date_saisie")
    private LocalDateTime dateSaisie;

    @Column(name = "est_valide")
    private Boolean estValide = true;

    @Column(name = "ancienne_valeur", precision = 5, scale = 2)
    private BigDecimal ancienneValeur;

    @Column(name = "corrige_par")
    private Long corrigePar;

    @Column(name = "date_correction")
    private LocalDateTime dateCorrection;

    @Column(name = "motif_correction", columnDefinition = "TEXT")
    private String motifCorrection;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        dateSaisie = LocalDateTime.now();
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

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public Long getAffectationId() {
        return affectationId;
    }

    public void setAffectationId(Long affectationId) {
        this.affectationId = affectationId;
    }

    public Long getPeriodeId() {
        return periodeId;
    }

    public void setPeriodeId(Long periodeId) {
        this.periodeId = periodeId;
    }

    public String getTypeEvaluation() {
        return typeEvaluation;
    }

    public void setTypeEvaluation(String typeEvaluation) {
        this.typeEvaluation = typeEvaluation;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public BigDecimal getSur() {
        return sur;
    }

    public void setSur(BigDecimal sur) {
        this.sur = sur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Long getSaisiPar() {
        return saisiPar;
    }

    public void setSaisiPar(Long saisiPar) {
        this.saisiPar = saisiPar;
    }

    public LocalDateTime getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(LocalDateTime dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public Boolean getEstValide() {
        return estValide;
    }

    public void setEstValide(Boolean estValide) {
        this.estValide = estValide;
    }

    public BigDecimal getAncienneValeur() {
        return ancienneValeur;
    }

    public void setAncienneValeur(BigDecimal ancienneValeur) {
        this.ancienneValeur = ancienneValeur;
    }

    public Long getCorrigePar() {
        return corrigePar;
    }

    public void setCorrigePar(Long corrigePar) {
        this.corrigePar = corrigePar;
    }

    public LocalDateTime getDateCorrection() {
        return dateCorrection;
    }

    public void setDateCorrection(LocalDateTime dateCorrection) {
        this.dateCorrection = dateCorrection;
    }

    public String getMotifCorrection() {
        return motifCorrection;
    }

    public void setMotifCorrection(String motifCorrection) {
        this.motifCorrection = motifCorrection;
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
