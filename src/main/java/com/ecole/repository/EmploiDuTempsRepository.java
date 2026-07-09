package com.ecole.repository;

import com.ecole.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {
    @Query(value = "SELECT * FROM emploi_du_temps WHERE salle_id = :salleId AND affectation_id IN (SELECT id FROM affectations_enseignement WHERE annee_scolaire_id = :anneeScolaireId)", nativeQuery = true)
    List<EmploiDuTemps> findBySalleIdAndAnneeScolaireId(@Param("salleId") Long salleId, @Param("anneeScolaireId") Long anneeScolaireId);

    @Query(value = "SELECT * FROM emploi_du_temps WHERE salle_id = :salleId", nativeQuery = true)
    List<EmploiDuTemps> findBySalleId(@Param("salleId") Long salleId);

    List<EmploiDuTemps> findByAffectation_Id(Long affectationId);
    boolean existsByHoraireEdt_Id(Long horaireEdtId);

    // Emploi du temps d'une classe, valide à une date donnée (ou sans limite de validité)
   // Emploi du temps d'une classe, valide à une date donnée (ou sans limite de validité)
    @Query("""
        SELECT e
        FROM EmploiDuTemps e
        JOIN FETCH e.affectation a
        JOIN FETCH a.matiere m
        JOIN FETCH a.professeur p
        LEFT JOIN FETCH e.salle s
        WHERE a.classe.id = :classeId
          AND (e.dateDebutValidite IS NULL OR e.dateDebutValidite <= :date)
          AND (e.dateFinValidite IS NULL OR e.dateFinValidite >= :date)
        ORDER BY e.jourSemaine ASC, e.heureDebut ASC
    """)
    List<EmploiDuTemps> findByClasseIdAndDate(
        @Param("classeId") Long classeId,
        @Param("date") LocalDate date
    );

}
