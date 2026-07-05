package com.ecole.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ecole.service.ActualiteService;

@RestController
@RequestMapping("/api/actualites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActualiteRestController {

    @Autowired
    private ActualiteService actualiteService;

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
    public ResponseEntity<Actualite> create(@RequestBody ActualiteDTO dto) {
        Actualite actualite = new Actualite();
        actualite.setTitre(dto.getTitre());
        actualite.setContenu(dto.getContenu());
        actualite.setCategorie(dto.getCategorie());
        actualite.setAuteurId(dto.getAuteurId());
        actualite.setAuteurNom(dto.getAuteurNom());
        actualite.setIconeClasse(dto.getIconeClasse());
        actualite.setImageUrl(dto.getImageUrl());
        actualite.setEstActive(true);

        Actualite saved = actualiteService.save(actualite);
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
}
