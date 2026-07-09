package com.ecole.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supports_cours")
public class SupportCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "affectation_id")
    private Integer affectationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id", insertable = false, updatable = false)
    private AffectationEnseignement affectation;

    @Column(name = "type_fichier_id")
    private Integer typeFichierId;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "fichier_url")
    private String fichierUrl;

    @Column(name = "type_contenu")
    private String typeContenu = "lecon";

    @Column(name = "date_limite")
    private LocalDateTime dateLimite;

    @Column(name = "accepte_retard")
    private Boolean accepteRetard = false;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "cree_par")
    private Integer creePar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AffectationEnseignement getAffectation() {
        return affectation;
    }

    public void setAffectation(AffectationEnseignement affectation) {
        this.affectation = affectation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAffectationId() {
        return affectationId;
    }

    public void setAffectationId(Integer affectationId) {
        this.affectationId = affectationId;
    }

    public Integer getTypeFichierId() {
        return typeFichierId;
    }

    public void setTypeFichierId(Integer typeFichierId) {
        this.typeFichierId = typeFichierId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFichierUrl() {
        return fichierUrl;
    }

    public void setFichierUrl(String fichierUrl) {
        this.fichierUrl = fichierUrl;
    }

    public String getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(String typeContenu) {
        this.typeContenu = typeContenu;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public Boolean getAccepteRetard() {
        return accepteRetard;
    }

    public void setAccepteRetard(Boolean accepteRetard) {
        this.accepteRetard = accepteRetard;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getCreePar() {
        return creePar;
    }

    public void setCreePar(Integer creePar) {
        this.creePar = creePar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
