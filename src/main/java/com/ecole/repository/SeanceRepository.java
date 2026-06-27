package com.ecole.repository;

import com.ecole.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    Optional<Seance> findByEmploiDuTempsIdAndDateSeance(Integer emploiDuTempsId, LocalDate dateSeance);
}
