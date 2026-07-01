package com.ecole.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecons")
public class Lecon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id")
    private AffectationEnseignement affectation;
    
    // Méthode pour compatibilité
    public AffectationEnseignement getAffectationId() {
        return affectation;
    }
    
    public void setAffectationId(AffectationEnseignement affectation) {
        this.affectation = affectation;
    }

    private String titre;

    private String contenu;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AffectationEnseignement getAffectation() {
        return affectation;
    }

    public void setAffectation(AffectationEnseignement affectation) {
        this.affectation = affectation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
