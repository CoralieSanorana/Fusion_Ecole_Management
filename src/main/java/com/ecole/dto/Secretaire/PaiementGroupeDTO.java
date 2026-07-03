package com.ecole.dto.Secretaire;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class PaiementGroupeDTO {

    private Long inscriptionId;
    private Long echeanceId;
    private List<LignePaiement> lignes;

    @Getter @Setter
    public static class LignePaiement {
        private BigDecimal montant;
        private String modePaiement;
    }
}