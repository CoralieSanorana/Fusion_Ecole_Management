package com.ecole.repository;

import com.ecole.entity.ProfilEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfilEtudiantRepository
        extends JpaRepository<ProfilEtudiant, Long>,
                JpaSpecificationExecutor<ProfilEtudiant> {  // ← ajout pour Specification

    List<ProfilEtudiant> findByIsArchivedFalseOrderByNomAsc();

    Optional<ProfilEtudiant> findByMatricule(String matricule);

    Optional<ProfilEtudiant> findByUserId(Long userId);

    long countByIsArchivedFalse();

    @Query(value = """
            SELECT pe.id, pe.matricule, pe.nom, pe.prenom, pe.telephone,
                   u.email,
                   i.id       AS inscription_id,
                   i.statut   AS statut_inscription,
                   c.nom      AS classe_nom,
                   n.libelle  AS niveau_libelle
            FROM profils_etudiants pe
            LEFT JOIN inscriptions i   ON i.etudiant_id = pe.id
            LEFT JOIN classes c         ON c.id = i.classe_id
            LEFT JOIN niveaux n         ON n.id = c.niveau_id
            LEFT JOIN users u           ON u.id = pe.user_id
            WHERE i.annee_scolaire_id = :anneeId
              AND pe.is_archived = false
            ORDER BY pe.nom ASC
            """, nativeQuery = true)
    List<Object[]> findAvecInscription(@Param("anneeId") Long anneeId);

    List<ProfilEtudiant> findByIsArchivedFalse();

    boolean existsByMatricule(String matricule);

    @Query("SELECT e FROM ProfilEtudiant e WHERE e.isArchived = false AND (" +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.matricule) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ProfilEtudiant> searchByNomOrPrenomOrMatricule(@Param("search") String search);

    // Recherche par classe — via relation @ManyToOne
    @Query("SELECT e FROM ProfilEtudiant e " +
       "JOIN Inscription i ON i.etudiant = e " +
       "WHERE e.isArchived = false " +
       "AND i.statut = 'active' " +
       "AND i.classeId = :classeId") 
    List<ProfilEtudiant> findByClasseId(@Param("classeId") Long classeId);

    // Recherche par niveau — via nom de classe
    @Query("SELECT e FROM ProfilEtudiant e " +
       "JOIN Inscription i ON i.etudiant = e " +
       "JOIN Classe c ON i.classeId = c.id " + // <-- On fait la jointure explicite avec l'entité Classe ici
       "WHERE e.isArchived = false " +
       "AND i.statut = 'active' " +
       "AND LOWER(c.nom) LIKE LOWER(CONCAT('%', :niveau, '%'))") // <-- On utilise c.nom au lieu de i.classe.nom
    List<ProfilEtudiant> findByNiveau(@Param("niveau") String niveau);
}
