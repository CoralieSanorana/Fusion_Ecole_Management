package com.ecole.repository;

import com.ecole.entity.Coefficient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CoefficientRepository extends JpaRepository<Coefficient, Long> {

    @Query(value = "SELECT * FROM coefficients WHERE matiere_id = :matiereId AND niveau_id = :niveauId", nativeQuery = true)
    List<Coefficient> findCoefficientsParMatiere(@Param("matiereId") Long matiereId,
            @Param("niveauId") Long niveauId);

    @Query(value = "SELECT * FROM coefficients WHERE niveau_id = :niveauId", nativeQuery = true)
    List<Coefficient> findByNiveauId(@Param("niveauId") Long niveauId);

    default Map<Long, BigDecimal> findCoefficientsMapByNiveau(Long niveauId) {
        return findByNiveauId(niveauId).stream()
                .collect(Collectors.toMap(Coefficient::getMatiereId, Coefficient::getValeur));
    }

}
