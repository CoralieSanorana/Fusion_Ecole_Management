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
    private final CoefficientRepository coefficientRepository;
    private final ProfilsProfesseursRepository profilsProfesseursRepository;
    private final AffectationEnseignementRepository affectationEnseignementRepository;

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
        // Vérifier qu'il existe au moins un établissement
        if (etablissementRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer un établissement avant de créer une année scolaire.");
        }
        // Vérifier que l'établissement existe
        if (annee.getEtablissement() != null && !etablissementRepository.existsById(annee.getEtablissement().getId())) {
            throw new IllegalArgumentException("L'établissement spécifié n'existe pas.");
        }
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
        // Vérifier qu'il existe au moins un établissement
        if (etablissementRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer un établissement avant de créer des niveaux.");
        }
        // Vérifier que l'établissement existe
        if (niveau.getEtablissement() != null && !etablissementRepository.existsById(niveau.getEtablissement().getId())) {
            throw new IllegalArgumentException("L'établissement spécifié n'existe pas.");
        }
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
        // Vérifier qu'il existe au moins un établissement
        if (etablissementRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer un établissement avant de créer des salles.");
        }
        // Vérifier que l'établissement existe
        if (salle.getEtablissement() != null && !etablissementRepository.existsById(salle.getEtablissement().getId())) {
            throw new IllegalArgumentException("L'établissement spécifié n'existe pas.");
        }
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
        // Vérifier qu'il existe au moins une année scolaire active
        Optional<AnneeScolaire> anneeActive = anneeScolaireRepository.findByEstActiveTrue();
        if (anneeActive.isEmpty()) {
            throw new IllegalStateException("Vous devez d'abord créer et activer une année scolaire avant de créer des classes.");
        }
        // Vérifier qu'il existe au moins un niveau
        if (niveauRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des niveaux avant de créer des classes.");
        }
        // Vérifier qu'il existe au moins une salle active
        if (salleRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des salles avant de créer des classes.");
        }
        // Vérifier que le niveau existe
        if (classe.getNiveau() != null && !niveauRepository.existsById(classe.getNiveau().getId())) {
            throw new IllegalArgumentException("Le niveau spécifié n'existe pas.");
        }
        // Vérifier que l'année scolaire existe
        if (classe.getAnneeScolaire() != null && !anneeScolaireRepository.existsById(classe.getAnneeScolaire().getId())) {
            throw new IllegalArgumentException("L'année scolaire spécifiée n'existe pas.");
        }
        // Vérifier que la salle existe
        if (classe.getSalle() != null && !salleRepository.existsById(classe.getSalle().getId())) {
            throw new IllegalArgumentException("La salle spécifiée n'existe pas.");
        }
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
        // Vérifier qu'il existe au moins un établissement
        if (etablissementRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer un établissement avant de créer des matières.");
        }
        // Vérifier que l'établissement existe
        if (matiere.getEtablissement() != null && !etablissementRepository.existsById(matiere.getEtablissement().getId())) {
            throw new IllegalArgumentException("L'établissement spécifié n'existe pas.");
        }
        if (matiere.getCreatedAt() == null) {
            matiere.setCreatedAt(LocalDateTime.now());
        }
        return matiereRepository.save(matiere);
    }

    // --- Coefficients ---
    public List<Coefficient> getAllCoefficients() {
        return coefficientRepository.findAll();
    }

    @Transactional
    public Coefficient saveCoefficient(Coefficient coefficient) {
        // Vérifier qu'il existe au moins une matière
        if (matiereRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des matières avant de définir des coefficients.");
        }
        // Vérifier qu'il existe au moins un niveau
        if (niveauRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des niveaux avant de définir des coefficients.");
        }
        // Vérifier que la matière existe
        if (coefficient.getMatiereId() != null && !matiereRepository.existsById(coefficient.getMatiereId())) {
            throw new IllegalArgumentException("La matière spécifiée n'existe pas.");
        }
        // Vérifier que le niveau existe
        if (coefficient.getNiveauId() != null && !niveauRepository.existsById(coefficient.getNiveauId())) {
            throw new IllegalArgumentException("Le niveau spécifié n'existe pas.");
        }
        return coefficientRepository.save(coefficient);
    }

    @Transactional
    public void deleteCoefficient(Long id) {
        coefficientRepository.deleteById(id);
    }

    // --- Affectations ---
    public List<AffectationEnseignement> getAllAffectations() {
        return affectationEnseignementRepository.findAll();
    }

    @Transactional
    public AffectationEnseignement saveAffectation(AffectationEnseignement affectation) {
        // Vérifier qu'il existe au moins un professeur
        if (profilsProfesseursRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des professeurs avant de créer des affectations.");
        }
        // Vérifier qu'il existe au moins une matière
        if (matiereRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des matières avant de créer des affectations.");
        }
        // Vérifier qu'il existe au moins une classe
        if (classeRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer des classes avant de créer des affectations.");
        }
        // Vérifier qu'il existe au moins une année scolaire
        if (anneeScolaireRepository.count() == 0) {
            throw new IllegalStateException("Vous devez d'abord créer une année scolaire avant de créer des affectations.");
        }
        // Vérifier que le professeur existe
        if (affectation.getProfesseur() != null && !profilsProfesseursRepository.existsById(affectation.getProfesseur().getId())) {
            throw new IllegalArgumentException("Le professeur spécifié n'existe pas.");
        }
        // Vérifier que la matière existe
        if (affectation.getMatiere() != null && !matiereRepository.existsById(affectation.getMatiere().getId())) {
            throw new IllegalArgumentException("La matière spécifiée n'existe pas.");
        }
        // Vérifier que la classe existe
        if (affectation.getClasse() != null && !classeRepository.existsById(affectation.getClasse().getId())) {
            throw new IllegalArgumentException("La classe spécifiée n'existe pas.");
        }
        // Vérifier que l'année scolaire existe
        if (affectation.getAnneeScolaire() != null && !anneeScolaireRepository.existsById(affectation.getAnneeScolaire().getId())) {
            throw new IllegalArgumentException("L'année scolaire spécifiée n'existe pas.");
        }
        return affectationEnseignementRepository.save(affectation);
    }

    @Transactional
    public void deleteAffectation(Long id) {
        affectationEnseignementRepository.deleteById(id);
    }

    @Transactional
    public void deleteMatiere(Long id) {
        matiereRepository.deleteById(id);
    }
}
