package com.ecole.service;

import com.ecole.entity.Coefficient;
import com.ecole.repository.CoefficientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
}
