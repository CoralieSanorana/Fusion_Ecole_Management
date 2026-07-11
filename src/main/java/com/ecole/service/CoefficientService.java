package com.ecole.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecole.entity.Coefficient;
import com.ecole.repository.CoefficientRepository;

@Service
public class CoefficientService {

    @Autowired
    private CoefficientRepository coefficientRepository;

    public List<Coefficient> findAll() {
        return coefficientRepository.findAll();
    }

    public Optional<Coefficient> findById(Long id) {
        return coefficientRepository.findById(id);
    }

    public Coefficient save(Coefficient coefficient) {
        return coefficientRepository.save(coefficient);
    }

    public void deleteById(Long id) {
        coefficientRepository.deleteById(id);
    }

    public Optional<Coefficient> findByMatiereIdAndNiveauId(Long matiereId, Long niveauId) {
        if (matiereId == null || niveauId == null) {
            return Optional.empty();
        }
        return coefficientRepository.findCoefficientsParMatiere(matiereId, niveauId)
                .stream()
                .findFirst();
    }

    public BigDecimal getCoefficientForMatiereAndNiveau(Long matiereId, Long niveauId) {
        return findByMatiereIdAndNiveauId(matiereId, niveauId)
                .map(Coefficient::getValeur)
                .orElse(BigDecimal.ONE);
    }

    public Map<Long, BigDecimal> findCoefficientsMapByNiveau(Long niveauId) {
        if (niveauId == null) {
            return Map.of();
        }
        return coefficientRepository.findCoefficientsMapByNiveau(niveauId);
    }
}
