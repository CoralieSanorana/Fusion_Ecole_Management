package com.ecole.repository;

import com.ecole.entity.Echeancier;
import com.ecole.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EcheancierRepository extends JpaRepository<Echeancier, Integer> {
    List<Echeancier> findByInscription(Inscription inscription);
}