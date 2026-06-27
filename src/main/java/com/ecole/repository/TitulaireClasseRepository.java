package com.ecole.repository;

import com.ecole.entity.TitulaireClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TitulaireClasseRepository extends JpaRepository<TitulaireClasse, Integer> {

    // Récupérer le titulaire d'une classe pour une année spécifique (Unique)
    Optional<TitulaireClasse> findByClasseIdAndAnneeScolaireId(Long classeId, Long anneeScolaireId);

    // Trouver toutes les classes dont un professeur a été titulaire
    List<TitulaireClasse> findByProfesseurId(Long professeurId);

    // Trouver la classe dont le prof est titulaire pour une année précise
    Optional<TitulaireClasse> findByProfesseurIdAndAnneeScolaireId(Long professeurId, Long anneeScolaireId);
}