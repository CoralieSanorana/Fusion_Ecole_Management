package com.ecole.service;

import com.ecole.entity.Note;
import com.ecole.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecole.entity.AffectationEnseignement;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CoefficientService coefficientService;

    @Autowired
    private MatiereService matiereService;

    @Autowired
    private AffectationEnseignementService affectationEnseignementService;

    public List<Note> findAll() {
        return noteRepository.findAll();
    }

    public Optional<Note> findById(Long id) {
        return noteRepository.findById(id);
    }

    public Note save(Note note) {
        return noteRepository.save(note);
    }

    public void deleteById(Long id) {
        noteRepository.deleteById(id);
    }

    public List<Note> findByEtudiantId(Long etudiantId) {
        return noteRepository.findByEtudiantId(etudiantId);
    }

    public List<Note> findByEtudiantIdByMatiereId(Long etudiantId, Long matiereId) {
        return noteRepository.findByEtudiantIdByMatiereId(etudiantId, matiereId);
    }

    public List<Note> findByEtudiantIdByMatiereIdByPeriodeId(Long etudiantId, Long matiereId, Long periodeId) {
        return noteRepository.findByEtudiantIdByMatiereIdByPeriodeId(etudiantId, matiereId, periodeId);
    }

    public List<Note> findByEtudiantIdByPeriodeId(Long etudiantId, Long periodeId) {
        return noteRepository.findByEtudiantIdByPeriodeId(etudiantId, periodeId);
    }

    public Map<String, Object> getBulletinEtudiant(Long etudiantId, Long periodeId, Long classeId) {
        Map<String, Object> bulletin = new HashMap<>();
        
        // Get all notes for the student in the period
        List<Note> notes = findByEtudiantIdByPeriodeId(etudiantId, periodeId);
        
        // Get all affectations for the class to determine subjects
        List<AffectationEnseignement> affectations = affectationEnseignementService.findByClasseId(classeId);
        
        // Group notes by subject
        Map<Long, List<Note>> notesByMatiere = new HashMap<>();
        for (Note note : notes) {
            // Get the affectation to find the matiereId
            AffectationEnseignement affectation = note.getAffectation();
            if (affectation != null) {
                Long matiereId = affectation.getMatiereId();
                notesByMatiere.computeIfAbsent(matiereId, k -> new ArrayList<>()).add(note);
            }
        }
        
        // Calculate subject averages with coefficients
        List<Map<String, Object>> matieresData = new ArrayList<>();
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal totalCoefficients = BigDecimal.ZERO;
        
        for (AffectationEnseignement affectation : affectations) {
            Long matiereId = affectation.getMatiereId();
            List<Note> matiereNotes = notesByMatiere.getOrDefault(matiereId, Collections.emptyList());
            
            if (!matiereNotes.isEmpty()) {
                // Calculate average for this subject
                BigDecimal sum = BigDecimal.ZERO;
                for (Note note : matiereNotes) {
                    if (note.getValeur() != null && note.getSur() != null && note.getSur().compareTo(BigDecimal.ZERO) > 0) {
                        // Normalize to /20
                        BigDecimal normalized = note.getValeur().multiply(BigDecimal.valueOf(20)).divide(note.getSur(), 2, RoundingMode.HALF_UP);
                        sum = sum.add(normalized);
                    }
                }
                BigDecimal moyenne = sum.divide(BigDecimal.valueOf(matiereNotes.size()), 2, RoundingMode.HALF_UP);
                
                // Get coefficient for this subject and class level
                // Assuming we have a way to get niveauId from classeId
                // For now, we'll use a default coefficient of 1
                BigDecimal coefficient = BigDecimal.ONE;
                // TODO: Get actual coefficient from CoefficientService based on matiereId and niveauId
                
                // Add to weighted total
                totalPoints = totalPoints.add(moyenne.multiply(coefficient));
                totalCoefficients = totalCoefficients.add(coefficient);
                
                Map<String, Object> matiereData = new HashMap<>();
                matiereData.put("matiereId", matiereId);
                matiereData.put("matiereNom", matiereService.findById(matiereId).map(m -> m.getNom()).orElse("Matière inconnue"));
                matiereData.put("moyenne", moyenne);
                matiereData.put("coefficient", coefficient);
                matiereData.put("notes", matiereNotes);
                matiereData.put("appreciation", getAppreciation(moyenne));
                
                matieresData.add(matiereData);
            }
        }
        
        // Calculate general average
        BigDecimal moyenneGenerale = totalCoefficients.compareTo(BigDecimal.ZERO) > 0 
            ? totalPoints.divide(totalCoefficients, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        bulletin.put("matieres", matieresData);
        bulletin.put("moyenneGenerale", moyenneGenerale);
        bulletin.put("appreciationGenerale", getAppreciationGenerale(moyenneGenerale));
        
        return bulletin;
    }

    private String getAppreciation(BigDecimal moyenne) {
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) {
            return "Très bien";
        } else if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) {
            return "Bien";
        } else if (moyenne.compareTo(BigDecimal.valueOf(12)) >= 0) {
            return "Assez bien";
        } else if (moyenne.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "Passable";
        } else {
            return "Insuffisant";
        }
    }

    private String getAppreciationGenerale(BigDecimal moyenne) {
        if (moyenne.compareTo(BigDecimal.valueOf(16)) >= 0) {
            return "Excellent travail, félicitations !";
        } else if (moyenne.compareTo(BigDecimal.valueOf(14)) >= 0) {
            return "Très bon travail, continuez ainsi !";
        } else if (moyenne.compareTo(BigDecimal.valueOf(12)) >= 0) {
            return "Bon travail, mais peut mieux faire.";
        } else if (moyenne.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "Travail satisfaisant, des efforts sont nécessaires.";
        } else {
            return "Travail insuffisant, un sérieux effort est requis.";
        }
    }
}

