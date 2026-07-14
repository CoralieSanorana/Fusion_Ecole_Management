package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "classes")
public class Classe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id")
    private AnneeScolaire anneeScolaire;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    private String nom;
    private Integer capaciteMax;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;    
    }
    public Niveau getNiveau() {
        return niveau;
    }
    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }
    public AnneeScolaire getAnneeScolaire() {
        return anneeScolaire;
    }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public Integer getCapaciteMax() {
        return capaciteMax;
    }
    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getNiveauId() {
        return niveau != null ? niveau.getId() : null;
    }
    public Long getAnneeScolaireId() {
        return anneeScolaire != null ? anneeScolaire.getId() : null;
    }
    public Salle getSalle() {
        return salle;
    }
    public void setSalle(Salle salle) {
        this.salle = salle;
    }
    public Long getSalleId() {
        return salle != null ? salle.getId() : null;
    }
    
    
}
