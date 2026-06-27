package com.ecole.repository;

import com.ecole.entity.ProfilsComptables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilsComptablesRepository extends JpaRepository<ProfilsComptables, Long> {
    Optional<ProfilsComptables> findByUserId(Long userId);
    Optional<ProfilsComptables> findByTelephone(String telephone);
}
