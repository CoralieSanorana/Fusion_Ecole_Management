package com.ecole.dto.Directeur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Résultat aplati d'une ligne du tableau de filtrage.
 * Combine ProfilEtudiant + Inscription + Classe + Niveau en un seul objet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantFilterResult {

    // ProfilEtudiant
    private Long      etudiantId;
    private String    matricule;
    private String    nom;
    private String    prenom;
    private LocalDate dateNaissance;
    private String    lieuNaissance;
    private String    sexe;
    private String    region;
    private String    nationalite;
    private String    telephone;
    private String    email;          // depuis User lié
    private Boolean   isArchived;

    // Inscription
    private Long      inscriptionId;
    private String    statutInscription;
    private String    typeInscription;
    private LocalDate dateInscription;

    // Classe + Niveau
    private String    classeNom;
    private String    niveauLibelle;

    // Année scolaire
    private String    anneeLibelle;
}
