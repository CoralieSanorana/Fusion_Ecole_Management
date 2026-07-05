package com.ecole.repository;

import com.ecole.entity.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {

    List<Depense> findByAnneeScolaire_IdAndDateDepenseBetweenOrderByDateDepenseDesc(Long anneeScolaireId,
                                                                                    LocalDate dateDebut,
                                                                                    LocalDate dateFin);

    @Query("""
            SELECT COALESCE(SUM(d.total), 0)
            FROM Depense d
            WHERE d.anneeScolaire.id = :anneeScolaireId
              AND d.dateDepense BETWEEN :dateDebut AND :dateFin
            """)
    BigDecimal sumTotalByAnneeScolaireAndDateBetween(@Param("anneeScolaireId") Long anneeScolaireId,
                                                     @Param("dateDebut") LocalDate dateDebut,
                                                     @Param("dateFin") LocalDate dateFin);
}
