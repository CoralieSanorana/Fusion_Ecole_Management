package com.ecole.repository;

import com.ecole.entity.Periode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PeriodeRepository extends JpaRepository<Periode, Integer> {
    // Permet de récupérer toutes les périodes d'une année scolaire spécifique
    List<Periode> findByAnneeScolaireId(Long anneeScolaireId);

    // Triées par ordre croissant (1, 2, 3...) — sert à isoler les 3 dernières périodes consécutives
    // pour le module "Statistiques Élèves" (détection décrochage).
    List<Periode> findByAnneeScolaireIdOrderByOrdreAsc(Long anneeScolaireId);
}