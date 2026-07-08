package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatiereExportDTO {
    private Integer id;
    private Integer etablissementId;
    private String nom;
    private String code;
    private LocalDateTime createdAt;
}
