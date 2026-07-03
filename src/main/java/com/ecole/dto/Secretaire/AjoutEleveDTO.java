package com.ecole.dto.Secretaire;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class AjoutEleveDTO {
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String commune;
    private String adresse;
    private String nomParent;
    private String prenomParent;
    private String telephoneParent;
    private String lienParente;
    private String classeId;

    // Remplacer dans AjoutEleveDTO.java
    public Long getClasseIdAsLong() {
        try {
            return Long.parseLong(classeId);
        } catch (Exception e) {
            return null;
        }
    }
}