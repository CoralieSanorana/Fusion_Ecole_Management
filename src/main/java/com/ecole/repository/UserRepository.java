package com.ecole.repository;

import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = "SELECT r.nom FROM roles r JOIN user_roles ur ON ur.role_id = r.id WHERE ur.user_id = :userId ORDER BY r.nom", nativeQuery = true)
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM ProfilEtudiant p WHERE p.userId = :userId")
    ProfilEtudiant findProfilEtudiantByUserId(@Param("userId") Long userId);

    @Query(value = """
            SELECT salle_id FROM inscriptions
            JOIN classes ON inscriptions.classe_id = classes.id
            WHERE inscriptions.etudiant_id = :etudiant_id
            AND inscriptions.annee_scolaire_id = :annee_scolaire_id
            """, nativeQuery = true)
    Integer getSalleEtudiant(@Param("etudiant_id") Integer etudiant_id,
            @Param("annee_scolaire_id") Integer annee_scolaire_id);
}
