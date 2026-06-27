package com.ecole.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "coefficients", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"matiere_id", "niveau_id"})
})
public class Coefficient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matiere_id")
    private Long matiereId;

    @Column(name = "niveau_id")
    private Long niveauId;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal valeur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Long matiereId) {
        this.matiereId = matiereId;
    }

    public Long getNiveauId() {
        return niveauId;
    }

    public void setNiveauId(Long niveauId) {
        this.niveauId = niveauId;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }
}