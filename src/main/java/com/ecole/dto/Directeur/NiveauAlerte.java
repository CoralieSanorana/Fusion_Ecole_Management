package com.ecole.dto.Directeur;

/**
 * Niveau d'alerte décrochage attribué à un étudiant.
 *
 * ROUGE : baisse de moyenne ≥ seuil  ET  taux d'absence ≥ seuil
 * JAUNE : baisse de moyenne ≥ seuil  OU   taux d'absence ≥ seuil
 * AUCUNE : aucun des deux critères n'est atteint
 */
public enum NiveauAlerte {
    ROUGE,
    JAUNE,
    AUCUNE
}
