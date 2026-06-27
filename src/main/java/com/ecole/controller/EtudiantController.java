package com.ecole.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EtudiantController {

    @GetMapping("/etudiant/emploi")
    public String emploi(Model model) {
        model.addAttribute("pageTitle", "Mon Emploi du Temps");
        model.addAttribute("currentRole", "etudiant");
        return "Etudiant/calendar";
    }

    @GetMapping("/etudiant/notes")
    public String notes(Model model) {
        model.addAttribute("pageTitle", "Mes Notes");
        model.addAttribute("currentRole", "etudiant");
        return "Etudiant/notes";
    }

    @GetMapping("/etudiant/devoirs")
    public String devoirs(Model model) {
        model.addAttribute("pageTitle", "Devoirs & Leçons");
        model.addAttribute("currentRole", "etudiant");
        return "Etudiant/devoirs";
    }

    @GetMapping("/etudiant/bulletin")
    public String bulletin(Model model) {
        model.addAttribute("pageTitle", "Mon Bulletin");
        model.addAttribute("currentRole", "etudiant");
        return "Etudiant/bulletin";
    }
}
