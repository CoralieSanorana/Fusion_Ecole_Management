package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "ecolage_mensuel")
public class EcolageMensuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    @ManyToOne
    @JoinColumn(name = "annee_scolaire_id")
    private AnneeScolaire anneeScolaire;

    private BigDecimal montant;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
