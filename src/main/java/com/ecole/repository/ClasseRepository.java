package com.ecole.repository;

import com.ecole.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
}
