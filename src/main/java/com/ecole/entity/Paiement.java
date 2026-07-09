package com.ecole.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "echeance_id")
    private Echeance echeance;

    @ManyToOne
    @JoinColumn(name = "inscription_id")
    @JsonIgnore
    private Inscription inscription;

    private BigDecimal montant;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "mode_paiement")
    private String modePaiement;

    @Column(name = "reference_transaction")
    private String referenceTransaction;

    // Référence groupe — relie plusieurs lignes du même paiement
    @Column(name = "notes")
    private String notes;

    // On réutilise reference_transaction comme référence groupe
    @ManyToOne
    @JoinColumn(name = "saisi_par")
    @JsonIgnore
    private User saisiPar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}