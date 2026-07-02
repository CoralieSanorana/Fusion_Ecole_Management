package com.ecole.dto.Directeur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Statistiques agrégées d'une classe : sert à afficher le nuage de points
 * (absence × moyenne) classe par classe + indicateur de corrélation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseStatistique {

    private Long   classeId;
    private String classeNom;
    private String niveauLibelle;

    private Integer effectif;
    private Integer nbAlerteRouge;
    private Integer nbAlerteJaune;

    /**
     * Coefficient de corrélation de Pearson entre taux d'absence et moyenne (entre -1 et 1).
     * Une valeur négative confirme la corrélation attendue : plus d'absences → moyenne plus basse.
     * null si données insuffisantes (moins de 2 étudiants avec données).
     */
    private Double correlationAbsenceMoyenne;

    /** Tous les points (un par étudiant) à afficher dans le nuage de points de cette classe. */
    private List<EleveStatPoint> points;
}
