package com.ecole.dto.Directeur;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardKpisDTO {
    private BigDecimal recettesTotal;
    private BigDecimal depensesSalairesProfesseurs;
    private BigDecimal beneficeNet;
    private BigDecimal ticketMoyen;
    private long transactionsTotal;
    private long elevesPayes;
    private long elevesImpayes;
    private long elevesTotal;
    private long professeursActifs;
}