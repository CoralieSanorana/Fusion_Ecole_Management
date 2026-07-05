package com.ecole.dto.Directeur;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardMonthlyStatDTO {
    private String value;
    private String label;
    private BigDecimal recettes;
    private BigDecimal depensesSalairesProfesseurs;
    private BigDecimal beneficeNet;
    private long transactions;
    private long elevesPayes;
    private boolean selected;
}