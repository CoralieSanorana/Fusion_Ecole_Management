package com.ecole.repository;

import com.ecole.entity.TypeDepense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeDepenseRepository extends JpaRepository<TypeDepense, Long> {
    List<TypeDepense> findAllByOrderByLibelleAsc();
}
