package com.ecole.dto.Secretaire;

public class EtudiantRechercheDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String telephone;
    private Long classeId;
    private String nomClasse;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public String getNomClasse() { return nomClasse; }
    public void setNomClasse(String nomClasse) { this.nomClasse = nomClasse; }
}
