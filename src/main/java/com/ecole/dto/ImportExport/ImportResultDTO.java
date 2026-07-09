package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    private Boolean success;
    private String message;
    private Integer totalRows;
    private Integer successfulRows;
    private Integer failedRows;
    private List<ImportErrorDTO> errors;
    private List<String> warnings;

    public ImportResultDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.totalRows = 0;
        this.successfulRows = 0;
        this.failedRows = 0;
    }
}
