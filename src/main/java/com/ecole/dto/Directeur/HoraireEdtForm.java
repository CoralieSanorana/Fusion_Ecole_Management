package com.ecole.dto.Directeur;

import lombok.Data;
import java.time.LocalTime;

@Data
public class HoraireEdtForm {
    private String id;
    private String libelle;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer ordre;
}
