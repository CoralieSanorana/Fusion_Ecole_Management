package com.ecole.dto.Directeur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreneauDTO {
    private Long id;
    private Integer jourSemaine;
    private Long horaireEdtId;
    private String matiereNom;
    private String matiereCode;
    private String classeNom;
    private String profNom;
    private String profPrenom;
    private String salleNom;
}
