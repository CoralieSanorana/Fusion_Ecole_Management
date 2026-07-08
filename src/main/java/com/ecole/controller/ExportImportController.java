package com.ecole.controller;

import com.ecole.dto.ImportExport.ImportResultDTO;
import com.ecole.service.FullExportImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export-import")
@RequiredArgsConstructor
@Slf4j
public class ExportImportController {

    private final FullExportImportService fullExportImportService;

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToCSV() {
        try {
            log.info("Demande d'export complet CSV");
            byte[] data = fullExportImportService.exportAllData();

            String filename = "export_complet_ecole_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Erreur lors de l'export CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import/excel")
    public ResponseEntity<ImportResultDTO> importFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Demande d'import complet CSV: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                ImportResultDTO errorResult = new ImportResultDTO(false, "Le fichier est vide");
                errorResult.getErrors().add(new com.ecole.dto.ImportExport.ImportErrorDTO(
                    0, "Fichier", "upload", "Le fichier est vide", "", 
                    com.ecole.dto.ImportExport.ImportErrorDTO.ErrorSeverity.CRITICAL
                ));
                return ResponseEntity.badRequest().body(errorResult);
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".csv")) {
                ImportResultDTO errorResult = new ImportResultDTO(false, "Format de fichier invalide");
                errorResult.getErrors().add(new com.ecole.dto.ImportExport.ImportErrorDTO(
                    0, "Fichier", "format", "Le fichier doit être au format CSV (.csv)", filename,
                    com.ecole.dto.ImportExport.ImportErrorDTO.ErrorSeverity.CRITICAL
                ));
                return ResponseEntity.badRequest().body(errorResult);
            }

            ImportResultDTO result = fullExportImportService.importAllData(file);
            
            if (result.getSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'import CSV", e);
            ImportResultDTO errorResult = new ImportResultDTO(false, "Erreur lors de l'import: " + e.getMessage());
            errorResult.getErrors().add(new com.ecole.dto.ImportExport.ImportErrorDTO(
                0, "Système", "import", e.getMessage(), "",
                com.ecole.dto.ImportExport.ImportErrorDTO.ErrorSeverity.CRITICAL
            ));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}
