package com.ecole.repository;

import com.ecole.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.salle.id = :salleId AND e.affectation.anneeScolaire.id = :anneeScolaireId")
    List<EmploiDuTemps> findBySalleIdAndAnneeScolaireId(@Param("salleId") Long salleId, @Param("anneeScolaireId") Long anneeScolaireId);
    List<EmploiDuTemps> findByAffectation_Id(Long affectationId);
    boolean existsByHoraireEdt_Id(Long horaireEdtId);
}
