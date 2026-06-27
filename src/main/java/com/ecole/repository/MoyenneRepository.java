package com.ecole.repository;

import com.ecole.entity.Moyenne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoyenneRepository extends JpaRepository<Moyenne, Long> {
}
