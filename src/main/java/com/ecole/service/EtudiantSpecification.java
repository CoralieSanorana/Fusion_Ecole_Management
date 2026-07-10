package com.ecole.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ecole.dto.Directeur.EtudiantFilterCriteria;
import com.ecole.entity.Classe;
import com.ecole.entity.Inscription;
import com.ecole.entity.Niveau;
import com.ecole.entity.ProfilEtudiant;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Specification JPA pour le filtre multi-critères d'étudiants.
 *
 * Fait des JOIN : ProfilEtudiant → Inscription → Classe → Niveau → AnneeScolaire
 * et sur ProfilEtudiant.user → User (pour email).
 *
 * Tous les prédicats sont combinés en AND.
 * Le texte libre (recherche) est un OR sur nom/prénom/matricule.
 */
public class EtudiantSpecification implements Specification<ProfilEtudiant> {

    private final EtudiantFilterCriteria c;

    public EtudiantSpecification(EtudiantFilterCriteria c) {
        this.c = c;
    }

@Override
public Predicate toPredicate(Root<ProfilEtudiant> root,
                             CriteriaQuery<?> query,
                             CriteriaBuilder cb) {

    List<Predicate> predicates = new ArrayList<>();

    Join<ProfilEtudiant, Inscription> inscJoin = root.join("inscriptions", JoinType.LEFT);

    // Join<ProfilEtudiant, User> userJoin =
    //         root.join("user", JoinType.LEFT);

    query.distinct(true);

    // ── Archivage ──
    if (c.getIsArchived() != null) {
        predicates.add(cb.equal(root.get("isArchived"), c.getIsArchived()));
    }

    // ── Recherche texte ──
    if (hasText(c.getRecherche())) {
        String like = "%" + c.getRecherche().toLowerCase() + "%";
        predicates.add(cb.or(
                cb.like(cb.lower(root.get("nom")), like),
                cb.like(cb.lower(root.get("prenom")), like),
                cb.like(cb.lower(root.get("matricule")), like)
        ));
    }

    // ── Identité ──
    if (hasText(c.getSexe())) {
        predicates.add(cb.equal(root.get("sexe"), c.getSexe()));
    }

    if (hasText(c.getRegion())) {
        predicates.add(cb.like(cb.lower(root.get("region")),
                "%" + c.getRegion().toLowerCase() + "%"));
    }

    if (hasText(c.getNationalite())) {
        predicates.add(cb.equal(root.get("nationalite"), c.getNationalite()));
    }

    // ── SCOLARITÉ (FIX IMPORTANT) ──
    // ❌ PLUS DE JOIN classe/niveau/annee
    // on utilise directement les IDs

    if (c.getClasseId() != null) {
        predicates.add(cb.equal(inscJoin.get("classeId"), c.getClasseId()));
    }

    if (c.getNiveauId() != null) {
        Join<Inscription, Classe> classeJoin = inscJoin.join("classe", JoinType.INNER);
        Join<Classe, Niveau> niveauJoin = classeJoin.join("niveau", JoinType.INNER);
        predicates.add(cb.equal(niveauJoin.get("id"), c.getNiveauId()));
    }

    if (c.getAnneeScolaireId() != null) {
        predicates.add(cb.equal(inscJoin.get("anneeScolaireId"), c.getAnneeScolaireId()));
    }

    if (hasText(c.getStatutInscription())) {
        predicates.add(cb.equal(inscJoin.get("statut"), c.getStatutInscription()));
    }

    if (hasText(c.getTypeInscription())) {
        predicates.add(cb.equal(inscJoin.get("typeInscription"), c.getTypeInscription()));
    }

    // ── Dates inscription ──
    if (c.getDateInscriptionDebut() != null) {
        predicates.add(cb.greaterThanOrEqualTo(
                inscJoin.get("dateInscription"), c.getDateInscriptionDebut()));
    }

    if (c.getDateInscriptionFin() != null) {
        predicates.add(cb.lessThanOrEqualTo(
                inscJoin.get("dateInscription"), c.getDateInscriptionFin()));
    }

    // ── Dates naissance ──
    if (c.getDateNaissanceDebut() != null) {
        predicates.add(cb.greaterThanOrEqualTo(
                root.get("dateNaissance"), c.getDateNaissanceDebut()));
    }

    if (c.getDateNaissanceFin() != null) {
        predicates.add(cb.lessThanOrEqualTo(
                root.get("dateNaissance"), c.getDateNaissanceFin()));
    }

    return cb.and(predicates.toArray(new Predicate[0]));
}

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
