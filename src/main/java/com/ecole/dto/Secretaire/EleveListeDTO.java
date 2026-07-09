package com.ecole.dto.Secretaire;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class EleveListeDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String nomClasse;
    private String niveau;
    private String photoUrl;
    private Map<Integer, Boolean> paiementsMois;

    // Ajouter cette méthode pour les initiales
    public String getInitiales() {
        if (prenom == null && nom == null)
            return "?";
        String prenomInitial = prenom != null && !prenom.isEmpty() ? prenom.substring(0, 1) : "";
        String nomInitial = nom != null && !nom.isEmpty() ? nom.substring(0, 1) : "";
        return (prenomInitial + nomInitial).toUpperCase();
    }
}