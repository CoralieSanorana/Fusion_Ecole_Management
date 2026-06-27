package com.ecole.service;

import com.ecole.entity.EmploiDuTemps;
import com.ecole.repository.EmploiDuTempsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecole.entity.AffectationEnseignement;

import java.util.List;
import java.util.Optional;

@Service
public class EmploiDuTempsService {

    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;

    @Autowired
    private AffectationEnseignementService affectationEnseignementService;

    public List<EmploiDuTemps> findAll() {
        return emploiDuTempsRepository.findAll();
    }

    public Optional<EmploiDuTemps> findById(Long id) {
        return emploiDuTempsRepository.findById(id);
    }

    public EmploiDuTemps save(EmploiDuTemps emploiDuTemps) {
        return emploiDuTempsRepository.save(emploiDuTemps);
    }

    public void deleteById(Long id) {
        emploiDuTempsRepository.deleteById(id);
    }

    public List<EmploiDuTemps> findByAffectationId(Long affectationId) {
        return emploiDuTempsRepository.findByAffectation_Id(affectationId);
    }

    public List<EmploiDuTemps> getCalendarProf(Long professeurId) {
        List<AffectationEnseignement> affectations = affectationEnseignementService.findByProfesseurId(professeurId);
        List<EmploiDuTemps> emploiDuTempsList = new java.util.ArrayList<>();
        for (AffectationEnseignement affectation : affectations) {
            emploiDuTempsList.addAll(findByAffectationId(affectation.getId()));
        }
        return emploiDuTempsList;
    }
}
