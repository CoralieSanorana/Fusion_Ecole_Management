package com.ecole.repository;

import com.ecole.entity.HistoriqueReinscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueReinscriptionRepository extends JpaRepository<HistoriqueReinscription, Long> {
}
