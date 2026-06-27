package com.ecole.repository;

import com.ecole.entity.ProfilsProfesseurs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilsProfesseursRepository extends JpaRepository<ProfilsProfesseurs, Long> {
    Optional<ProfilsProfesseurs> findByUserId(Long userId);
    Optional<ProfilsProfesseurs> findByTelephone(String telephone);
}
