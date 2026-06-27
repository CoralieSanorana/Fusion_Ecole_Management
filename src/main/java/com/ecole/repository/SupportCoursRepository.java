package com.ecole.repository;

import com.ecole.entity.SupportCours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportCoursRepository extends JpaRepository<SupportCours, Long> {
    List<SupportCours> findByAffectationIdOrderByCreatedAtDesc(Long affectationId);
}