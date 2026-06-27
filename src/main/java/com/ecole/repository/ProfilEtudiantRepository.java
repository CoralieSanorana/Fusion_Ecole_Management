package com.ecole.repository;

import com.ecole.entity.ProfilEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilEtudiantRepository extends JpaRepository<ProfilEtudiant, Long> {
}
