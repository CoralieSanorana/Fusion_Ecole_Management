package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportDataDTO {
    private List<EtablissementExportDTO> etablissements;
    private List<AnneeScolaireExportDTO> anneesScolaires;
    private List<NiveauExportDTO> niveaux;
    private List<SalleExportDTO> salles;
    private List<ClasseExportDTO> classes;
    private List<MatiereExportDTO> matieres;
    private String exportDate;
    private String exportVersion;
}
