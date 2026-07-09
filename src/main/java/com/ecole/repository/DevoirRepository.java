package com.ecole.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ecole.entity.AffectationEnseignement;
import com.ecole.entity.Devoir;

public interface DevoirRepository extends JpaRepository<Devoir, Long> {

    List<Devoir> findByAffectation(AffectationEnseignement affectation);

    List<Devoir> findByEstActifTrue();

    List<Devoir> findByDateLimiteAfter(LocalDate date);

    List<Devoir> findByAffectationAndEstActif(AffectationEnseignement affectation, Boolean estActif);



    @Query(value = "SELECT DISTINCT d.* FROM devoirs d JOIN affectations_enseignement a ON d.affectation_id = a.id WHERE a.classe_id = :classeId AND d.est_actif = true ORDER BY d.date_publication DESC", nativeQuery = true)
    List<Devoir> findByClasse(@Param("classeId") Long classeId);

}
