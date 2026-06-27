package com.ecole.controller;

import com.ecole.entity.*;
import com.ecole.service.EdtService;
import com.ecole.service.InitializeService;
import com.ecole.service.EmployeService;
import com.ecole.dto.Directeur.*;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class DirecteurController {

    private final EdtService edtService;
    private final InitializeService initializeService;
    private final EmployeService employeService;
    private final com.ecole.service.MatiereService matiereService;
    private final com.ecole.service.TypesContratsEmployesService typesContratsEmployesService;

    @GetMapping("/directeur/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("currentRole", "directeur");
        return "directeur/dashboard";
    }

    @GetMapping("/directeur/finances")
    public String finances(Model model) {
        model.addAttribute("pageTitle", "Finances & Bénéfices");
        model.addAttribute("currentRole", "directeur");
        return "directeur/finances";
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
        return "directeur/initialize";
    }

    // --- API Endpoints for Initialization ---
    @GetMapping("/api/directeur/initialize/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInitializeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("etablissements", initializeService.getAllEtablissements());
        data.put("anneesScolaires", initializeService.getAllAnneesScolaires());
        data.put("niveaux", initializeService.getAllNiveaux());
        data.put("salles", initializeService.getAllSalles());
        data.put("classes", initializeService.getAllClasses());
        data.put("matieres", initializeService.getAllMatieres());
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
}
