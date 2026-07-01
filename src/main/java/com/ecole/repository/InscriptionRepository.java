package com.ecole.repository;

import com.ecole.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByClasseId(Long classeId);
    @Query(value = "SELECT * FROM inscriptions WHERE etudiant_id = :etudiantId AND statut = 'active'", nativeQuery = true)
    List<Inscription> findActiveByEtudiant(@Param("etudiantId") Long etudiantId);

    @Query(value = "SELECT * FROM inscriptions WHERE classe_id = :classeId AND statut = 'active'", nativeQuery = true)
    List<Inscription> findActiveByClasse(@Param("classeId") Long classeId);
}
