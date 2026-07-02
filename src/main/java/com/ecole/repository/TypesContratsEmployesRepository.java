package com.ecole.repository;

import com.ecole.entity.TypesContratsEmployes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypesContratsEmployesRepository extends JpaRepository<TypesContratsEmployes, Long> {
}
