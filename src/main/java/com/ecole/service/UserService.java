package com.ecole.service;

import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.User;
import com.ecole.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("userLoggedIn");

        if (user == null) {
            return null;
        }

        ProfilEtudiant profil = (ProfilEtudiant) session.getAttribute("profilEtudiant");
        if (profil == null) {
            profil = userRepository.findProfilEtudiantByUserId(user.getId());
            if (profil != null) {
                session.setAttribute("profilEtudiant", profil);
            }
        }

        return user;
    }

    public ProfilEtudiant getCurrentProfil(HttpSession session) {
        ProfilEtudiant profil = (ProfilEtudiant) session.getAttribute("profilEtudiant");

        if (profil == null) {
            User user = getCurrentUser(session);
            if (user != null) {
                profil = userRepository.findProfilEtudiantByUserId(user.getId());
                if (profil != null) {
                    session.setAttribute("profilEtudiant", profil);
                }
            }
        }

        return profil;
    }
}
