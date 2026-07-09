package com.ecole.repository;

import com.ecole.entity.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {
    Optional<AnneeScolaire> findByEstActiveTrue();
    Optional<AnneeScolaire> findByEstActive(Boolean estActive);
}
