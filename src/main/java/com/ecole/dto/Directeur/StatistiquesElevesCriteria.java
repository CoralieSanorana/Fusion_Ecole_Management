package com.ecole.dto.Directeur;

import lombok.Data;

/**
 * Critères de filtre pour le module "Statistiques Élèves" (détection décrochage).
 *
 * L'analyse croise les 3 dernières périodes consécutives (Periode.ordre)
 * de l'année scolaire choisie avec le taux d'absence de chaque étudiant.
 */
@Data
public class StatistiquesElevesCriteria {

    // ── Périmètre ────────────────────────────────────────────────
    /** Année scolaire analysée. Si null → année active (estActive = true). */
    private Long anneeScolaireId;

    /** Restreindre à une classe précise. Si null → toutes les classes. */
    private Long classeId;

    /**
     * Dernière période (ordre) à inclure dans l'analyse.
     * Les 2 périodes précédentes (ordre-1, ordre-2) sont automatiquement incluses.
     * Si null → la dernière période disponible (ordre max) de l'année scolaire est utilisée.
     */
    private Long periodeFinId;

    // ── Seuils (paramétrables, avec valeurs par défaut) ────────────
    /** Baisse de moyenne (en points/20) déclenchant une alerte. Défaut : 2.0 */
    private Double seuilBaisseMoyenne = 2.0;

    /** Taux d'absence (%) déclenchant une alerte. Défaut : 15.0 */
    private Double seuilTauxAbsence = 15.0;
}
