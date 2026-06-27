package com.ecole.repository;

import com.ecole.entity.TypeFichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeFichierRepository extends JpaRepository<TypeFichier, Long> {
}
