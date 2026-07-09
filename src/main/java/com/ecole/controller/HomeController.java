package com.ecole.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";  // Affiche la page index.html
    }

    @GetMapping("/login")
    public String login() {
        return "index";
    }

    @GetMapping("/error")
    public String error() {
        return "error";  // Va chercher templates/error.html
    }
}