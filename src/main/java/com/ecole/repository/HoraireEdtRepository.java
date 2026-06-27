package com.ecole.repository;

import com.ecole.entity.HoraireEdt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HoraireEdtRepository extends JpaRepository<HoraireEdt, Long> {
    List<HoraireEdt> findByNiveauIdOrderByOrdre(Long niveauId);
    List<HoraireEdt> findByNiveauIsNullOrderByOrdre();
    void deleteByNiveauId(Long niveauId);
}
