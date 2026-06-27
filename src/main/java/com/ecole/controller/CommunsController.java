package com.ecole.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunsController {

    @GetMapping("/communs/actualites")
    public String actualites(Model model) {
        model.addAttribute("pageTitle", "Actualités");
        model.addAttribute("currentRole", "directeur");
        return "communs/actualites";
    }

    @GetMapping("/communs/notifications")
    public String notifications(Model model) {
        model.addAttribute("pageTitle", "Notifications");
        model.addAttribute("currentRole", "directeur");
        return "communs/notifications";
    }
}
