package com.ecole.service;

import com.ecole.entity.TypeFichier;
import com.ecole.repository.TypeFichierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeFichierService {

    @Autowired
    private TypeFichierRepository typeFichierRepository;

    public List<TypeFichier> findAll() {
        return typeFichierRepository.findAll();
    }

    public Optional<TypeFichier> findById(Long id) {
        return typeFichierRepository.findById(id);
    }

    public TypeFichier save(TypeFichier typeFichier) {
        return typeFichierRepository.save(typeFichier);
    }

    public void deleteById(Long id) {
        typeFichierRepository.deleteById(id);
    }
}
