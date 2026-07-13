package com.ecole.service;

import com.ecole.entity.*;
import com.ecole.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EcolageMensuelService {

    @Autowired private EcolageMensuelRepository ecolageMensuelRepository;
    @Autowired private NiveauRepository niveauRepository;
    @Autowired private AnneeScolaireRepository anneeScolaireRepository;

    public List<EcolageMensuel> getTarifsAnneeActive() {
        AnneeScolaire annee = anneeScolaireRepository.findByEstActiveTrue()
            .orElseThrow(() -> new RuntimeException("Aucune année scolaire active"));
        return ecolageMensuelRepository.findByAnneeScolaire(annee);
    }

    public EcolageMensuel getTarifParNiveau(Long niveauId) {
        AnneeScolaire annee = anneeScolaireRepository.findByEstActiveTrue()
            .orElseThrow(() -> new RuntimeException("Aucune année scolaire active"));
        Niveau niveau = niveauRepository.findById(niveauId)
            .orElseThrow(() -> new RuntimeException("Niveau introuvable"));
        return ecolageMensuelRepository.findByNiveauAndAnneeScolaire(niveau, annee)
            .orElse(null);
    }

    @Transactional
    public EcolageMensuel configurer(Long niveauId, BigDecimal montant) {
        AnneeScolaire annee = anneeScolaireRepository.findByEstActiveTrue()
            .orElseThrow(() -> new RuntimeException("Aucune année scolaire active"));
        Niveau niveau = niveauRepository.findById(niveauId)
            .orElseThrow(() -> new RuntimeException("Niveau introuvable"));

        EcolageMensuel ecolage = ecolageMensuelRepository
            .findByNiveauAndAnneeScolaire(niveau, annee)
            .orElse(new EcolageMensuel());

        ecolage.setNiveau(niveau);
        ecolage.setAnneeScolaire(annee);
        ecolage.setMontant(montant);
        if (ecolage.getCreatedAt() == null) {
            ecolage.setCreatedAt(LocalDateTime.now());
        }
        return ecolageMensuelRepository.save(ecolage);
    }

    public List<Niveau> getAllNiveaux() {
        return niveauRepository.findAll();
    }
}