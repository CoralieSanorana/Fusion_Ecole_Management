package com.ecole.service;

import com.ecole.entity.ProfilEtudiant;
import com.ecole.repository.ProfilEtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfilEtudiantService {

    @Autowired
    private ProfilEtudiantRepository profilEtudiantRepository;

    public List<ProfilEtudiant> findAll() {
        return profilEtudiantRepository.findAll();
    }

    public Optional<ProfilEtudiant> findById(Long id) {
        return profilEtudiantRepository.findById(id);
    }

    public ProfilEtudiant save(ProfilEtudiant profilEtudiant) {
        return profilEtudiantRepository.save(profilEtudiant);
    }

    public void deleteById(Long id) {
        profilEtudiantRepository.deleteById(id);
    }
}
