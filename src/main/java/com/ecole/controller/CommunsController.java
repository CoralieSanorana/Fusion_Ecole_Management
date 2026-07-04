package com.ecole.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ecole.service.ActualiteService;
import com.ecole.service.NotificationService;

@Controller
public class CommunsController {

    @Autowired
    private ActualiteService actualiteService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/communs/actualites")
    public String actualites(Model model) {
        model.addAttribute("pageTitle", "Actualités");
        model.addAttribute("currentRole", "directeur");
        model.addAttribute("actualites", actualiteService.findAllActive());
        return "communs/actualites";
    }

    @GetMapping("/communs/notifications")
    public String notifications(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Notifications");
        model.addAttribute("currentRole", "directeur");
        model.addAttribute("notifications", notificationService.findAll());
        return "communs/notifications";
    }
}
