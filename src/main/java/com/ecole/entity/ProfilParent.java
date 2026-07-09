package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "profils_parents")
public class ProfilParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String profession;

    @Column(name = "lien_parente")
    private String lienParente;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}