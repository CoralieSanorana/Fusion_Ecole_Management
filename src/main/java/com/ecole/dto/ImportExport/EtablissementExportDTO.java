package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementExportDTO {
    private Integer id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String logoUrl;
    private Integer directeurId;
    private LocalDateTime createdAt;
}
