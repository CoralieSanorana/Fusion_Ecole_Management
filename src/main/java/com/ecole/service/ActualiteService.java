package com.ecole.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecole.entity.Actualite;
import com.ecole.repository.ActualiteRepository;

@Service
public class ActualiteService {

    @Autowired
    private ActualiteRepository actualiteRepository;

    public List<Actualite> findAll() {
        return actualiteRepository.findAll();
    }

    public List<Actualite> findAllActive() {
        return actualiteRepository.findAllActive();
    }

    public List<Actualite> findByCategorie(String categorie) {
        return actualiteRepository.findByCategorie(categorie);
    }

    public List<Actualite> findTop10Active() {
        return actualiteRepository.findTop10ByEstActiveTrueOrderByDatePublicationDesc();
    }

    public long countActive() {
        return actualiteRepository.countByEstActiveTrue();
    }

    public Optional<Actualite> findById(Long id) {
        return actualiteRepository.findById(id);
    }

    public Actualite save(Actualite actualite) {
        if (actualite.getDatePublication() == null) {
            actualite.setDatePublication(LocalDateTime.now());
        }
        if (actualite.getEstActive() == null) {
            actualite.setEstActive(true);
        }
        return actualiteRepository.save(actualite);
    }

    public void deleteById(Long id) {
        actualiteRepository.deleteById(id);
    }

    public void deactivate(Long id) {
        Optional<Actualite> actualite = actualiteRepository.findById(id);
        if (actualite.isPresent()) {
            actualite.get().setEstActive(false);
            actualiteRepository.save(actualite.get());
        }
    }
}
