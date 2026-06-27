package com.ecole.service;

import com.ecole.entity.AffectationEnseignement;
import com.ecole.repository.AffectationEnseignementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AffectationEnseignementService {

    @Autowired
    private AffectationEnseignementRepository affectationEnseignementRepository;

    public List<AffectationEnseignement> findAll() {
        return affectationEnseignementRepository.findAll();
    }

    public Optional<AffectationEnseignement> findById(Long id) {
        return affectationEnseignementRepository.findById(id);
    }

    public AffectationEnseignement save(AffectationEnseignement affectationEnseignement) {
        return affectationEnseignementRepository.save(affectationEnseignement);
    }

    public void deleteById(Long id) {
        affectationEnseignementRepository.deleteById(id);
    }

    public List<AffectationEnseignement> findByProfesseurId(Long professeurId) {
        return affectationEnseignementRepository.findByProfesseurId(professeurId);
    }

    public List<AffectationEnseignement> findByClasseId(Long classeId) {
        return affectationEnseignementRepository.findByClasse_Id(classeId);
    }
}
