package com.ecole.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AffectationDTO {
    private Long id;
    private Long professeurId;
    private String professeurNom;
    private Long matiereId;
    private String matiereNom;
    private Long classeId;
    private String classeNom;
    private Long anneeScolaireId;
    private String anneeScolaireNom;
    private BigDecimal heuresHebdo;
}
