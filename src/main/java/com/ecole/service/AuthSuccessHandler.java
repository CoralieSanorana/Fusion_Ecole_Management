package com.ecole.service;

import com.ecole.entity.User;
import com.ecole.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Mettre à jour last_login
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Redirection selon priorité des rôles
        Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();
        String redirect;

        if (hasRole(roles, "ROLE_DIRECTEUR")) {
            redirect = "/directeur/dashboard";
        } else if (hasRole(roles, "ROLE_SECRETARIAT")) {
            redirect = "/secretariat/eleves";
        } else if (hasRole(roles, "ROLE_PROFESSEUR")) {
            redirect = "/professeur/notes";
        } else if (hasRole(roles, "ROLE_ETUDIANT")) {
            redirect = "/etudiant/bulletin";
        } else {
            redirect = "/parent/suivi";
        }

        response.sendRedirect(redirect);
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> roles, String roleName) {
        return roles.stream().anyMatch(r -> r.getAuthority().equals(roleName));
    }
}