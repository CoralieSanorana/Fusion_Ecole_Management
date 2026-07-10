package com.ecole.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoefficientDTO {
    private Long id;
    private Long matiereId;
    private String matiereNom;
    private Long niveauId;
    private String niveauNom;
    private BigDecimal valeur;
}
