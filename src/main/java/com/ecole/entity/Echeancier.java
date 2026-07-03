package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter @Setter
@Entity
@Table(name = "echeanciers")
public class Echeancier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inscription_id")
    private Inscription inscription;

    @ManyToOne
    @JoinColumn(name = "grille_id")
    private GrilleTarifaire grille;

    private String type;

    @Column(name = "montant_total")
    private BigDecimal montantTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "echeancier", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Echeance> echeances;
}