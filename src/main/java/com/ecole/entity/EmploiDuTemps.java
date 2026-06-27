package com.ecole.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "emploi_du_temps")
public class EmploiDuTemps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "affectation_id")
    private AffectationEnseignement affectation;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "horaire_edt_id")
    private HoraireEdt horaireEdt;

    private Integer jourSemaine;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private LocalDate dateDebutValidite;
    private LocalDate dateFinValidite;

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
    public Salle getSalle() {
        return salle;
    }
    public void setSalle(Salle salle) {
        this.salle = salle;
    }
    public HoraireEdt getHoraireEdt() {
        return horaireEdt;
    }
    public void setHoraireEdt(HoraireEdt horaireEdt) {
        this.horaireEdt = horaireEdt;
    }
    public Integer getJourSemaine() {
        return jourSemaine;
    }
    public void setJourSemaine(Integer jourSemaine) {
        this.jourSemaine = jourSemaine;
    }
    public LocalTime getHeureDebut() {
        return heureDebut;
    }
    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }
    public LocalTime getHeureFin() {
        return heureFin;
    }
    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }   
    public LocalDate getDateDebutValidite() {
        return dateDebutValidite;
    }
    public void setDateDebutValidite(LocalDate dateDebutValidite) {
        this.dateDebutValidite = dateDebutValidite;
    }
    public LocalDate getDateFinValidite() {
        return dateFinValidite;
    }
    public void setDateFinValidite(LocalDate dateFinValidite) {
        this.dateFinValidite = dateFinValidite;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getAffectationId(){
        return affectation.getId();
    }
    public void setAffectationId(Long affectationId){
        this.affectation.setId(affectationId);
    }
    public Long getSalleId(){
        return salle.getId();
    }
    public void setSalleId(Long salleId){
        this.salle.setId(salleId);
    }
    public Long getHoraireEdtId(){
        return horaireEdt.getId();
    }
    public void setHoraireEdtId(Long horaireEdtId){
        this.horaireEdt.setId(horaireEdtId);
    }
}
