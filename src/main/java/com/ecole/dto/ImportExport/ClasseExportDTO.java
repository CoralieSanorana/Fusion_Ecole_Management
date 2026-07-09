package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseExportDTO {
    private Integer id;
    private Integer niveauId;
    private Integer anneeScolaireId;
    private String nom;
    private Integer capaciteMax;
    private Integer salleId;
    private LocalDateTime createdAt;
}
