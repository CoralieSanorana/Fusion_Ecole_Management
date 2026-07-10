package com.ecole.repository;

import com.ecole.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    List<Absence> findBySeanceId(Long seanceId);

    /**
     * Nombre de séances réellement données pour une classe sur une plage de dates
     * (toutes matières confondues). Sert de dénominateur pour le taux d'absence.
     */
    @Query(value = """
            SELECT COUNT(DISTINCT s.id)
            FROM seances s
            JOIN emploi_du_temps edt ON edt.id = s.emploi_du_temps_id
            JOIN affectations_enseignement ae ON ae.id = edt.affectation_id
            WHERE ae.classe_id = :classeId
              AND s.a_eu_lieu = true
              AND s.date_seance BETWEEN :dateDebut AND :dateFin
            """, nativeQuery = true)
    Long countSeancesPrevues(@Param("classeId") Long classeId,
                              @Param("dateDebut") LocalDate dateDebut,
                              @Param("dateFin") LocalDate dateFin);

    /**
     * Nombre d'absences (justifiées + non justifiées, hors simples retards) par étudiant
     * d'une classe sur une plage de dates. Sert au module "Statistiques Élèves".
     * Object[] -> [0]=etudiant_id, [1]=nb_absences
     */
    @Query(value = """
            SELECT a.etudiant_id, COUNT(*)
            FROM absences a
            JOIN seances s ON s.id = a.seance_id
            JOIN emploi_du_temps edt ON edt.id = s.emploi_du_temps_id
            JOIN affectations_enseignement ae ON ae.id = edt.affectation_id
            WHERE ae.classe_id = :classeId
              AND s.a_eu_lieu = true
              AND s.date_seance BETWEEN :dateDebut AND :dateFin
              AND a.type IN ('non_justifiee', 'justifiee')
            GROUP BY a.etudiant_id
            """, nativeQuery = true)
    List<Object[]> countAbsencesParEtudiant(@Param("classeId") Long classeId,
                                             @Param("dateDebut") LocalDate dateDebut,
                                             @Param("dateFin") LocalDate dateFin);

    @Query(value = """
            SELECT a.id,
                   a.type,
                   a.motif,
                   a.justificatif_url,
                   a.created_at,
                   s.date_seance,
                   s.heure_debut,
                   s.heure_fin,
                   m.nom AS matiere_nom,
                   c.nom AS classe_nom,
                   salle.nom AS salle_nom,
                   prof.nom AS professeur_nom,
                   prof.prenom AS professeur_prenom
            FROM absences a
            LEFT JOIN seances s ON s.id = a.seance_id
            LEFT JOIN emploi_du_temps edt ON edt.id = s.emploi_du_temps_id
            LEFT JOIN affectations_enseignement ae ON ae.id = edt.affectation_id
            LEFT JOIN matieres m ON m.id = ae.matiere_id
            LEFT JOIN classes c ON c.id = ae.classe_id
            LEFT JOIN salles salle ON salle.id = edt.salle_id
            LEFT JOIN profils_professeurs prof ON prof.id = ae.professeur_id
            WHERE a.etudiant_id = :etudiantId
            ORDER BY s.date_seance DESC, s.heure_debut DESC, a.created_at DESC
            """, nativeQuery = true)
    List<Object[]> findHistoriqueEtudiant(@Param("etudiantId") Long etudiantId);

    long countByEtudiantId(Long etudiantId);

    long countByEtudiantIdAndType(Long etudiantId, String type);
}
