package com.ecole.dto.Secretaire;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class EleveProfilDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String photoUrl;
    private LocalDate dateNaissance;
    private String commune;
    private String adresse;
    private String nomClasse;
    private LocalDate dateInscription;
    private String nomParent;
    private String prenomParent;
    private String telephoneParent;
    private String lienParente;
    private List<PaiementHistoriqueDTO> historiquesPaiements;

    // Ajouter cette méthode pour les initiales
    public String getInitiales() {
        if (prenom == null && nom == null)
            return "?";
        String prenomInitial = prenom != null && !prenom.isEmpty() ? prenom.substring(0, 1) : "";
        String nomInitial = nom != null && !nom.isEmpty() ? nom.substring(0, 1) : "";
        return (prenomInitial + nomInitial).toUpperCase();
    }
}