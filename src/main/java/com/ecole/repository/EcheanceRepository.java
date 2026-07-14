package com.ecole.repository;

import com.ecole.entity.Echeance;
import com.ecole.entity.Echeancier;
import com.ecole.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    // ── Nouveau flux mensuel ──────────────────────────────────

    List<Echeance> findByInscriptionOrderByAnneeAscMoisAsc(Inscription inscription);

    List<Echeance> findByInscriptionAndEstSoldeeFalseOrderByAnneeAscMoisAsc(Inscription inscription);

    Optional<Echeance> findByInscriptionAndMoisAndAnnee(Inscription inscription, Integer mois, Integer annee);

    // Corrigé — passe par anneeScolaireId au lieu de la relation
    @Query("""
        SELECT e FROM Echeance e
        JOIN e.inscription i
        JOIN AnneeScolaire a ON a.id = i.anneeScolaireId
        WHERE a.estActive = true
        AND e.estSoldee = false
        ORDER BY e.annee ASC, e.mois ASC
        """)
    List<Echeance> findAllImpayes();

    // Impayés par classe
    @Query("""
        SELECT e FROM Echeance e
        JOIN e.inscription i
        WHERE i.classeId = :classeId
        AND e.estSoldee = false
        ORDER BY e.annee ASC, e.mois ASC
        """)
    List<Echeance> findImpayes(@Param("classeId") Long classeId);

    // ── Ancien flux tranches — gardé pour compatibilité ──────

    List<Echeance> findByEcheancierAndEstSoldeeFalse(Echeancier echeancier);
    List<Echeance> findByEcheancier(Echeancier echeancier);
}