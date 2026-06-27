package com.ecole.service;

import com.ecole.entity.*;
import com.ecole.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InitializeService {

    private final EtablissementRepository etablissementRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final NiveauRepository niveauRepository;
    private final SalleRepository salleRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;

    // --- Etablissement ---
    public List<Etablissement> getAllEtablissements() {
        return etablissementRepository.findAll();
    }

    public Optional<Etablissement> getEtablissementById(Long id) {
        return etablissementRepository.findById(id);
    }

    @Transactional
    public Etablissement saveEtablissement(Etablissement etablissement) {
        if (etablissement.getCreatedAt() == null) {
            etablissement.setCreatedAt(LocalDateTime.now());
        }
        return etablissementRepository.save(etablissement);
    }

    @Transactional
    public void deleteEtablissement(Long id) {
        etablissementRepository.deleteById(id);
    }

    // --- Annee Scolaire ---
    public List<AnneeScolaire> getAllAnneesScolaires() {
        return anneeScolaireRepository.findAll();
    }

    public Optional<AnneeScolaire> getAnneeScolaireById(Long id) {
        return anneeScolaireRepository.findById(id);
    }

    public Optional<AnneeScolaire> getAnneeActive() {
        return anneeScolaireRepository.findByEstActiveTrue();
    }

    @Transactional
    public AnneeScolaire saveAnneeScolaire(AnneeScolaire annee) {
        // Désactiver l'ancienne année active si la nouvelle est active
        if (Boolean.TRUE.equals(annee.getEstActive())) {
            anneeScolaireRepository.findAll().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getEstActive()) && !a.getId().equals(annee.getId()))
                    .forEach(a -> {
                        a.setEstActive(false);
                        anneeScolaireRepository.save(a);
                    });
        }
        if (annee.getCreatedAt() == null) {
            annee.setCreatedAt(LocalDateTime.now());
        }
        return anneeScolaireRepository.save(annee);
    }

    @Transactional
    public void deleteAnneeScolaire(Long id) {
        anneeScolaireRepository.deleteById(id);
    }

    // --- Niveaux ---
    public List<Niveau> getAllNiveaux() {
        return niveauRepository.findAll();
    }

    public Optional<Niveau> getNiveauById(Long id) {
        return niveauRepository.findById(id);
    }

    @Transactional
    public Niveau saveNiveau(Niveau niveau) {
        if (niveau.getCreatedAt() == null) {
            niveau.setCreatedAt(LocalDateTime.now());
        }
        return niveauRepository.save(niveau);
    }

    @Transactional
    public void deleteNiveau(Long id) {
        niveauRepository.deleteById(id);
    }

    // --- Salles ---
    public List<Salle> getAllSalles() {
        return salleRepository.findAll();
    }

    public Optional<Salle> getSalleById(Long id) {
        return salleRepository.findById(id);
    }

    @Transactional
    public Salle saveSalle(Salle salle) {
        if (salle.getCreatedAt() == null) {
            salle.setCreatedAt(LocalDateTime.now());
        }
        return salleRepository.save(salle);
    }

    @Transactional
    public void deleteSalle(Long id) {
        salleRepository.deleteById(id);
    }

    // --- Classes ---
    public List<Classe> getAllClasses() {
        return classeRepository.findAll();
    }

    public Optional<Classe> getClasseById(Long id) {
        return classeRepository.findById(id);
    }

    @Transactional
    public Classe saveClasse(Classe classe) {
        if (classe.getCreatedAt() == null) {
            classe.setCreatedAt(LocalDateTime.now());
        }
        return classeRepository.save(classe);
    }

    @Transactional
    public void deleteClasse(Long id) {
        classeRepository.deleteById(id);
    }

    // --- Matieres ---
    public List<Matiere> getAllMatieres() {
        return matiereRepository.findAll();
    }

    public Optional<Matiere> getMatiereById(Long id) {
        return matiereRepository.findById(id);
    }

    @Transactional
    public Matiere saveMatiere(Matiere matiere) {
        if (matiere.getCreatedAt() == null) {
            matiere.setCreatedAt(LocalDateTime.now());
        }
        return matiereRepository.save(matiere);
    }

    @Transactional
    public void deleteMatiere(Long id) {
        matiereRepository.deleteById(id);
    }
}
