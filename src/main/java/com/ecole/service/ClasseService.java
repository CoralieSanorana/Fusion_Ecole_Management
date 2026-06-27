package com.ecole.service;

import com.ecole.entity.Classe;
import com.ecole.repository.ClasseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClasseService {

    @Autowired
    private ClasseRepository classeRepository;

    public List<Classe> findAll() {
        return classeRepository.findAll();
    }

    public Optional<Classe> findById(Long id) {
        return classeRepository.findById(id);
    }

    public Classe save(Classe classe) {
        return classeRepository.save(classe);
    }

    public void deleteById(Long id) {
        classeRepository.deleteById(id);
    }
}
