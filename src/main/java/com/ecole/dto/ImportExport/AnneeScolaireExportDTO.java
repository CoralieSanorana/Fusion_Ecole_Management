package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnneeScolaireExportDTO {
    private Integer id;
    private Integer etablissementId;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean estActive;
    private LocalDateTime createdAt;
}
