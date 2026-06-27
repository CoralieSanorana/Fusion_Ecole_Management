package com.ecole.service;

import com.ecole.entity.Matiere;
import com.ecole.repository.MatiereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatiereService {

    @Autowired
    private MatiereRepository matiereRepository;

    public List<Matiere> findAll() {
        return matiereRepository.findAll();
    }

    public Optional<Matiere> findById(Long id) {
        return matiereRepository.findById(id);
    }

    public Matiere save(Matiere matiere) {
        return matiereRepository.save(matiere);
    }

    public void deleteById(Long id) {
        matiereRepository.deleteById(id);
    }
}
