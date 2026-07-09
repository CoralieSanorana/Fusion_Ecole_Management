package com.ecole.repository;

import com.ecole.entity.ProfilParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfilParentRepository extends JpaRepository<ProfilParent, Integer> {

    @Query(value = """
        SELECT pp.* FROM profils_parents pp
        JOIN etudiants_parents ep ON ep.parent_id = pp.id
        WHERE ep.etudiant_id = :etudiantId
          AND ep.est_contact_principal = true
        LIMIT 1
        """, nativeQuery = true)
    Optional<ProfilParent> findContactPrincipalByEtudiantId(@Param("etudiantId") Long etudiantId);
}