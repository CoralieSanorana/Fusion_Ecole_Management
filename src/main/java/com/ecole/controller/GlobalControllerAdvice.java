package com.ecole.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.ProfilsDirecteurs;
import com.ecole.entity.ProfilsProfesseurs;
import com.ecole.entity.ProfilsSecretariat;
import com.ecole.entity.User;
import com.ecole.repository.ProfilEtudiantRepository;
import com.ecole.repository.ProfilsDirecteursRepository;
import com.ecole.repository.ProfilsProfesseursRepository;
import com.ecole.repository.ProfilsSecretariatRepository;
import com.ecole.repository.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfilEtudiantRepository profilEtudiantRepository;

    @Autowired
    private ProfilsProfesseursRepository profilsProfesseursRepository;

    @Autowired
    private ProfilsSecretariatRepository profilsSecretariatRepository;

    @Autowired
    private ProfilsDirecteursRepository profilsDirecteursRepository;

    @ModelAttribute
    public void addUserInfoToModel(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("currentUser", user);

                // Get user's name and role info
                Long userId = null;
                String userName = user.getEmail();
                String userRoleLabel = "Utilisateur";
                String userInitials = "US";

                // Get user's roles
                Optional<String> roleOpt = user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getNom())
                        .findFirst();

                if (roleOpt.isPresent()) {
                    String role = roleOpt.get();
                    userRoleLabel = getRoleLabel(role);

                    // Try to get profile based on role
                    switch (role.toUpperCase()) {
                        case "ETUDIANT":
                            Optional<ProfilEtudiant> etudiantOpt = profilEtudiantRepository.findByUserId(user.getId());
                            if (etudiantOpt.isPresent()) {
                                ProfilEtudiant etudiant = etudiantOpt.get();
                                userId = etudiant.getId();
                                userName = etudiant.getPrenom() + " " + etudiant.getNom();
                                userInitials = getInitials(userName);
                            }
                            break;
                        case "PROFESSEUR":
                            Optional<ProfilsProfesseurs> profOpt = profilsProfesseursRepository
                                    .findByUserId(user.getId());
                            if (profOpt.isPresent()) {
                                ProfilsProfesseurs prof = profOpt.get();
                                userName = prof.getPrenom() + " " + prof.getNom();
                                userInitials = getInitials(userName);
                            }
                            break;
                        case "SECRETARIAT":
                            Optional<ProfilsSecretariat> secretariatOpt = profilsSecretariatRepository
                                    .findByUserId(user.getId());
                            if (secretariatOpt.isPresent()) {
                                ProfilsSecretariat secretariat = secretariatOpt.get();
                                userName = secretariat.getPrenom() + " " + secretariat.getNom();
                                userInitials = getInitials(userName);
                            }
                            break;
                        case "DIRECTEUR":
                            Optional<ProfilsDirecteurs> directeurOpt = profilsDirecteursRepository
                                    .findByUserId(user.getId());
                            if (directeurOpt.isPresent()) {
                                ProfilsDirecteurs directeur = directeurOpt.get();
                                userName = directeur.getPrenom() + " " + directeur.getNom();
                                userInitials = getInitials(userName);
                            }
                            break;
                    }
                }

                model.addAttribute("userId", userId);
                model.addAttribute("currentUserName", userName);
                model.addAttribute("currentUserRoleLabel", userRoleLabel);
                model.addAttribute("currentUserInitials", userInitials);
            }
        }
    }

    private String getRoleLabel(String role) {
        return switch (role.toUpperCase()) {
            case "DIRECTEUR" -> "Directeur";
            case "SECRETARIAT" -> "Secrétaire";
            case "PROFESSEUR" -> "Professeur";
            case "ETUDIANT" -> "Étudiant";
            case "PARENT" -> "Parent";
            default -> "Utilisateur";
        };
    }

    private String getInitials(String name) {
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return "US";
    }
}