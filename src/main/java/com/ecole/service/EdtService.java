package com.ecole.service;

import com.ecole.dto.Directeur.*;
import com.ecole.entity.*;
import com.ecole.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EdtService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final NiveauRepository niveauRepository;
    private final SalleRepository salleRepository;
    private final HoraireEdtRepository horaireEdtRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final AffectationEnseignementRepository AffectationEnseignementRepository;

    public AnneeScolaire getAnneeActive() {
        return anneeScolaireRepository.findByEstActiveTrue().orElse(null);
    }

    public List<Niveau> getAllNiveaux() {
        return niveauRepository.findAll();
    }

    public List<Salle> getActiveSalles() {
        return salleRepository.findAll().stream().filter(Salle::getIsActive).toList();
    }

    public List<HoraireEdt> getHoraires(Long niveauId) {
        if (niveauId != null) {
            List<HoraireEdt> niveauHoraires = horaireEdtRepository.findByNiveauIdOrderByOrdre(niveauId);
            if (!niveauHoraires.isEmpty()) {
                return niveauHoraires;
            }
        }
        return horaireEdtRepository.findByNiveauIsNullOrderByOrdre();
    }

    public Map<Long, Map<Integer, CreneauDTO>> getCreneauxParSalle(Long anneeId, Long salleId, List<HoraireEdt> horaires) {
        Map<Long, Map<Integer, CreneauDTO>> result = new HashMap<>();
        
        List<EmploiDuTemps> creneaux = emploiDuTempsRepository.findBySalleIdAndAnneeScolaireId(salleId, anneeId);
        
        for (HoraireEdt horaire : horaires) {
            result.put(horaire.getId(), new HashMap<>());
        }
        
        for (EmploiDuTemps creneau : creneaux) {
            if (creneau.getHoraireEdt() != null && creneau.getJourSemaine() != null) {
                CreneauDTO dto = new CreneauDTO();
                dto.setId(creneau.getId());
                dto.setJourSemaine(creneau.getJourSemaine());
                dto.setHoraireEdtId(creneau.getHoraireEdt().getId());
                
                if (creneau.getAffectation() != null) {
                    AffectationEnseignement aff = creneau.getAffectation();
                    if (aff.getMatiere() != null) {
                        dto.setMatiereNom(aff.getMatiere().getNom());
                        dto.setMatiereCode(aff.getMatiere().getCode());
                    }
                    if (aff.getClasse() != null) {
                        dto.setClasseNom(aff.getClasse().getNom());
                    }
                    if (aff.getProfesseur() != null) {
                        dto.setProfNom(aff.getProfesseur().getNom());
                        dto.setProfPrenom(aff.getProfesseur().getPrenom());
                    }
                }
                
                if (creneau.getSalle() != null) {
                    dto.setSalleNom(creneau.getSalle().getNom());
                }
                
                Map<Integer, CreneauDTO> horaireMap = result.get(creneau.getHoraireEdt().getId());
                if (horaireMap != null) {
                    horaireMap.put(creneau.getJourSemaine(), dto);
                }
            }
        }
        
        return result;
    }

    public Integer countCreneaux(Long anneeId, Long salleId) {
        return emploiDuTempsRepository.findBySalleIdAndAnneeScolaireId(salleId, anneeId).size();
    }

    public List<AffectationDetailDTO> getAffectationsWithDetails(Long anneeId) {
        List<AffectationEnseignement> affectations = AffectationEnseignementRepository.findByAnneeScolaireId(anneeId);
        List<AffectationDetailDTO> result = new ArrayList<>();
        
        for (AffectationEnseignement aff : affectations) {
            AffectationDetailDTO dto = new AffectationDetailDTO();
            dto.setId(aff.getId());
            
            if (aff.getClasse() != null && aff.getClasse().getNiveau() != null) {
                dto.setNiveauLibelle(aff.getClasse().getNiveau().getLibelle());
            }
            if (aff.getClasse() != null) {
                dto.setClasseNom(aff.getClasse().getNom());
            }
            if (aff.getMatiere() != null) {
                dto.setMatiereNom(aff.getMatiere().getNom());
                dto.setMatiereCode(aff.getMatiere().getCode());
            }
            if (aff.getProfesseur() != null) {
                dto.setProfNom(aff.getProfesseur().getNom());
                dto.setProfPrenom(aff.getProfesseur().getPrenom());
            }
            
            result.add(dto);
        }
        
        return result;
    }

    public void saveCreneaux(Long salleId, Long affectationId, List<String> cells) {
        Optional<Salle> salleOpt = salleRepository.findById(salleId);
        Optional<AffectationEnseignement> affOpt = AffectationEnseignementRepository.findById(affectationId);
        
        if (salleOpt.isPresent() && affOpt.isPresent()) {
            Salle salle = salleOpt.get();
            AffectationEnseignement affectation = affOpt.get();
            
            for (String cell : cells) {
                String[] parts = cell.split("\\|");
                if (parts.length == 2) {
                    Integer jourSemaine = Integer.parseInt(parts[0]);
                    Long horaireId = Long.parseLong(parts[1]);
                    
                    Optional<HoraireEdt> horaireOpt = horaireEdtRepository.findById(horaireId);
                    if (horaireOpt.isPresent()) {
                        HoraireEdt horaire = horaireOpt.get();
                        
                        EmploiDuTemps edt = new EmploiDuTemps();
                        edt.setSalle(salle);
                        edt.setAffectation(affectation);
                        edt.setHoraireEdt(horaire);
                        edt.setJourSemaine(jourSemaine);
                        edt.setHeureDebut(horaire.getHeureDebut());
                        edt.setHeureFin(horaire.getHeureFin());
                        edt.setCreatedAt(java.time.LocalDateTime.now());
                        
                        emploiDuTempsRepository.save(edt);
                    }
                }
            }
        }
    }

    public void saveHoraires(Long niveauId, List<com.ecole.dto.Directeur.HoraireEdtForm> horaires, Boolean resetToGlobal) {
        if (resetToGlobal) {
            horaireEdtRepository.deleteByNiveauId(niveauId);
        } else if (horaires != null && !horaires.isEmpty()) {
            horaireEdtRepository.deleteByNiveauId(niveauId);
            Niveau niveau = niveauRepository.findById(niveauId).orElse(null);
            for (com.ecole.dto.Directeur.HoraireEdtForm h : horaires) {
                HoraireEdt horaire = new HoraireEdt();
                horaire.setLibelle(h.getLibelle());
                horaire.setHeureDebut(h.getHeureDebut());
                horaire.setHeureFin(h.getHeureFin());
                horaire.setOrdre(h.getOrdre());
                horaire.setIsActive(true);
                horaire.setNiveau(niveau);
                horaireEdtRepository.save(horaire);
            }
        }
    }

    public void saveHorairesFromRequest(Long niveauId, jakarta.servlet.http.HttpServletRequest request, Boolean resetToGlobal) {
        Niveau niveau = niveauRepository.findById(niveauId).orElse(null);
        if (niveau == null) {
            return;
        }

        if (resetToGlobal) {
            // Only delete if not used
            List<HoraireEdt> existingHoraires = horaireEdtRepository.findByNiveauIdOrderByOrdre(niveauId);
            for (HoraireEdt h : existingHoraires) {
                if (!emploiDuTempsRepository.existsByHoraireEdt_Id(h.getId())) {
                    horaireEdtRepository.delete(h);
                }
            }
            return;
        }

        // Get current horaires for this niveau
        List<HoraireEdt> existingHoraires = horaireEdtRepository.findByNiveauIdOrderByOrdre(niveauId);
        // We'll collect the IDs that are kept
        java.util.Set<Long> keptIds = new java.util.HashSet<>();

        int index = 0;
        while (true) {
            String libelle = request.getParameter("horaires[" + index + "][libelle]");
            String heureDebutStr = request.getParameter("horaires[" + index + "][heureDebut]");
            String heureFinStr = request.getParameter("horaires[" + index + "][heureFin]");
            String ordreStr = request.getParameter("horaires[" + index + "][ordre]");
            String idStr = request.getParameter("horaires[" + index + "][id]");
            
            if (libelle == null && heureDebutStr == null && heureFinStr == null && ordreStr == null) {
                break;
            }
            
            if (libelle != null && heureDebutStr != null && heureFinStr != null && ordreStr != null) {
                HoraireEdt horaire;
                if (idStr != null && !idStr.equals("new")) {
                    // Existing horaire
                    Long id = Long.parseLong(idStr);
                    horaire = horaireEdtRepository.findById(id).orElse(null);
                    if (horaire == null) {
                        horaire = new HoraireEdt();
                    } else {
                        keptIds.add(id);
                    }
                } else {
                    horaire = new HoraireEdt();
                }

                horaire.setLibelle(libelle);
                horaire.setHeureDebut(LocalTime.parse(heureDebutStr));
                horaire.setHeureFin(LocalTime.parse(heureFinStr));
                horaire.setOrdre(Integer.parseInt(ordreStr));
                horaire.setIsActive(true);
                horaire.setNiveau(niveau);
                HoraireEdt saved = horaireEdtRepository.save(horaire);
                keptIds.add(saved.getId());
            }
            index++;
        }

        // Now delete existing horaires that aren't in keptIds and aren't used
        for (HoraireEdt h : existingHoraires) {
            if (!keptIds.contains(h.getId()) && !emploiDuTempsRepository.existsByHoraireEdt_Id(h.getId())) {
                horaireEdtRepository.delete(h);
            }
        }
    }
}
