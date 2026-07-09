package com.ecole.api;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecole.dto.ActualiteDTO;
import com.ecole.entity.Actualite;
import com.ecole.entity.Notification;
import com.ecole.entity.User;
import com.ecole.repository.UserRepository;
import com.ecole.service.ActualiteService;
import com.ecole.service.NotificationService;

@RestController
@RequestMapping("/api/actualites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActualiteRestController {

    @Autowired
    private ActualiteService actualiteService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Actualite>> getAll() {
        return ResponseEntity.ok(actualiteService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actualite> getById(@PathVariable Long id) {
        Optional<Actualite> actualite = actualiteService.findById(id);
        return actualite.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Actualite>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(actualiteService.findByCategorie(categorie));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTEUR') or hasRole('SECRETARIAT')")
    public ResponseEntity<Actualite> create(@RequestBody ActualiteDTO dto, Authentication authentication) {
        Optional<User> currentUser = userRepository.findByEmail(authentication.getName());

        Actualite actualite = new Actualite();
        actualite.setTitre(dto.getTitre());
        actualite.setContenu(dto.getContenu());
        actualite.setCategorie(dto.getCategorie());
        currentUser.ifPresentOrElse(user -> {
            actualite.setAuteurId(user.getId());
            actualite.setAuteurNom(user.getEmail());
        }, () -> {
            actualite.setAuteurId(dto.getAuteurId());
            actualite.setAuteurNom(dto.getAuteurNom());
        });
        actualite.setIconeClasse(dto.getIconeClasse());
        actualite.setImageUrl(dto.getImageUrl());
        actualite.setEstActive(true);

        Actualite saved = actualiteService.save(actualite);
        notifierActualiteImportante(saved, currentUser.map(User::getId).orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTEUR') or hasRole('SECRETARIAT')")
    public ResponseEntity<Actualite> update(@PathVariable Long id, @RequestBody ActualiteDTO dto) {
        Optional<Actualite> existing = actualiteService.findById(id);
        if (existing.isPresent()) {
            Actualite actualite = existing.get();
            actualite.setTitre(dto.getTitre());
            actualite.setContenu(dto.getContenu());
            actualite.setCategorie(dto.getCategorie());
            if (dto.getAuteurNom() != null) {
                actualite.setAuteurNom(dto.getAuteurNom());
            }
            if (dto.getIconeClasse() != null) {
                actualite.setIconeClasse(dto.getIconeClasse());
            }
            if (dto.getImageUrl() != null) {
                actualite.setImageUrl(dto.getImageUrl());
            }
            Actualite updated = actualiteService.save(actualite);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTEUR') or hasRole('SECRETARIAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        actualiteService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private void notifierActualiteImportante(Actualite actualite, Long auteurId) {
        if (!doitNotifier(actualite.getCategorie())) {
            return;
        }

        List<User> destinataires = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .filter(user -> auteurId == null || !auteurId.equals(user.getId()))
                .toList();

        for (User destinataire : destinataires) {
            Notification notification = new Notification();
            notification.setUserId(Math.toIntExact(destinataire.getId()));
            notification.setTitre("Nouvelle actualité — " + actualite.getTitre());
            notification.setMessage(actualite.getCategorie() + " : " + actualite.getContenu());
            notification.setLienAction("/communs/actualites");
            notification.setEstLu(false);
            notification.setEntiteType("actualite");
            notification.setEntiteId(Math.toIntExact(actualite.getId()));
            notificationService.save(notification);
        }
    }

    private boolean doitNotifier(String categorie) {
        if (categorie == null) {
            return false;
        }
        String normalized = Normalizer.normalize(categorie.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.equals("important")
                || normalized.equals("evenement");
    }
}
