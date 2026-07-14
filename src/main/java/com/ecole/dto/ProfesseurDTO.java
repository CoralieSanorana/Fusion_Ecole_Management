package com.ecole.dto;

import lombok.Data;

@Data
public class ProfesseurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String matricule;
    private String specialite;
    private String telephone;
    private String adresse;
    private String typeContrat;
}
