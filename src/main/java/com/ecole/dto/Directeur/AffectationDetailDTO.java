package com.ecole.dto.Directeur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AffectationDetailDTO {
    private Long id;
    private String niveauLibelle;
    private String classeNom;
    private String matiereNom;
    private String matiereCode;
    private String profNom;
    private String profPrenom;
}
