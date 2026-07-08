package com.ecole.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_reinscriptions")
public class HistoriqueReinscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inscription_id_old")
    private Long inscriptionIdOld;

    @Column(name = "inscription_id_new")
    private Long inscriptionIdNew;

    @Column(name = "etudiant_id")
    private Long etudiantId;

    @Column(name = "ancienne_annee_scolaire_id")
    private Long ancienneAnneeScolaireId;

    @Column(name = "nouvelle_annee_scolaire_id")
    private Long nouvelleAnneeScolaireId;

    @Column(name = "ancienne_classe_id")
    private Long ancienneClasseId;

    @Column(name = "nouvelle_classe_id")
    private Long nouvelleClasseId;

    @Column(name = "ancien_statut")
    private String ancienStatut;

    @Column(name = "ancien_rang_final")
    private Integer ancienRangFinal;

    @Column(name = "ancien_resultat")
    private String ancienResultat;

    @Column(name = "ancienne_moyenne_generale", precision = 5, scale = 2)
    private BigDecimal ancienneMoyenneGenerale;

    @Column(name = "absences_annee_precedente")
    private Integer absencesAnneePrecedente;

    @Column(name = "change_par")
    private Long changePar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInscriptionIdOld() { return inscriptionIdOld; }
    public void setInscriptionIdOld(Long inscriptionIdOld) { this.inscriptionIdOld = inscriptionIdOld; }

    public Long getInscriptionIdNew() { return inscriptionIdNew; }
    public void setInscriptionIdNew(Long inscriptionIdNew) { this.inscriptionIdNew = inscriptionIdNew; }

    public Long getEtudiantId() { return etudiantId; }
    public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }

    public Long getAncienneAnneeScolaireId() { return ancienneAnneeScolaireId; }
    public void setAncienneAnneeScolaireId(Long ancienneAnneeScolaireId) { this.ancienneAnneeScolaireId = ancienneAnneeScolaireId; }

    public Long getNouvelleAnneeScolaireId() { return nouvelleAnneeScolaireId; }
    public void setNouvelleAnneeScolaireId(Long nouvelleAnneeScolaireId) { this.nouvelleAnneeScolaireId = nouvelleAnneeScolaireId; }

    public Long getAncienneClasseId() { return ancienneClasseId; }
    public void setAncienneClasseId(Long ancienneClasseId) { this.ancienneClasseId = ancienneClasseId; }

    public Long getNouvelleClasseId() { return nouvelleClasseId; }
    public void setNouvelleClasseId(Long nouvelleClasseId) { this.nouvelleClasseId = nouvelleClasseId; }

    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }

    public Integer getAncienRangFinal() { return ancienRangFinal; }
    public void setAncienRangFinal(Integer ancienRangFinal) { this.ancienRangFinal = ancienRangFinal; }

    public String getAncienResultat() { return ancienResultat; }
    public void setAncienResultat(String ancienResultat) { this.ancienResultat = ancienResultat; }

    public BigDecimal getAncienneMoyenneGenerale() { return ancienneMoyenneGenerale; }
    public void setAncienneMoyenneGenerale(BigDecimal ancienneMoyenneGenerale) { this.ancienneMoyenneGenerale = ancienneMoyenneGenerale; }

    public Integer getAbsencesAnneePrecedente() { return absencesAnneePrecedente; }
    public void setAbsencesAnneePrecedente(Integer absencesAnneePrecedente) { this.absencesAnneePrecedente = absencesAnneePrecedente; }

    public Long getChangePar() { return changePar; }
    public void setChangePar(Long changePar) { this.changePar = changePar; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
