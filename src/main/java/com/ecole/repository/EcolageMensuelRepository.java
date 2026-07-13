package com.ecole.repository;

import com.ecole.entity.EcolageMensuel;
import com.ecole.entity.Niveau;
import com.ecole.entity.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EcolageMensuelRepository extends JpaRepository<EcolageMensuel, Integer> {

    Optional<EcolageMensuel> findByNiveauAndAnneeScolaire(Niveau niveau, AnneeScolaire anneeScolaire);

    List<EcolageMensuel> findByAnneeScolaire(AnneeScolaire anneeScolaire);
}
