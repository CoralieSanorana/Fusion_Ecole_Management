package com.ecole.service;

import com.ecole.entity.Niveau;
import com.ecole.repository.NiveauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NiveauService {

    @Autowired
    private NiveauRepository niveauRepository;

    public List<Niveau> findAll() {
        return niveauRepository.findAll();
    }

    public Optional<Niveau> findById(Long id) {
        return niveauRepository.findById(id);
    }

    public Niveau save(Niveau niveau) {
        return niveauRepository.save(niveau);
    }

    public void deleteById(Long id) {
        niveauRepository.deleteById(id);
    }
}
