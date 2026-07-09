package com.ecole.dto.Directeur;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DashboardTransactionDTO {
    private String transactionType;
    private String libellePrincipal;
    private String libelleSecondaire;
    private String referenceTransaction;
    private LocalDate datePaiement;
    private String dateLibelle;
    private BigDecimal montant;
    private String modePaiement;
    private String etudiantNom;
    private String classeNom;
    private String matricule;
    private String echeanceLabel;
}