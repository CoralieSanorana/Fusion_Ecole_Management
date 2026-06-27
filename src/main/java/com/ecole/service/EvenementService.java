package com.ecole.service;

import com.ecole.entity.Evenement;
import com.ecole.repository.EvenementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    public List<Evenement> findAll() {
        return evenementRepository.findAll();
    }

    public Optional<Evenement> findById(Long id) {
        return evenementRepository.findById(id);
    }

    public Evenement save(Evenement evenement) {
        return evenementRepository.save(evenement);
    }

    public void deleteById(Long id) {
        evenementRepository.deleteById(id);
    }
}
