package com.ecole.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecretaireController {

    @GetMapping("/secretariat/paiement")
    public String paiement(Model model) {
        model.addAttribute("pageTitle", "Ajouter un Paiement");
        model.addAttribute("currentRole", "secretariat");
        return "Secretaire/paiement";
    }

    @GetMapping("/secretariat/bilan")
    public String bilan(Model model) {
        model.addAttribute("pageTitle", "Bilan de Paiement");
        model.addAttribute("currentRole", "secretariat");
        return "Secretaire/bilan";
    }

    @GetMapping("/secretariat/eleves")
    public String eleves(Model model) {
        model.addAttribute("pageTitle", "Liste des Élèves");
        model.addAttribute("currentRole", "secretariat");
        return "Secretaire/eleves";
    }

    @GetMapping("/secretariat/profil")
    public String profil(Model model) {
        model.addAttribute("pageTitle", "Profil de l'Élève");
        model.addAttribute("currentRole", "secretariat");
        return "Secretaire/profil_eleve";
    }
}
