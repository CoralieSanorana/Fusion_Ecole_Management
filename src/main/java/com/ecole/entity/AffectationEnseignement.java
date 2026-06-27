package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "affectations_enseignement")
public class AffectationEnseignement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private ProfilsProfesseurs professeur;

    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id")
    private AnneeScolaire anneeScolaire;

    private BigDecimal heuresHebdo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ProfilsProfesseurs getProfesseur() {
        return professeur;
    }
    public void setProfesseur(ProfilsProfesseurs professeur) {
        this.professeur = professeur;
    }
    public Matiere getMatiere() {
        return matiere;
    }
    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }
    public Classe getClasse() {
        return classe;
    }
    public void setClasse(Classe classe) {
        this.classe = classe;
    }
    public AnneeScolaire getAnneeScolaire() {
        return anneeScolaire;
    }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }
    public BigDecimal getHeuresHebdo() {
        return heuresHebdo;
    }
    public void setHeuresHebdo(BigDecimal heuresHebdo) {
        this.heuresHebdo = heuresHebdo;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getClasseId() {
        return classe.getId();
    }

    public void setClasseId(Long classeId) {
        this.classe.setId(classeId);
    }

    public Long getMatiereId() {
        return matiere.getId();
    }

    public void setMatiereId(Long matiereId) {
        this.matiere.setId(matiereId);
    }
    
}
