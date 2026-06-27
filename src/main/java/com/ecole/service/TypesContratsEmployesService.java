package com.ecole.service;

import com.ecole.entity.TypesContratsEmployes;
import com.ecole.repository.TypesContratsEmployesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypesContratsEmployesService {

    @Autowired
    private TypesContratsEmployesRepository typesContratsEmployesRepository;

    public List<TypesContratsEmployes> findAll() {
        return typesContratsEmployesRepository.findAll();
    }

    public Optional<TypesContratsEmployes> findById(Long id) {
        return typesContratsEmployesRepository.findById(id);
    }

    public TypesContratsEmployes save(TypesContratsEmployes typesContratsEmployes) {
        return typesContratsEmployesRepository.save(typesContratsEmployes);
    }

    public void deleteById(Long id) {
        typesContratsEmployesRepository.deleteById(id);
    }
}
