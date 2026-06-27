package com.ecole.service;

import com.ecole.entity.Moyenne;
import com.ecole.repository.MoyenneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MoyenneService {

    @Autowired
    private MoyenneRepository moyenneRepository;

    public List<Moyenne> findAll() {
        return moyenneRepository.findAll();
    }

    public Optional<Moyenne> findById(Long id) {
        return moyenneRepository.findById(id);
    }

    public Moyenne save(Moyenne moyenne) {
        return moyenneRepository.save(moyenne);
    }

    public void deleteById(Long id) {
        moyenneRepository.deleteById(id);
    }
}
