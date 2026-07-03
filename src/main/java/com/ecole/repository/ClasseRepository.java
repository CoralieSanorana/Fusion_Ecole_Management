package com.ecole.repository;

import com.ecole.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    // Sert à itérer classe par classe pour construire les nuages de points
    // du module "Statistiques Élèves".
    List<Classe> findByAnneeScolaire_Id(Long anneeScolaireId);
    
    @Query("SELECT c FROM Classe c JOIN c.anneeScolaire a WHERE a.estActive = true ORDER BY c.nom")
    List<Classe> findAllActiveAnnee();
}