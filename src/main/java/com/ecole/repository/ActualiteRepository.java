package com.ecole.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecole.entity.Actualite;

@Repository
public interface ActualiteRepository extends JpaRepository<Actualite, Long> {

    @Query("SELECT a FROM Actualite a WHERE a.estActive = true ORDER BY a.datePublication DESC")
    List<Actualite> findAllActive();

    @Query("SELECT a FROM Actualite a WHERE a.estActive = true AND a.categorie = ?1 ORDER BY a.datePublication DESC")
    List<Actualite> findByCategorie(String categorie);

    @Query("SELECT a FROM Actualite a WHERE a.estActive = true ORDER BY a.datePublication DESC LIMIT 10")
    List<Actualite> findTop10Active();
}
