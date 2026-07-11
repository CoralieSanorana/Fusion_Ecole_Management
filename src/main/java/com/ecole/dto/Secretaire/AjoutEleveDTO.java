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
    private String sexe;
    private String lieuNaissance;
    private String nationalite;
    private String commune;
    private String adresse;
    private String telephone;
    private String nomParent;
    private String prenomParent;
    private String telephoneParent;
    private String lienParente;
    private String classeId;

    public Long getClasseIdAsLong() {
        try {
            return Long.parseLong(classeId);
        } catch (Exception e) {
            return null;
        }
    }
}
