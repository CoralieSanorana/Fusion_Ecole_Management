package com.ecole.repository;

import com.ecole.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByEtudiantId(Long etudiantId);

    @Query(value = "SELECT * FROM notes WHERE etudiant_id = :etudiantId AND affectation_id IN (SELECT id FROM affectations_enseignement WHERE matiere_id = :matiereId)", nativeQuery = true)
    List<Note> findByEtudiantIdByMatiereId(@Param("etudiantId") Long etudiantId, @Param("matiereId") Long matiereId);

    @Query(value = "SELECT * FROM notes WHERE etudiant_id = :etudiantId AND affectation_id IN (SELECT id FROM affectations_enseignement WHERE matiere_id = :matiereId) AND periode_id = :periodeId", nativeQuery = true)
    List<Note> findByEtudiantIdByMatiereIdByPeriodeId(
        @Param("etudiantId") Long etudiantId,
        @Param("matiereId") Long matiereId,
        @Param("periodeId") Long periodeId
    );
    @Query(value = "SELECT * FROM notes WHERE etudiant_id = :etudiantId AND periode_id = :periodeId", nativeQuery = true)
    List<Note> findByEtudiantIdByPeriodeId(@Param("etudiantId") Long etudiantId, @Param("periodeId") Long periodeId);

    @Query(value = "SELECT * FROM notes WHERE etudiant_id = :etudiantId ORDER BY date_saisie DESC", nativeQuery = true)
    List<Note> findNotesParEtudiant(@Param("etudiantId") Long etudiantId);

    @Query(value = "SELECT * FROM notes WHERE etudiant_id = :etudiantId AND periode_id = :periodeId ORDER BY date_saisie DESC", nativeQuery = true)
    List<Note> findNotesParEtudiantEtPeriode(
        @Param("etudiantId") Long etudiantId,
        @Param("periodeId") Long periodeId
    );

    @Query(value = "SELECT * FROM notes WHERE etudiant_id IN (:etudiantIds) AND periode_id = :periodeId", nativeQuery = true)
    List<Note> findNotesParEtudiantsEtPeriode(
        @Param("etudiantIds") List<Long> etudiantIds,
        @Param("periodeId") Long periodeId
    );
}
