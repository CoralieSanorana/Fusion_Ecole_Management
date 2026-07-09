package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "echeances")
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "echeancier_id")
    private Echeancier echeancier;

    @Column(name = "numero_tranche")
    private Integer numeroTranche;

    @Column(name = "montant_attendu")
    private BigDecimal montantAttendu;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Column(name = "est_soldee")
    private Boolean estSoldee;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}