package com.ecole.dto;

import java.time.LocalDateTime;

public class ActualiteDTO {
    private Long id;
    private String titre;
    private String contenu;
    private String categorie;
    private Long auteurId;
    private String auteurNom;
    private String imageUrl;
    private String iconeClasse;
    private LocalDateTime datePublication;
    private Boolean estActive;

    public ActualiteDTO() {
    }

    public ActualiteDTO(String titre, String contenu, String categorie) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public void setAuteurId(Long auteurId) {
        this.auteurId = auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public void setAuteurNom(String auteurNom) {
        this.auteurNom = auteurNom;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIconeClasse() {
        return iconeClasse;
    }

    public void setIconeClasse(String iconeClasse) {
        this.iconeClasse = iconeClasse;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public Boolean getEstActive() {
        return estActive;
    }

    public void setEstActive(Boolean estActive) {
        this.estActive = estActive;
    }
}
