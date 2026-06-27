package com.ecole.service;

import com.ecole.entity.ProfilsProfesseurs;
import com.ecole.repository.ProfilsProfesseursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfilsProfesseursService {

    @Autowired
    private ProfilsProfesseursRepository ProfilsProfesseursRepository;

    public List<ProfilsProfesseurs> findAll() {
        return ProfilsProfesseursRepository.findAll();
    }

    public Optional<ProfilsProfesseurs> findById(Long id) {
        return ProfilsProfesseursRepository.findById(id);
    }

    public ProfilsProfesseurs save(ProfilsProfesseurs ProfilsProfesseurs) {
        return ProfilsProfesseursRepository.save(ProfilsProfesseurs);
    }

    public void deleteById(Long id) {
        ProfilsProfesseursRepository.deleteById(id);
    }
}
