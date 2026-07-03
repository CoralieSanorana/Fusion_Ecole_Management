package com.ecole.repository;

import com.ecole.entity.Echeance;
import com.ecole.entity.Echeancier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<Echeance, Long> {
    List<Echeance> findByEcheancierAndEstSoldeeFalse(Echeancier echeancier);
    List<Echeance> findByEcheancier(Echeancier echeancier);
}