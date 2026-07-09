package com.ecole.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Inscription;
import com.ecole.entity.ProfilEtudiant;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByClasseId(Long classeId);
    
    @Query(value = "SELECT * FROM inscriptions WHERE etudiant_id = :etudiantId AND statut = 'active'", nativeQuery = true)
    List<Inscription> findActiveByEtudiant(@Param("etudiantId") Long etudiantId);

    @Query(value = "SELECT * FROM inscriptions WHERE classe_id = :classeId AND statut = 'active'", nativeQuery = true)
    List<Inscription> findActiveByClasse(@Param("classeId") Long classeId);

    Optional<Inscription> findByEtudiantIdAndAnneeScolaireId(Long etudiantId, Long anneeId);
    
    @Query("SELECT i FROM Inscription i " +
          "JOIN AnneeScolaire a ON i.anneeScolaireId = a.id " +
          "WHERE a = :anneeScolaire AND i.statut = :statut")
    List<Inscription> findByAnneeScolaireAndStatut(
        @Param("anneeScolaire") AnneeScolaire anneeScolaire, 
        @Param("statut") String statut
    );
    
    long countByAnneeScolaireIdAndStatut(Long anneeId, String statut);
    
    List<Inscription> findByEtudiantId(Long etudiantId);

    /**
     * Élèves activement inscrits pour une année scolaire (et éventuellement une classe précise),
     * avec leurs infos d'identité + classe + niveau. Sert au module "Statistiques Élèves".
     * Object[] -> [0]=inscription_id, [1]=etudiant_id, [2]=matricule, [3]=nom, [4]=prenom,
     *             [5]=photo_url, [6]=classe_id, [7]=classe_nom, [8]=niveau_libelle
     */
    @Query(value = """
            SELECT i.id          AS inscription_id,
                   i.etudiant_id AS etudiant_id,
                   pe.matricule  AS matricule,
                   pe.nom        AS nom,
                   pe.prenom     AS prenom,
                   pe.photo_url  AS photo_url,
                   c.id          AS classe_id,
                   c.nom         AS classe_nom,
                   n.libelle     AS niveau_libelle
            FROM inscriptions i
            JOIN profils_etudiants pe ON pe.id = i.etudiant_id
            JOIN classes c            ON c.id = i.classe_id
            LEFT JOIN niveaux n        ON n.id = c.niveau_id
            WHERE i.annee_scolaire_id = :anneeId
              AND i.statut = 'active'
              AND pe.is_archived = false
              AND (:classeId IS NULL OR i.classe_id = :classeId)
            ORDER BY c.nom ASC, pe.nom ASC, pe.prenom ASC
            """, nativeQuery = true)
    List<Object[]> findElevesActifsPourStatistiques(@Param("anneeId") Long anneeId,
                                                      @Param("classeId") Long classeId);
    Optional<Inscription> findByEtudiantAndStatut(ProfilEtudiant etudiant, String statut);

    @Query(value = """
            SELECT i.* FROM inscriptions i
            JOIN annees_scolaires a ON a.id = i.annee_scolaire_id
            WHERE i.etudiant_id = :etudiantId
              AND a.est_active = true
            LIMIT 1
            """, nativeQuery = true)
    Optional<Inscription> findActiveByEtudiantId(@Param("etudiantId") Long etudiantId);

}

