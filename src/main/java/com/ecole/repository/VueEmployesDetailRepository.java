package com.ecole.repository;

import com.ecole.entity.VueEmployesDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VueEmployesDetailRepository extends JpaRepository<VueEmployesDetail, Long> {
    List<VueEmployesDetail> findByRoleNomIn(List<String> roleNoms);
    List<VueEmployesDetail> findByIsActiveTrue();
    Optional<VueEmployesDetail> findByUserId(Long userId);

    @Query("SELECT e FROM VueEmployesDetail e WHERE e.isActive = true " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.fonction) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:roleNom IS NULL OR e.roleNom = :roleNom) " +
           "AND (:matiereId IS NULL OR e.idMatiere = :matiereId) " +
           "AND (:salaireMin IS NULL OR e.salaireMensuel >= :salaireMin) " +
           "AND (:salaireMax IS NULL OR e.salaireMensuel <= :salaireMax)")
    List<VueEmployesDetail> filterEmployes(@Param("keyword") String keyword,
                                          @Param("roleNom") String roleNom,
                                          @Param("matiereId") Long matiereId,
                                          @Param("salaireMin") BigDecimal salaireMin,
                                          @Param("salaireMax") BigDecimal salaireMax);
}
