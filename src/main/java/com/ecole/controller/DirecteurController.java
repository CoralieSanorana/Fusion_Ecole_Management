package com.ecole.controller;

import com.ecole.entity.*;
import com.ecole.service.EdtService;
import com.ecole.service.DirecteurDashboardService;
import com.ecole.service.InitializeService;
import com.ecole.service.EmployeService;
import com.ecole.service.EtudiantFilterService;
import com.ecole.service.StatistiquesElevesService;
import com.ecole.service.ProfilsProfesseursService;
import com.ecole.service.CoefficientService;
import com.ecole.service.AffectationEnseignementService;
import com.ecole.dto.Directeur.*;
import com.ecole.dto.ProfesseurDTO;
import com.ecole.dto.CoefficientDTO;
import com.ecole.dto.AffectationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DirecteurController {

    private final EdtService edtService;
    private final InitializeService initializeService;
    private final EmployeService employeService;
    private final DirecteurDashboardService directeurDashboardService;
    private final com.ecole.service.MatiereService matiereService;
    private final com.ecole.service.TypesContratsEmployesService typesContratsEmployesService;
    private final EtudiantFilterService filterService;
    private final StatistiquesElevesService statistiquesElevesService;
    private final ProfilsProfesseursService ProfilsProfesseursService;
    private final CoefficientService coefficientService;
    private final AffectationEnseignementService affectationEnseignementService;


    @GetMapping("/directeur/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("currentRole", "directeur");
        model.addAttribute("dashboardEndpoint", "/api/directeur/dashboard");
        return "directeur/dashboard";
    }

    @GetMapping("/api/directeur/dashboard")
    @ResponseBody
    public ResponseEntity<com.ecole.dto.Directeur.DashboardResponseDTO> getDashboard(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String mois) {
        return ResponseEntity.ok(directeurDashboardService.getDashboard(anneeScolaireId, mois));
    }

    @GetMapping("/api/directeur/dashboard/export/transactions.pdf")
    @ResponseBody
    public ResponseEntity<byte[]> exportDashboardTransactionsPdf(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String mois) throws Exception {
        DashboardResponseDTO dashboard = directeurDashboardService.getDashboard(anneeScolaireId, mois);
        byte[] pdf = directeurDashboardService.exportTransactionsPdf(dashboard);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + buildExportFilename("transactions-dashboard", dashboard))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/directeur/dashboard/export/bilan.pdf")
    @ResponseBody
    public ResponseEntity<byte[]> exportDashboardBilanPdf(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String mois) throws Exception {
        DashboardResponseDTO dashboard = directeurDashboardService.getDashboard(anneeScolaireId, mois);
        byte[] pdf = directeurDashboardService.exportDashboardPdf(dashboard);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + buildExportFilename("bilan-dashboard", dashboard))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/directeur/professeurs")
    public String professeurs(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "matiere", required = false) Long matiereId,
            @RequestParam(name = "salaireMin", required = false) BigDecimal salaireMin,
            @RequestParam(name = "salaireMax", required = false) BigDecimal salaireMax,
            Model model) {
        model.addAttribute("pageTitle", "Corps Professoral");
        model.addAttribute("currentRole", "directeur");
        model.addAttribute("matieres", matiereService.findAll());
        model.addAttribute("typesContrats", typesContratsEmployesService.findAll());
        
        // Treat empty strings as null
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String normalizedRole = (role != null && !role.trim().isEmpty()) ? role : null;
        
        List<VueEmployesDetail> employes;
        if (normalizedKeyword != null || normalizedRole != null || matiereId != null || salaireMin != null || salaireMax != null) {
            employes = employeService.filterEmployes(normalizedKeyword, normalizedRole, matiereId, salaireMin, salaireMax);
        } else {
            employes = employeService.getProfesseursEtSecretaires();
        }
        
        model.addAttribute("employes", employes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("matiereId", matiereId);
        model.addAttribute("salaireMin", salaireMin);
        model.addAttribute("salaireMax", salaireMax);
        
        return "directeur/professeurs";
    }

    @GetMapping("/directeur/employe/contrat/{id}")
    public String employeContrat(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Contrat employé");
        model.addAttribute("currentRole", "directeur");
        
        employeService.getEmployeById(id).ifPresentOrElse(
            employe -> {
                model.addAttribute("employe", employe);
                model.addAttribute("texteRestant", employeService.calculateRemainingDaysText(employe));
                model.addAttribute("photoUrl", employeService.getPhotoUrl(employe));
            },
            () -> model.addAttribute("employe", null)
        );
        
        return "directeur/employe_contrat";
    }

    @PostMapping("/directeur/employe/contrat/{id}/photo")
    public String uploadEmployeePhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String photoUrl = employeService.uploadEmployeePhoto(id, file);
            redirectAttributes.addFlashAttribute("success", "Photo de l'employé enregistrée avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement de la photo: " + e.getMessage());
        }
        return "redirect:/directeur/employe/contrat/" + id;
    }

    @PostMapping("/directeur/employe/contrat/{id}/photo/delete")
    public String deleteEmployeePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeService.deleteEmployeePhoto(id);
        redirectAttributes.addFlashAttribute("success", "La photo de l'employé a été supprimée avec succès.");
        return "redirect:/directeur/employe/contrat/" + id;
    }

    @GetMapping("/directeur/employes/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) {
        boolean exists = employeService.emailExists(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "Cette adresse email est déjà utilisée. Choisissez une autre adresse avant d'enregistrer l'employé." : "Cette adresse email est disponible.");
        return response;
    }

    @GetMapping("/directeur/employes/check-phone")
    @ResponseBody
    public Map<String, Object> checkPhone(@RequestParam String telephone) {
        Map<String, Object> result = employeService.validateAndCheckPhone(telephone);
        return result;
    }

    @GetMapping("/directeur/employes/check-password")
    @ResponseBody
    public Map<String, Object> checkPassword(@RequestParam String password) {
        Map<String, Object> result = employeService.validatePassword(password);
        return result;
    }

    @PostMapping("/directeur/employes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createEmployee(
            @RequestParam String fonction,
            @RequestParam String email,
            @RequestParam String prenom,
            @RequestParam String nom,
            @RequestParam String sexe,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String adresse,
            @RequestParam Long typeContratId,
            @RequestParam(required = false) BigDecimal salaireMensuel,
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(required = false) Long matiereId,
            @RequestParam(required = false) BigDecimal heuresHebdo,
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) MultipartFile photo) {
        
        try {
            employeService.createEmployee(fonction, email, prenom, nom, sexe, telephone, password, adresse, 
                typeContratId, salaireMensuel, dateDebut, dateFin, matiereId, heuresHebdo, specialite, photo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employé créé avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la création de l'employé: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/directeur/profil_professeur")
    public String ProfilsProfesseurs(Model model) {
        model.addAttribute("pageTitle", "Profil du Professeur");
        model.addAttribute("currentRole", "directeur");
        return "directeur/profil_professeur";
    }

    @GetMapping("/directeur/ecolages")
    public String ecolages(Model model) {
        model.addAttribute("pageTitle", "Écolages du Mois");
        model.addAttribute("currentRole", "directeur");
        return "directeur/ecolages";
    }

    @GetMapping({"/directeur/edt", "/fragments/directeur/edt"})
    public String edt(
            @RequestParam(name = "salle_id", required = false) Long salleId,
            @RequestParam(name = "niveau_id", required = false) Long niveauId,
            Model model) {
        
        // First get all model data for the timetable
        AnneeScolaire annee = edtService.getAnneeActive();
        List<Niveau> niveaux = edtService.getAllNiveaux();
        List<Salle> salles = edtService.getActiveSalles();

        if (salleId == null && !salles.isEmpty()) {
            salleId = salles.get(0).getId();
        }

        Salle selectedSalle = null;
        if (salleId != null) {
            for (Salle s : salles) {
                if (s.getId().equals(salleId)) {
                    selectedSalle = s;
                    break;
                }
            }
        }

        if (selectedSalle == null && !salles.isEmpty()) {
            selectedSalle = salles.get(0);
            salleId = selectedSalle.getId();
        }

        List<HoraireEdt> horaires = edtService.getHoraires(niveauId);

        Niveau selectedNiveau = null;
        if (niveauId != null) {
            for (Niveau n : niveaux) {
                if (n.getId().equals(niveauId)) {
                    selectedNiveau = n;
                    break;
                }
            }
        }

        String selectedNiveauLibelle = selectedNiveau != null ? selectedNiveau.getLibelle() : "";

        Map<Long, Map<Integer, CreneauDTO>> parHoraire = new HashMap<>();
        if (annee != null && salleId != null) {
            parHoraire = edtService.getCreneauxParSalle(annee.getId(), salleId, horaires);
        }

        Integer creneauxCount = annee != null && salleId != null ? edtService.countCreneaux(annee.getId(), salleId) : 0;
        List<AffectationDetailDTO> affectations = annee != null ? edtService.getAffectationsWithDetails(annee.getId()) : List.of();

        Map<Integer, String> jours = new HashMap<>();
        jours.put(1, "Lundi");
        jours.put(2, "Mardi");
        jours.put(3, "Mercredi");
        jours.put(4, "Jeudi");
        jours.put(5, "Vendredi");
        jours.put(6, "Samedi");

        model.addAttribute("annee", annee);
        model.addAttribute("niveaux", niveaux);
        model.addAttribute("selectedNiveauId", niveauId);
        model.addAttribute("selectedNiveauLibelle", selectedNiveauLibelle);
        model.addAttribute("salles", salles);
        model.addAttribute("selectedSalleId", salleId);
        model.addAttribute("selectedSalle", selectedSalle);
        model.addAttribute("horaires", horaires);
        model.addAttribute("jours", jours);
        model.addAttribute("parHoraire", parHoraire);
        model.addAttribute("creneauxCount", creneauxCount);
        model.addAttribute("affectations", affectations);
        model.addAttribute("pageTitle", "Emplois du temps");
        model.addAttribute("currentRole", "directeur");

        return "directeur/edt";
    }

    @PostMapping({"/directeur/edt", "/fragments/directeur/edt"})
    public String saveEdt(
            @RequestParam("salle_id") Long salleId,
            @RequestParam("affectation_id") Long affectationId,
            @RequestParam(name = "cells", required = false) List<String> cells) {

        if (cells != null && !cells.isEmpty()) {
            edtService.saveCreneaux(salleId, affectationId, cells);
        }

        return "redirect:/directeur/edt?salle_id=" + salleId;
    }

    @PostMapping({"/directeur/edt/save-horaires", "/fragments/directeur/edt/save-horaires"})
    public String saveHoraires(
            @RequestParam("niveau_id") Long niveauId,
            @RequestParam("salle_id") Long salleId,
            @RequestParam(name = "reset_to_global", required = false) Boolean resetToGlobal,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            edtService.saveHorairesFromRequest(niveauId, request, resetToGlobal != null ? resetToGlobal : false);
        } catch (Exception e) {
            e.printStackTrace();
            // Just redirect, even if there's an error
        }
        return "redirect:/directeur/edt?salle_id=" + salleId + "&niveau_id=" + niveauId;
    }

    // --- Initialization Routes ---
    @GetMapping("/directeur/initialize")
    public String initialize(Model model) {
        model.addAttribute("pageTitle", "Initialisation de l'application");
        model.addAttribute("currentRole", "directeur");
        model.addAttribute("etablissements", initializeService.getAllEtablissements());
        model.addAttribute("anneesScolaires", initializeService.getAllAnneesScolaires());
        model.addAttribute("niveaux", initializeService.getAllNiveaux());
        model.addAttribute("salles", initializeService.getAllSalles());
        model.addAttribute("classes", initializeService.getAllClasses());
        model.addAttribute("matieres", initializeService.getAllMatieres());
        
        // Convert to DTOs to avoid circular reference issues
        List<ProfesseurDTO> professeurDTOs = ProfilsProfesseursService.findAll().stream()
            .map(p -> {
                ProfesseurDTO dto = new ProfesseurDTO();
                dto.setId(p.getId());
                dto.setNom(p.getNom());
                dto.setPrenom(p.getPrenom());
                dto.setMatricule(p.getMatricule());
                dto.setSpecialite(p.getSpecialite());
                dto.setTelephone(p.getTelephone());
                dto.setAdresse(p.getAdresse());
                dto.setTypeContrat(p.getTypeContrat());
                return dto;
            }).collect(Collectors.toList());
        model.addAttribute("professeurs", professeurDTOs);
        
        List<CoefficientDTO> coefficientDTOs = coefficientService.findAll().stream()
            .map(c -> {
                CoefficientDTO dto = new CoefficientDTO();
                dto.setId(c.getId());
                dto.setMatiereId(c.getMatiereId());
                dto.setNiveauId(c.getNiveauId());
                dto.setValeur(c.getValeur());
                
                // Get matiere and niveau names
                if (c.getMatiereId() != null) {
                    matiereService.findById(c.getMatiereId()).ifPresent(m -> dto.setMatiereNom(m.getNom()));
                }
                if (c.getNiveauId() != null) {
                    initializeService.getAllNiveaux().stream()
                        .filter(n -> n.getId().equals(c.getNiveauId()))
                        .findFirst()
                        .ifPresent(n -> dto.setNiveauNom(n.getLibelle()));
                }
                
                return dto;
            }).collect(Collectors.toList());
        model.addAttribute("coefficients", coefficientDTOs);
        
        List<AffectationDTO> affectationDTOs = affectationEnseignementService.findAll().stream()
            .map(a -> {
                AffectationDTO dto = new AffectationDTO();
                dto.setId(a.getId());
                dto.setProfesseurId(a.getProfesseur() != null ? a.getProfesseur().getId() : null);
                dto.setProfesseurNom(buildProfesseurNom(a.getProfesseur()));
                dto.setMatiereId(a.getMatiere() != null ? a.getMatiere().getId() : null);
                dto.setMatiereNom(a.getMatiere() != null ? a.getMatiere().getNom() : null);
                dto.setClasseId(a.getClasse() != null ? a.getClasse().getId() : null);
                dto.setClasseNom(a.getClasse() != null ? a.getClasse().getNom() : null);
                dto.setAnneeScolaireId(a.getAnneeScolaire() != null ? a.getAnneeScolaire().getId() : null);
                dto.setAnneeScolaireNom(a.getAnneeScolaire() != null ? a.getAnneeScolaire().getLibelle() : null);
                dto.setHeuresHebdo(a.getHeuresHebdo());
                return dto;
            }).collect(Collectors.toList());
        model.addAttribute("affectations", affectationDTOs);
        
        return "directeur/initialize";
    }
    
    private String buildProfesseurNom(ProfilsProfesseurs professeur) {
        if (professeur == null) return null;
        return professeur.getNom() + " " + professeur.getPrenom();
    }

    // --- API Endpoints for Initialization ---
    @GetMapping("/api/directeur/initialize/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInitializeData() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("etablissements", initializeService.getAllEtablissements());
            data.put("anneesScolaires", initializeService.getAllAnneesScolaires());
            data.put("niveaux", initializeService.getAllNiveaux());
            data.put("salles", initializeService.getAllSalles());
            data.put("classes", initializeService.getAllClasses());
            data.put("matieres", initializeService.getAllMatieres());
            
            // Convert to DTOs to avoid circular reference issues
            List<ProfesseurDTO> professeurDTOs = ProfilsProfesseursService.findAll().stream()
                .map(p -> {
                    ProfesseurDTO dto = new ProfesseurDTO();
                    dto.setId(p.getId());
                    dto.setNom(p.getNom());
                    dto.setPrenom(p.getPrenom());
                    dto.setMatricule(p.getMatricule());
                    dto.setSpecialite(p.getSpecialite());
                    dto.setTelephone(p.getTelephone());
                    dto.setAdresse(p.getAdresse());
                    dto.setTypeContrat(p.getTypeContrat());
                    return dto;
                }).collect(Collectors.toList());
            data.put("professeurs", professeurDTOs);
            
            List<CoefficientDTO> coefficientDTOs = coefficientService.findAll().stream()
                .map(c -> {
                    CoefficientDTO dto = new CoefficientDTO();
                    dto.setId(c.getId());
                    dto.setMatiereId(c.getMatiereId());
                    dto.setNiveauId(c.getNiveauId());
                    dto.setValeur(c.getValeur());
                    
                    // Get matiere and niveau names
                    if (c.getMatiereId() != null) {
                        matiereService.findById(c.getMatiereId()).ifPresent(m -> dto.setMatiereNom(m.getNom()));
                    }
                    if (c.getNiveauId() != null) {
                        initializeService.getAllNiveaux().stream()
                            .filter(n -> n.getId().equals(c.getNiveauId()))
                            .findFirst()
                            .ifPresent(n -> dto.setNiveauNom(n.getLibelle()));
                    }
                    
                    return dto;
                }).collect(Collectors.toList());
            data.put("coefficients", coefficientDTOs);
            
            List<AffectationDTO> affectationDTOs = affectationEnseignementService.findAll().stream()
                .map(a -> {
                    AffectationDTO dto = new AffectationDTO();
                    dto.setId(a.getId());
                    dto.setProfesseurId(a.getProfesseur() != null ? a.getProfesseur().getId() : null);
                    dto.setProfesseurNom(buildProfesseurNom(a.getProfesseur()));
                    dto.setMatiereId(a.getMatiere() != null ? a.getMatiere().getId() : null);
                    dto.setMatiereNom(a.getMatiere() != null ? a.getMatiere().getNom() : null);
                    dto.setClasseId(a.getClasse() != null ? a.getClasse().getId() : null);
                    dto.setClasseNom(a.getClasse() != null ? a.getClasse().getNom() : null);
                    dto.setAnneeScolaireId(a.getAnneeScolaire() != null ? a.getAnneeScolaire().getId() : null);
                    dto.setAnneeScolaireNom(a.getAnneeScolaire() != null ? a.getAnneeScolaire().getLibelle() : null);
                    dto.setHeuresHebdo(a.getHeuresHebdo());
                    return dto;
                }).collect(Collectors.toList());
            data.put("affectations", affectationDTOs);
        } catch (Exception e) {
            // If any service fails, still return the data that was successfully loaded
            System.err.println("Error loading initialization data: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(data);
    }

    // Etablissement
    @PostMapping("/api/directeur/initialize/etablissement")
    @ResponseBody
    public ResponseEntity<Etablissement> saveEtablissement(@RequestBody Etablissement etablissement) {
        return ResponseEntity.ok(initializeService.saveEtablissement(etablissement));
    }

    @DeleteMapping("/api/directeur/initialize/etablissement/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteEtablissement(@PathVariable Long id) {
        initializeService.deleteEtablissement(id);
        return ResponseEntity.ok().build();
    }

    // Annee Scolaire
    @PostMapping("/api/directeur/initialize/annee-scolaire")
    @ResponseBody
    public ResponseEntity<AnneeScolaire> saveAnneeScolaire(@RequestBody AnneeScolaire annee) {
        return ResponseEntity.ok(initializeService.saveAnneeScolaire(annee));
    }

    @DeleteMapping("/api/directeur/initialize/annee-scolaire/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAnneeScolaire(@PathVariable Long id) {
        initializeService.deleteAnneeScolaire(id);
        return ResponseEntity.ok().build();
    }

    // Niveau
    @PostMapping("/api/directeur/initialize/niveau")
    @ResponseBody
    public ResponseEntity<Niveau> saveNiveau(@RequestBody Niveau niveau) {
        return ResponseEntity.ok(initializeService.saveNiveau(niveau));
    }

    @DeleteMapping("/api/directeur/initialize/niveau/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteNiveau(@PathVariable Long id) {
        initializeService.deleteNiveau(id);
        return ResponseEntity.ok().build();
    }

    // Salle
    @PostMapping("/api/directeur/initialize/salle")
    @ResponseBody
    public ResponseEntity<Salle> saveSalle(@RequestBody Salle salle) {
        return ResponseEntity.ok(initializeService.saveSalle(salle));
    }

    @DeleteMapping("/api/directeur/initialize/salle/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteSalle(@PathVariable Long id) {
        initializeService.deleteSalle(id);
        return ResponseEntity.ok().build();
    }

    // Classe
    @PostMapping("/api/directeur/initialize/classe")
    @ResponseBody
    public ResponseEntity<Classe> saveClasse(@RequestBody Classe classe) {
        return ResponseEntity.ok(initializeService.saveClasse(classe));
    }

    @DeleteMapping("/api/directeur/initialize/classe/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteClasse(@PathVariable Long id) {
        initializeService.deleteClasse(id);
        return ResponseEntity.ok().build();
    }

    // Matiere
    @PostMapping("/api/directeur/initialize/matiere")
    @ResponseBody
    public ResponseEntity<Matiere> saveMatiere(@RequestBody Matiere matiere) {
        return ResponseEntity.ok(initializeService.saveMatiere(matiere));
    }

    @DeleteMapping("/api/directeur/initialize/matiere/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteMatiere(@PathVariable Long id) {
        initializeService.deleteMatiere(id);
        return ResponseEntity.ok().build();
    }

    // Coefficient
    @PostMapping("/api/directeur/initialize/coefficient")
    @ResponseBody
    public ResponseEntity<CoefficientDTO> saveCoefficient(@RequestBody Map<String, Object> payload) {
        Coefficient coefficient = new Coefficient();
        coefficient.setMatiereId(((Number) payload.get("matiereId")).longValue());
        coefficient.setNiveauId(((Number) payload.get("niveauId")).longValue());
        coefficient.setValeur(new BigDecimal(payload.get("valeur").toString()));
        
        Coefficient saved = coefficientService.save(coefficient);
        
        CoefficientDTO dto = new CoefficientDTO();
        dto.setId(saved.getId());
        dto.setMatiereId(saved.getMatiereId());
        dto.setNiveauId(saved.getNiveauId());
        dto.setValeur(saved.getValeur());
        
        // Get matiere and niveau names
        if (saved.getMatiereId() != null) {
            matiereService.findById(saved.getMatiereId()).ifPresent(m -> dto.setMatiereNom(m.getNom()));
        }
        if (saved.getNiveauId() != null) {
            initializeService.getAllNiveaux().stream()
                .filter(n -> n.getId().equals(saved.getNiveauId()))
                .findFirst()
                .ifPresent(n -> dto.setNiveauNom(n.getLibelle()));
        }
        
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/api/directeur/initialize/coefficient/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCoefficient(@PathVariable Long id) {
        coefficientService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Affectation
    @PostMapping("/api/directeur/initialize/affectation")
    @ResponseBody
    public ResponseEntity<AffectationDTO> saveAffectation(@RequestBody Map<String, Object> payload) {
        AffectationEnseignement affectation = new AffectationEnseignement();
        
        Long professeurId = payload.get("professeurId") != null ? ((Number) payload.get("professeurId")).longValue() : null;
        Long matiereId = payload.get("matiereId") != null ? ((Number) payload.get("matiereId")).longValue() : null;
        Long classeId = payload.get("classeId") != null ? ((Number) payload.get("classeId")).longValue() : null;
        Long anneeScolaireId = payload.get("anneeScolaireId") != null ? ((Number) payload.get("anneeScolaireId")).longValue() : null;
        
        if (professeurId != null) {
            ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
            affectation.setProfesseur(professeur);
        }
        
        if (matiereId != null) {
            Matiere matiere = matiereService.findById(matiereId).orElse(null);
            affectation.setMatiere(matiere);
        }
        
        if (classeId != null) {
            initializeService.getClasseById(classeId).ifPresent(affectation::setClasse);
        }
        
        if (anneeScolaireId != null) {
            initializeService.getAnneeScolaireById(anneeScolaireId).ifPresent(affectation::setAnneeScolaire);
        }
        
        affectation.setHeuresHebdo(new BigDecimal(payload.get("heuresHebdo").toString()));
        affectation.setCreatedAt(java.time.LocalDateTime.now());
        
        AffectationEnseignement saved = affectationEnseignementService.save(affectation);
        
        AffectationDTO dto = new AffectationDTO();
        dto.setId(saved.getId());
        dto.setProfesseurId(saved.getProfesseur() != null ? saved.getProfesseur().getId() : null);
        dto.setProfesseurNom(buildProfesseurNom(saved.getProfesseur()));
        dto.setMatiereId(saved.getMatiere() != null ? saved.getMatiere().getId() : null);
        dto.setMatiereNom(saved.getMatiere() != null ? saved.getMatiere().getNom() : null);
        dto.setClasseId(saved.getClasse() != null ? saved.getClasse().getId() : null);
        dto.setClasseNom(saved.getClasse() != null ? saved.getClasse().getNom() : null);
        dto.setAnneeScolaireId(saved.getAnneeScolaire() != null ? saved.getAnneeScolaire().getId() : null);
        dto.setAnneeScolaireNom(saved.getAnneeScolaire() != null ? saved.getAnneeScolaire().getLibelle() : null);
        dto.setHeuresHebdo(saved.getHeuresHebdo());
        
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/api/directeur/initialize/affectation/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAffectation(@PathVariable Long id) {
        affectationEnseignementService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Employees API Endpoints ---
    @GetMapping("/api/directeur/employes")
    @ResponseBody
    public ResponseEntity<List<VueEmployesDetail>> getAllEmployes() {
        return ResponseEntity.ok(employeService.getAllEmployes());
    }

    @GetMapping("/api/directeur/employes/professeurs")
    @ResponseBody
    public ResponseEntity<List<VueEmployesDetail>> getProfesseurs() {
        return ResponseEntity.ok(employeService.getProfesseurs());
    }

    @GetMapping("/api/directeur/employes/secretaires")
    @ResponseBody
    public ResponseEntity<List<VueEmployesDetail>> getSecretaires() {
        return ResponseEntity.ok(employeService.getSecretaires());
    }

    // Filtre etudiants 
    // ----------------------------------------------------------------
    // PAGE HTML — rendu Thymeleaf
    // ----------------------------------------------------------------

    @GetMapping("/directeur/etudiants")
    public String page(Model model) {
        model.addAttribute("pageTitle", "Étudiants");
        model.addAttribute("currentRole", "directeur");
        // Les données de filtre (selects) sont chargées via JS au montage
        return "directeur/etudiants-filtre";
    }

    // ----------------------------------------------------------------
    // API — données pour peupler les selects du formulaire
    // ----------------------------------------------------------------

    @GetMapping("/api/directeur/etudiants/form-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFormData() {
        return ResponseEntity.ok(filterService.getFilterFormData());
    }

    // ----------------------------------------------------------------
    // API — détail d'un étudiant par ID
    // ----------------------------------------------------------------

    @GetMapping("/api/directeur/etudiants/{id}")
    @ResponseBody
    public ResponseEntity<EtudiantFilterResult> getEtudiantById(@PathVariable Long id) {
        EtudiantFilterResult etudiant = filterService.getEtudiantById(id);
        if (etudiant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(etudiant);
    }

    // ----------------------------------------------------------------
    // API — recherche paginée + filtrée (appelée à chaque changement)
    // ----------------------------------------------------------------

    @GetMapping("/api/directeur/etudiants/recherche")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechercher(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String statutInscription,
            @RequestParam(required = false) String typeInscription,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateInscriptionDebut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateInscriptionFin,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissanceDebut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissanceFin,
            @RequestParam(required = false) Boolean isArchived,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "15")  int pageSize,
            @RequestParam(defaultValue = "nom")  String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir
    ) {
        EtudiantFilterCriteria criteria = new EtudiantFilterCriteria();
        criteria.setRecherche(recherche);
        criteria.setSexe(sexe);
        criteria.setRegion(region);
        criteria.setNationalite(nationalite);
        criteria.setClasseId(classeId);
        criteria.setNiveauId(niveauId);
        criteria.setAnneeScolaireId(anneeScolaireId);
        criteria.setStatutInscription(statutInscription);
        criteria.setTypeInscription(typeInscription);
        criteria.setDateInscriptionDebut(dateInscriptionDebut);
        criteria.setDateInscriptionFin(dateInscriptionFin);
        criteria.setDateNaissanceDebut(dateNaissanceDebut);
        criteria.setDateNaissanceFin(dateNaissanceFin);
        criteria.setIsArchived(isArchived != null ? isArchived : false);
        criteria.setPage(page);
        criteria.setPageSize(pageSize);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir);

        Page<EtudiantFilterResult> result = filterService.filtrer(criteria);

        // Réponse enrichie pour le JS (évite de re-parser du côté client)
        return ResponseEntity.ok(Map.of(
            "content",       result.getContent(),
            "totalElements", result.getTotalElements(),
            "totalPages",    result.getTotalPages(),
            "currentPage",   result.getNumber(),
            "pageSize",      result.getSize(),
            "hasNext",       result.hasNext(),
            "hasPrevious",   result.hasPrevious()
        ));
    }

    //Statistique etudiant 
    // ----------------------------------------------------------------
    // PAGE HTML — Statistiques Élèves (détection décrochage)
    // ----------------------------------------------------------------

    @GetMapping("/directeur/statistiques-eleves")
    public String statistiquesEleves(Model model) {
        model.addAttribute("pageTitle", "Statistiques Élèves");
        model.addAttribute("currentRole", "directeur");
        return "directeur/statistiques-eleves";
    }

    // ----------------------------------------------------------------
    // API — classes disponibles pour le filtre
    // ----------------------------------------------------------------

    @GetMapping("/api/directeur/statistiques-eleves/classes")
    @ResponseBody
    public ResponseEntity<List<Classe>> getClassesStatistiques(
            @RequestParam(required = false) Long anneeScolaireId) {
        return ResponseEntity.ok(statistiquesElevesService.listerClasses(anneeScolaireId));
    }

    // ----------------------------------------------------------------
    // API — analyse décrochage (croisement moyennes / absences)
    // ----------------------------------------------------------------

    @GetMapping("/api/directeur/statistiques-eleves")
    @ResponseBody
    public ResponseEntity<StatistiquesElevesResponse> getStatistiquesEleves(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long periodeFinId,
            @RequestParam(required = false) Double seuilBaisseMoyenne,
            @RequestParam(required = false) Double seuilTauxAbsence
    ) {
        StatistiquesElevesCriteria criteria = new StatistiquesElevesCriteria();
        criteria.setAnneeScolaireId(anneeScolaireId);
        criteria.setClasseId(classeId);
        criteria.setPeriodeFinId(periodeFinId);
        if (seuilBaisseMoyenne != null) criteria.setSeuilBaisseMoyenne(seuilBaisseMoyenne);
        if (seuilTauxAbsence != null) criteria.setSeuilTauxAbsence(seuilTauxAbsence);

        return ResponseEntity.ok(statistiquesElevesService.analyser(criteria));
    }

    private String buildExportFilename(String prefix, DashboardResponseDTO dashboard) {
        String suffix = dashboard != null && dashboard.getSelectedMonth() != null
                ? dashboard.getSelectedMonth()
                : java.time.YearMonth.now().toString();
        return prefix + "-" + suffix.replaceAll("[^0-9A-Za-z_-]", "-") + ".pdf";
    }
}
