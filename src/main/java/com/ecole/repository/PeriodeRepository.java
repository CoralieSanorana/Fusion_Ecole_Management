package com.ecole.repository;

import com.ecole.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PeriodeRepository extends JpaRepository<Periode, Integer> {
    // Permet de récupérer toutes les périodes d'une année scolaire spécifique
    List<Periode> findByAnneeScolaireId(Long anneeScolaireId);
}