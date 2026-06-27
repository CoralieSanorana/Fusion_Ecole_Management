package com.ecole.service;

import com.ecole.entity.AnneeScolaire;
import com.ecole.repository.AnneeScolaireRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AnneeScolaireService {

    private final AnneeScolaireRepository anneeScolaireRepository;

    public AnneeScolaireService(AnneeScolaireRepository anneeScolaireRepository) {
        this.anneeScolaireRepository = anneeScolaireRepository;
    }

    public List<AnneeScolaire> findAll() {
        return anneeScolaireRepository.findAll();
    }

    public Optional<AnneeScolaire> findById(Long id) {
        return anneeScolaireRepository.findById(id);
    }

    public Optional<AnneeScolaire> findByEstActive(Boolean estActive) {
        return anneeScolaireRepository.findByEstActive(estActive);
    }

    public AnneeScolaire save(AnneeScolaire anneeScolaire) {
        return anneeScolaireRepository.save(anneeScolaire);
    }

    public void deleteById(Long id) {
        anneeScolaireRepository.deleteById(id);
    }
}
