package com.ecole.repository;

import com.ecole.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PeriodeRepository extends JpaRepository<Periode, Integer> {
    // Permet de récupérer toutes les périodes d'une année scolaire spécifique
    List<Periode> findByAnneeScolaireId(Long anneeScolaireId);
    @Query(value = "SELECT * FROM periodes WHERE annee_scolaire_id = :anneeScolaireId ORDER BY ordre ASC", nativeQuery = true)
    List<Periode> findByAnneeScolaire(@Param("anneeScolaireId") Long anneeScolaireId);

}