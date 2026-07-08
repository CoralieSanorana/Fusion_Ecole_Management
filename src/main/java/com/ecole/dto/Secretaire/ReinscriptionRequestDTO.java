package com.ecole.dto.Secretaire;

import java.time.LocalDate;

public class ReinscriptionRequestDTO {
    private Long etudiantId;
    private Long anneeScolaireId;
    private Long classeId;
    private LocalDate dateInscription;
    private String statut = "active";

    public Long getEtudiantId() { return etudiantId; }
    public void setEtudiantId(Long etudiantId) { this.etudiantId = etudiantId; }

    public Long getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Long anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
