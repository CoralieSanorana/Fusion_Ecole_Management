package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vue_employes_detail")
@Immutable
@Data
public class VueEmployesDetail {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    private String email;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "role_nom")
    private String roleNom;
    
    private String nom;
    private String prenom;
    
    @Column(name = "contrat_id")
    private Long contratId;
    
    @Column(name = "reference_contrat")
    private String referenceContrat;
    
    private String fonction;
    private String sexe;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "type_contrat_code")
    private String typeContratCode;
    
    @Column(name = "type_contrat_libelle")
    private String typeContratLibelle;
    
    @Column(name = "date_debut")
    private LocalDate dateDebut;
    
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @Column(name = "document_url")
    private String documentUrl;
    
    @Column(name = "salaire_mensuel")
    private BigDecimal salaireMensuel;
    
    @Column(name = "heures_hebdo")
    private BigDecimal heuresHebdo;
    
    @Column(name = "id_matiere")
    private Long idMatiere;
    
    @Column(name = "matiere_nom")
    private String matiereNom;
    
    @Column(name = "jours_restants")
    private Integer joursRestants;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    public String getRoleNom() {
        return roleNom;
    }
    public void setRoleNom(String roleNom) {
        this.roleNom = roleNom;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public Long getContratId() {
        return contratId;
    }
    public void setContratId(Long contratId) {
        this.contratId = contratId;
    }
    public String getReferenceContrat() {
        return referenceContrat;
    }
    public void setReferenceContrat(String referenceContrat) {
        this.referenceContrat = referenceContrat;
    }
    public String getFonction() {
        return fonction;
    }
    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
    public String getSexe() {
        return sexe;
    }
    public void setSexe(String sexe) {
        this.sexe = sexe;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public String getTypeContratCode() {
        return typeContratCode;
    }
    public void setTypeContratCode(String typeContratCode) {
        this.typeContratCode = typeContratCode;
    }
    public String getTypeContratLibelle() {
        return typeContratLibelle;
    }
    public void setTypeContratLibelle(String typeContratLibelle) {
        this.typeContratLibelle = typeContratLibelle;
    }
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    public LocalDate getDateFin() {
        return dateFin;
    }
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    public String getDocumentUrl() {
        return documentUrl;
    }
    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
    public BigDecimal getSalaireMensuel() {
        return salaireMensuel;
    }
    public void setSalaireMensuel(BigDecimal salaireMensuel) {
        this.salaireMensuel = salaireMensuel;
    }
    public BigDecimal getHeuresHebdo() {
        return heuresHebdo;
    }
    public void setHeuresHebdo(BigDecimal heuresHebdo) {
        this.heuresHebdo = heuresHebdo;
    }
    public Long getIdMatiere() {
        return idMatiere;
    }
    public void setIdMatiere(Long idMatiere) {
        this.idMatiere = idMatiere;
    }
    public String getMatiereNom() {
        return matiereNom;
    }
    public void setMatiereNom(String matiereNom) {
        this.matiereNom = matiereNom;
    }
    public Integer getJoursRestants() {
        return joursRestants;
    }
    public void setJoursRestants(Integer joursRestants) {
        this.joursRestants = joursRestants;
    }
    
}
