package com.ecole.dto.Directeur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse complète du module "Statistiques Élèves".
 * Contient les paramètres effectifs de l'analyse, les listes d'alerte actionnables
 * (Rouge / Jaune) et le détail par classe pour les nuages de points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesElevesResponse {

    // ── Contexte de l'analyse (rappel des paramètres effectifs) ────
    private String anneeLibelle;
    /** Libellés des 3 périodes consécutives analysées, dans l'ordre chronologique. */
    private List<String> periodesAnalysees;
    private Double seuilBaisseMoyenne;
    private Double seuilTauxAbsence;

    // ── Indicateurs globaux ──────────────────────────────────────
    private Integer effectifTotal;
    private Integer nbAlerteRouge;
    private Integer nbAlerteJaune;
    /** Corrélation de Pearson globale (toutes classes confondues) absence × moyenne. */
    private Double correlationGlobale;

    // ── Données actionnables ─────────────────────────────────────
    /** Étudiants en Liste Rouge, triés par baisse de moyenne décroissante. */
    private List<EleveStatPoint> listeRouge;

    /** Étudiants en Liste Jaune, triés par baisse de moyenne décroissante. */
    private List<EleveStatPoint> listeJaune;

    // ── Visualisation ─────────────────────────────────────────────
    /** Nuage de points + corrélation, un bloc par classe. */
    private List<ClasseStatistique> classes;
}
