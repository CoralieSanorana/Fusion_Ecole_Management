package com.ecole.dto.ImportExport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDTO {
    private Integer rowNumber;
    private String tableName;
    private String fieldName;
    private String errorMessage;
    private String value;
    private ErrorSeverity severity;

    public enum ErrorSeverity {
        CRITICAL,
        WARNING,
        INFO
    }
}
