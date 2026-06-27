package com.ecole.repository;

import com.ecole.entity.ProfilsDirecteurs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilsDirecteursRepository extends JpaRepository<ProfilsDirecteurs, Long> {
    Optional<ProfilsDirecteurs> findByUserId(Long userId);
    Optional<ProfilsDirecteurs> findByTelephone(String telephone);
}
