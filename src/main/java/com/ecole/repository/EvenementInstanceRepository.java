package com.ecole.repository;

import com.ecole.entity.EvenementInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvenementInstanceRepository extends JpaRepository<EvenementInstance, Long> {
}
