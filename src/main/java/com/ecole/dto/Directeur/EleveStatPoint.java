package com.ecole.dto.Directeur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ligne par étudiant : sert à la fois de point pour le nuage de points
 * (absence × moyenne) et de ligne pour les listes Rouge / Jaune.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EleveStatPoint {

    // ── Identité ─────────────────────────────────────────────────
    private Long   etudiantId;
    private String matricule;
    private String nom;
    private String prenom;
    private String photoUrl;

    // ── Scolarité ────────────────────────────────────────────────
    private Long   classeId;
    private String classeNom;
    private Long   inscriptionId;

    // ── Axe Y du nuage de points : moyennes par période consécutive ─
    /** Moyenne générale sur chacune des 3 périodes analysées, dans l'ordre chronologique. */
    private List<BigDecimal> moyennesParPeriode;

    /** Moyenne générale de la période la plus récente (= axe Y du nuage de points). */
    private BigDecimal moyenneRecente;

    /** Différence (moyenne période la plus ancienne − moyenne période la plus récente). Positif = baisse. */
    private BigDecimal deltaMoyenne;

    // ── Axe X du nuage de points : assiduité ────────────────────────
    /** Nombre total de séances prévues sur la période d'analyse. */
    private Integer nbSeancesPrevues;

    /** Nombre de séances où l'étudiant a été marqué absent. */
    private Integer nbAbsences;

    /** Taux d'absence en % sur la période d'analyse (= axe X du nuage de points). */
    private BigDecimal tauxAbsence;

    // ── Alerte décrochage ────────────────────────────────────────
    private NiveauAlerte niveauAlerte;

    /** Explications lisibles, ex: ["Baisse de moyenne de 2.5 pts", "Taux d'absence 18.0% (seuil 15.0%)"] */
    private List<String> motifsAlerte;
}
