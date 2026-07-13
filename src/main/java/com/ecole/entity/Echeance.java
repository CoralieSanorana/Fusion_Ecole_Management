package com.ecole.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "echeances")
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nouvelle relation directe vers inscription
    @ManyToOne
    @JoinColumn(name = "inscription_id")
    @JsonIgnore
    private Inscription inscription;

    // Mois concerné (1=Janvier ... 12=Décembre)
    private Integer mois;

    // Année concernée (2025, 2026)
    private Integer annee;

    // Snapshot du montant au moment de la génération
    @Column(name = "montant_ecolage")
    private BigDecimal montantEcolage;

    @Column(name = "est_soldee")
    private Boolean estSoldee = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Anciens champs gardés pour compatibilité — non utilisés dans le nouveau flux
    @ManyToOne
    @JoinColumn(name = "echeancier_id")
    @JsonIgnore
    private Echeancier echeancier;

    @Column(name = "numero_tranche")
    private Integer numeroTranche;

    @Column(name = "montant_attendu")
    private BigDecimal montantAttendu;

    @Column(name = "date_limite")
    private java.time.LocalDate dateLimite;

    // Nom du mois pour affichage
    public String getNomMois() {
        if (mois == null) return "";
        return switch (mois) {
            case 1  -> "Janvier";
            case 2  -> "Février";
            case 3  -> "Mars";
            case 4  -> "Avril";
            case 5  -> "Mai";
            case 6  -> "Juin";
            case 7  -> "Juillet";
            case 8  -> "Août";
            case 9  -> "Septembre";
            case 10 -> "Octobre";
            case 11 -> "Novembre";
            case 12 -> "Décembre";
            default -> "";
        };
    }

    // Label complet pour affichage ex : "Septembre 2025"
    public String getLabel() {
        return getNomMois() + " " + annee;
    }
}