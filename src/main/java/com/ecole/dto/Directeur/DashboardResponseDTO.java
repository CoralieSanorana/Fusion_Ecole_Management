package com.ecole.dto.Directeur;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponseDTO {
    private Long anneeScolaireId;
    private String anneeScolaireLibelle;
    private List<DashboardAnneeOptionDTO> annees;
    private List<DashboardMonthOptionDTO> mois;
    private String selectedMonth;
    private String selectedMonthLabel;
    private DashboardKpisDTO kpis;
    private List<DashboardMonthlyStatDTO> serieMensuelle;
    private List<DashboardTransactionDTO> transactions;
    private String resume;
}