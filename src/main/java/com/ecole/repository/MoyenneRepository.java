package com.ecole.repository;

import com.ecole.entity.Moyenne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoyenneRepository extends JpaRepository<Moyenne, Long> {

    /**
     * Moyennes GÉNÉRALES (matiere_id IS NULL) pour un ensemble d'inscriptions
     * et de périodes données. Sert au module "Statistiques Élèves" (décrochage).
     * Object[] -> [0]=etudiant_id, [1]=inscription_id, [2]=periode_id, [3]=valeur
     */
    @Query(value = """
            SELECT m.etudiant_id, m.inscription_id, m.periode_id, m.valeur
            FROM moyennes m
            WHERE m.matiere_id IS NULL
              AND m.periode_id IN (:periodeIds)
              AND m.inscription_id IN (:inscriptionIds)
            """, nativeQuery = true)
    List<Object[]> findMoyennesGeneralesParPeriodes(@Param("periodeIds") List<Long> periodeIds,
                                                      @Param("inscriptionIds") List<Long> inscriptionIds);
}

