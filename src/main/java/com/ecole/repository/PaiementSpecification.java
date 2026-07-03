package com.ecole.repository;

import com.ecole.entity.Paiement;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaiementSpecification {

    public static Specification<Paiement> filtrer(
            String nom,
            String classe,
            String modePaiement,
            LocalDate dateDebut,
            LocalDate dateFin) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Jointures
            Join<?, ?> inscription = root.join("inscription");
            Join<?, ?> etudiant    = inscription.join("etudiant");
            Join<?, ?> classeJoin  = inscription.join("classe");

            // Filtre nom ou prénom élève
            if (nom != null && !nom.isBlank()) {
                String pattern = "%" + nom.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(etudiant.get("nom")), pattern),
                    cb.like(cb.lower(etudiant.get("prenom")), pattern)
                ));
            }

            // Filtre classe
            if (classe != null && !classe.isBlank()) {
                predicates.add(cb.like(
                    cb.lower(classeJoin.get("nom")),
                    "%" + classe.toLowerCase() + "%"
                ));
            }

            // Filtre mode paiement
            if (modePaiement != null && !modePaiement.isBlank()) {
                predicates.add(cb.equal(root.get("modePaiement"), modePaiement));
            }

            // Filtre date début
            if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("datePaiement"), dateDebut));
            }

            // Filtre date fin
            if (dateFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("datePaiement"), dateFin));
            }

            // Tri par date décroissante
            query.orderBy(cb.desc(root.get("datePaiement")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}