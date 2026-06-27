package com.ecole.repository;

import com.ecole.entity.ProfilsSecretariat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilsSecretariatRepository extends JpaRepository<ProfilsSecretariat, Long> {
    Optional<ProfilsSecretariat> findByUserId(Long userId);
    Optional<ProfilsSecretariat> findByTelephone(String telephone);
}
