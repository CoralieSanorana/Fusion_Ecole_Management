package com.ecole.repository;

import com.ecole.entity.ContratsEmployes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratsEmployesRepository extends JpaRepository<ContratsEmployes, Long> {
}
