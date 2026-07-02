package com.ecole.dto.Directeur;

import lombok.Data;
import java.time.LocalDate;

/**
 * Critères de filtre pour la recherche paginée d'étudiants.
 *
 * Tous les champs sont optionnels (null = pas de filtre sur ce critère).
 */
@Data
public class EtudiantFilterCriteria {

    // ── Recherche texte libre ────────────────────────────────────
    /** Cherche dans nom OU prénom OU matricule (ILIKE) */
    private String recherche;

    // ── Filtres identité ─────────────────────────────────────────
    private String sexe;          // 'M' ou 'F'
    private String region;
    private String nationalite;

    // ── Filtres scolarité ────────────────────────────────────────
    private Long    classeId;
    private Long    niveauId;
    private Long    anneeScolaireId;
    private String  statutInscription; // 'active','transfere','exclu','diplome','abandonne'
    private String  typeInscription;   // 'nouvelle','reinscription'

    // ── Bornes date inscription ───────────────────────────────────
    private LocalDate dateInscriptionDebut;
    private LocalDate dateInscriptionFin;

    // ── Bornes date de naissance ─────────────────────────────────
    private LocalDate dateNaissanceDebut;
    private LocalDate dateNaissanceFin;

    // ── Archivage ────────────────────────────────────────────────
    /** null = tous, false = actifs seulement (défaut), true = archivés seulement */
    private Boolean isArchived = false;

    // ── Pagination ───────────────────────────────────────────────
    private int page     = 0;
    private int pageSize = 15;

    // ── Tri ──────────────────────────────────────────────────────
    /** Champ de tri : 'nom', 'prenom', 'matricule', 'dateInscription', 'classe' */
    private String sortBy    = "nom";
    private String sortDir   = "asc";   // 'asc' ou 'desc'
}
