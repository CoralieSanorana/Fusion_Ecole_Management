package com.ecole.service;

import com.ecole.entity.TitulaireClasse;
import com.ecole.repository.TitulaireClasseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TitulaireClasseService {

    private final TitulaireClasseRepository titulaireClasseRepository;

    public TitulaireClasseService(TitulaireClasseRepository titulaireClasseRepository) {
        this.titulaireClasseRepository = titulaireClasseRepository;
    }

    public List<TitulaireClasse> findAll() {
        return titulaireClasseRepository.findAll();
    }

    public Optional<TitulaireClasse> findById(Integer id) {
        return titulaireClasseRepository.findById(id);
    }

    public Optional<TitulaireClasse> findByClasseIdAndAnneeScolaireId(Long classeId, Long anneeScolaireId) {
        return titulaireClasseRepository.findByClasseIdAndAnneeScolaireId(classeId, anneeScolaireId);
    }

    public List<TitulaireClasse> findByProfesseurId(Long professeurId) {
        return titulaireClasseRepository.findByProfesseurId(professeurId);
    }

    public Optional<TitulaireClasse> findByProfesseurIdAndAnneeScolaireId(Long professeurId, Long anneeScolaireId) {
        return titulaireClasseRepository.findByProfesseurIdAndAnneeScolaireId(professeurId, anneeScolaireId);
    }

    public TitulaireClasse save(TitulaireClasse titulaireClasse) {
        return titulaireClasseRepository.save(titulaireClasse);
    }

    public void delete(Integer id) {
        titulaireClasseRepository.deleteById(id);
    }
}