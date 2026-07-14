package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "niveaux")
public class Niveau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    private String libelle;
    private Integer ordre;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLibelle() {
        return libelle;
    }
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    public Integer getOrdre() {
        return ordre;
    }
    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Etablissement getEtablissement() {
        return etablissement;
    }
    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }
    public Long getEtablissementId() {
        return etablissement != null ? etablissement.getId() : null;
    }
    
}
