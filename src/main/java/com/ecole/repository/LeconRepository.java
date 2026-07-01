package com.ecole.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecole.entity.AffectationEnseignement;
import com.ecole.entity.Lecon;

public interface LeconRepository extends JpaRepository<Lecon, Long> {

        List<Lecon> findByAffectation(AffectationEnseignement affectation);

        List<Lecon> findByAffectationOrderByDatePublicationDesc(AffectationEnseignement affectation);

        List<Lecon> findByDatePublicationBeforeOrderByDatePublicationDesc(LocalDate date);

        List<Lecon> findByAffectationAndDatePublicationBefore(
                        AffectationEnseignement affectation,
                        LocalDate date);

        @Query(value = "SELECT * FROM lecons WHERE affectation_id = :affectationId AND date_publication <= :date ORDER BY date_publication DESC", nativeQuery = true)
        List<Lecon> findPublieeesParAffectation(
                        @Param("affectationId") Long affectationId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT DISTINCT l.* FROM lecons l JOIN affectations_enseignement a ON l.affectation_id = a.id WHERE a.classe_id = :classeId", nativeQuery = true)
        List<Lecon> findByClasseId(@Param("classeId") Long classeId);
}
