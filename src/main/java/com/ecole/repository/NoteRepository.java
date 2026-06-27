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
    
    @Query("SELECT n FROM Note n JOIN AffectationEnseignement a ON n.affectationId = a.id WHERE n.etudiantId = :etudiantId AND a.matiere.id = :matiereId")
    List<Note> findByEtudiantIdByMatiereId(@Param("etudiantId") Long etudiantId, @Param("matiereId") Long matiereId);

    @Query("SELECT n FROM Note n " +
       "JOIN AffectationEnseignement a ON n.affectationId = a.id " + // Vérifiez aussi si affectationId ne devrait pas être n.affectation.id
       "JOIN Periode p ON n.periodeId = p.id " +
       "WHERE n.etudiantId = :etudiantId AND a.matiere.id = :matiereId AND p.id = :periodeId")
    List<Note> findByEtudiantIdByMatiereIdByPeriodeId(
        @Param("etudiantId") Long etudiantId, 
        @Param("matiereId") Long matiereId, 
        @Param("periodeId") Long periodeId
    );
    @Query("SELECT n FROM Note n JOIN Periode p ON n.periodeId = p.id WHERE n.etudiantId = :etudiantId AND p.id = :periodeId")
    List<Note> findByEtudiantIdByPeriodeId(@Param("etudiantId") Long etudiantId, @Param("periodeId") Long periodeId);    

}
