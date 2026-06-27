package com.ecole.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "moyennes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"etudiant_id", "inscription_id", "periode_id", "matiere_id"})
})
public class Moyenne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "etudiant_id")
    private Integer etudiantId;

    @Column(name = "inscription_id")
    private Integer inscriptionId;

    @Column(name = "periode_id")
    private Integer periodeId;

    @Column(name = "matiere_id")
    private Integer matiereId;

    @Column(precision = 5, scale = 2)
    private BigDecimal valeur;

    private Integer rang;

    @Column(name = "effectif_classe")
    private Integer effectifClasse;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Integer etudiantId) {
        this.etudiantId = etudiantId;
    }

    public Integer getInscriptionId() {
        return inscriptionId;
    }

    public void setInscriptionId(Integer inscriptionId) {
        this.inscriptionId = inscriptionId;
    }

    public Integer getPeriodeId() {
        return periodeId;
    }

    public void setPeriodeId(Integer periodeId) {
        this.periodeId = periodeId;
    }

    public Integer getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Integer matiereId) {
        this.matiereId = matiereId;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public Integer getRang() {
        return rang;
    }

    public void setRang(Integer rang) {
        this.rang = rang;
    }

    public Integer getEffectifClasse() {
        return effectifClasse;
    }

    public void setEffectifClasse(Integer effectifClasse) {
        this.effectifClasse = effectifClasse;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
