package com.ecole.dto.Secretaire;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class PaiementHistoriqueDTO {
    private String moisLabel;
    private LocalDate datePaiement;
    private BigDecimal montant;
    private String statut;

    // Ajouter cette méthode
    public boolean isPaye() {
        return "Payé".equals(statut);
    }
}