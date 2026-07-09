package com.ecole.repository;

import com.ecole.entity.Paiement;
import com.ecole.entity.Inscription;
import com.ecole.entity.Echeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer>,
        JpaSpecificationExecutor<Paiement> {

    List<Paiement> findByInscription(Inscription inscription);

    List<Paiement> findByEcheance(Echeance echeance);

    // Toutes les lignes d'un même groupe de paiement
    List<Paiement> findByReferenceTransaction(String referenceTransaction);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.inscription = :inscription")
    BigDecimal totalEncaisseParInscription(Inscription inscription);

    @Query(value = """
            SELECT p.* FROM paiements p
            JOIN inscriptions i ON i.id = p.inscription_id
            JOIN annees_scolaires a ON a.id = i.annee_scolaire_id
            WHERE i.etudiant_id = :etudiantId
              AND a.est_active = true
            ORDER BY p.date_paiement ASC
            """, nativeQuery = true)
    List<Paiement> findByEtudiantIdCurrentYear(@Param("etudiantId") Long etudiantId);
}