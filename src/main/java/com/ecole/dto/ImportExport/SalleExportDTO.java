package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleExportDTO {
    private Integer id;
    private Integer etablissementId;
    private String nom;
    private Integer capacite;
    private String type;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
