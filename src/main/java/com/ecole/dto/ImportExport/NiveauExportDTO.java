package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauExportDTO {
    private Integer id;
    private Integer etablissementId;
    private String libelle;
    private Integer ordre;
    private LocalDateTime createdAt;
}
