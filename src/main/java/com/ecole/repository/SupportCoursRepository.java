package com.ecole.repository;

import com.ecole.entity.SupportCours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportCoursRepository extends JpaRepository<SupportCours, Integer>,
        JpaSpecificationExecutor<SupportCours> {
    List<SupportCours> findByAffectationIdOrderByCreatedAtDesc(Integer affectationId);

    @Query("""
        SELECT DISTINCT s
        FROM SupportCours s
        JOIN FETCH s.affectation a
        WHERE a.classe.id = :classeId
        ORDER BY s.createdAt DESC
    """)
    List<SupportCours> findByClasse(@Param("classeId") Long classeId);
}
