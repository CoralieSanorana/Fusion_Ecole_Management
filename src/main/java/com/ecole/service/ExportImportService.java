package com.ecole.service;

import com.ecole.dto.ImportExport.*;
import com.ecole.entity.*;
import com.ecole.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportImportService {

    private final EtablissementRepository etablissementRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final NiveauRepository niveauRepository;
    private final SalleRepository salleRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public ExportDataDTO exportAllData() {
        log.info("Début de l'export de toutes les données");

        ExportDataDTO exportData = new ExportDataDTO();
        
        try {
            exportData.setEtablissements(mapEtablissements(etablissementRepository.findAll()));
            exportData.setAnneesScolaires(mapAnneesScolaires(anneeScolaireRepository.findAll()));
            exportData.setNiveaux(mapNiveaux(niveauRepository.findAll()));
            exportData.setSalles(mapSalles(salleRepository.findAll()));
            exportData.setMatieres(mapMatieres(matiereRepository.findAll()));
            exportData.setClasses(mapClasses(classeRepository.findAll()));
            
            exportData.setExportDate(LocalDateTime.now().format(DATE_FORMATTER));
            exportData.setExportVersion("1.0");

            log.info("Export terminé avec succès");
            return exportData;
        } catch (Exception e) {
            log.error("Erreur lors de l'export", e);
            throw new RuntimeException("Erreur lors de l'export des données: " + e.getMessage(), e);
        }
    }

    public byte[] exportToExcel(ExportDataDTO exportData) throws IOException {
        log.info("Génération du fichier Excel");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet summarySheet = workbook.createSheet("Résumé");
            createSummarySheet(summarySheet, exportData, createHelper);

            Sheet etablissementsSheet = workbook.createSheet("Etablissements");
            createEtablissementsSheet(etablissementsSheet, exportData.getEtablissements());

            Sheet anneesSheet = workbook.createSheet("Annees_Scolaires");
            createAnneesScolairesSheet(anneesSheet, exportData.getAnneesScolaires());

            Sheet niveauxSheet = workbook.createSheet("Niveaux");
            createNiveauxSheet(niveauxSheet, exportData.getNiveaux());

            Sheet sallesSheet = workbook.createSheet("Salles");
            createSallesSheet(sallesSheet, exportData.getSalles());

            Sheet matieresSheet = workbook.createSheet("Matieres");
            createMatieresSheet(matieresSheet, exportData.getMatieres());

            Sheet classesSheet = workbook.createSheet("Classes");
            createClassesSheet(classesSheet, exportData.getClasses());

            workbook.write(outputStream);
            log.info("Fichier Excel généré avec succès");
            return outputStream.toByteArray();
        }
    }

    private void createSummarySheet(Sheet sheet, ExportDataDTO exportData, CreationHelper createHelper) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Table");
        headerRow.createCell(1).setCellValue("Nombre d'enregistrements");

        int rowNum = 1;
        addSummaryRow(sheet, rowNum++, "Etablissements", exportData.getEtablissements().size());
        addSummaryRow(sheet, rowNum++, "Années Scolaires", exportData.getAnneesScolaires().size());
        addSummaryRow(sheet, rowNum++, "Niveaux", exportData.getNiveaux().size());
        addSummaryRow(sheet, rowNum++, "Salles", exportData.getSalles().size());
        addSummaryRow(sheet, rowNum++, "Matières", exportData.getMatieres().size());
        addSummaryRow(sheet, rowNum++, "Classes", exportData.getClasses().size());

        rowNum += 2;
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Date d'export");
        infoRow.createCell(1).setCellValue(exportData.getExportDate());

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Version");
        infoRow.createCell(1).setCellValue(exportData.getExportVersion());

        autoSizeColumns(sheet);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String tableName, int count) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(tableName);
        row.createCell(1).setCellValue(count);
    }

    private void createEtablissementsSheet(Sheet sheet, List<EtablissementExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nom", "Adresse", "Téléphone", "Email", "Logo URL", "Directeur ID", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (EtablissementExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getNom() != null ? dto.getNom() : "");
            row.createCell(2).setCellValue(dto.getAdresse() != null ? dto.getAdresse() : "");
            row.createCell(3).setCellValue(dto.getTelephone() != null ? dto.getTelephone() : "");
            row.createCell(4).setCellValue(dto.getEmail() != null ? dto.getEmail() : "");
            row.createCell(5).setCellValue(dto.getLogoUrl() != null ? dto.getLogoUrl() : "");
            row.createCell(6).setCellValue(dto.getDirecteurId() != null ? String.valueOf(dto.getDirecteurId()) : "");
            row.createCell(7).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void createAnneesScolairesSheet(Sheet sheet, List<AnneeScolaireExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Etablissement ID", "Libellé", "Date Début", "Date Fin", "Active", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (AnneeScolaireExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getEtablissementId() != null ? String.valueOf(dto.getEtablissementId()) : "");
            row.createCell(2).setCellValue(dto.getLibelle() != null ? dto.getLibelle() : "");
            row.createCell(3).setCellValue(dto.getDateDebut() != null ? dto.getDateDebut().toString() : "");
            row.createCell(4).setCellValue(dto.getDateFin() != null ? dto.getDateFin().toString() : "");
            row.createCell(5).setCellValue(dto.getEstActive() != null ? dto.getEstActive() : false);
            row.createCell(6).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void createNiveauxSheet(Sheet sheet, List<NiveauExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Etablissement ID", "Libellé", "Ordre", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (NiveauExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getEtablissementId() != null ? String.valueOf(dto.getEtablissementId()) : "");
            row.createCell(2).setCellValue(dto.getLibelle() != null ? dto.getLibelle() : "");
            row.createCell(3).setCellValue(dto.getOrdre() != null ? String.valueOf(dto.getOrdre()) : "");
            row.createCell(4).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void createSallesSheet(Sheet sheet, List<SalleExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Etablissement ID", "Nom", "Capacité", "Type", "Active", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (SalleExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getEtablissementId() != null ? String.valueOf(dto.getEtablissementId()) : "");
            row.createCell(2).setCellValue(dto.getNom() != null ? dto.getNom() : "");
            row.createCell(3).setCellValue(dto.getCapacite() != null ? String.valueOf(dto.getCapacite()) : "");
            row.createCell(4).setCellValue(dto.getType() != null ? dto.getType() : "");
            row.createCell(5).setCellValue(dto.getIsActive() != null ? dto.getIsActive() : false);
            row.createCell(6).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void createMatieresSheet(Sheet sheet, List<MatiereExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Etablissement ID", "Nom", "Code", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (MatiereExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getEtablissementId() != null ? String.valueOf(dto.getEtablissementId()) : "");
            row.createCell(2).setCellValue(dto.getNom() != null ? dto.getNom() : "");
            row.createCell(3).setCellValue(dto.getCode() != null ? dto.getCode() : "");
            row.createCell(4).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void createClassesSheet(Sheet sheet, List<ClasseExportDTO> data) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Niveau ID", "Année Scolaire ID", "Nom", "Capacité Max", "Salle ID", "Créé le"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (ClasseExportDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getId() != null ? String.valueOf(dto.getId()) : "");
            row.createCell(1).setCellValue(dto.getNiveauId() != null ? String.valueOf(dto.getNiveauId()) : "");
            row.createCell(2).setCellValue(dto.getAnneeScolaireId() != null ? String.valueOf(dto.getAnneeScolaireId()) : "");
            row.createCell(3).setCellValue(dto.getNom() != null ? dto.getNom() : "");
            row.createCell(4).setCellValue(dto.getCapaciteMax() != null ? String.valueOf(dto.getCapaciteMax()) : "");
            row.createCell(5).setCellValue(dto.getSalleId() != null ? String.valueOf(dto.getSalleId()) : "");
            row.createCell(6).setCellValue(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATE_FORMATTER) : "");
        }
        autoSizeColumns(sheet);
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private List<EtablissementExportDTO> mapEtablissements(List<Etablissement> entities) {
        List<EtablissementExportDTO> dtos = new ArrayList<>();
        for (Etablissement entity : entities) {
            dtos.add(new EtablissementExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getNom(),
                entity.getAdresse(),
                entity.getTelephone(),
                entity.getEmail(),
                entity.getLogoUrl(),
                null,
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    private List<AnneeScolaireExportDTO> mapAnneesScolaires(List<AnneeScolaire> entities) {
        List<AnneeScolaireExportDTO> dtos = new ArrayList<>();
        for (AnneeScolaire entity : entities) {
            dtos.add(new AnneeScolaireExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getEtablissement() != null ? entity.getEtablissement().getId().intValue() : null,
                entity.getLibelle(),
                entity.getDateDebut(),
                entity.getDateFin(),
                entity.getEstActive(),
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    private List<NiveauExportDTO> mapNiveaux(List<Niveau> entities) {
        List<NiveauExportDTO> dtos = new ArrayList<>();
        for (Niveau entity : entities) {
            dtos.add(new NiveauExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getEtablissement() != null ? entity.getEtablissement().getId().intValue() : null,
                entity.getLibelle(),
                entity.getOrdre(),
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    private List<SalleExportDTO> mapSalles(List<Salle> entities) {
        List<SalleExportDTO> dtos = new ArrayList<>();
        for (Salle entity : entities) {
            dtos.add(new SalleExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getEtablissement() != null ? entity.getEtablissement().getId().intValue() : null,
                entity.getNom(),
                entity.getCapacite(),
                entity.getType(),
                entity.getIsActive(),
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    private List<MatiereExportDTO> mapMatieres(List<Matiere> entities) {
        List<MatiereExportDTO> dtos = new ArrayList<>();
        for (Matiere entity : entities) {
            dtos.add(new MatiereExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getEtablissement() != null ? entity.getEtablissement().getId().intValue() : null,
                entity.getNom(),
                entity.getCode(),
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    private List<ClasseExportDTO> mapClasses(List<Classe> entities) {
        List<ClasseExportDTO> dtos = new ArrayList<>();
        for (Classe entity : entities) {
            dtos.add(new ClasseExportDTO(
                entity.getId() != null ? entity.getId().intValue() : null,
                entity.getNiveauId() != null ? entity.getNiveauId().intValue() : null,
                entity.getAnneeScolaireId() != null ? entity.getAnneeScolaireId().intValue() : null,
                entity.getNom(),
                entity.getCapaciteMax(),
                null,
                entity.getCreatedAt()
            ));
        }
        return dtos;
    }

    @Transactional
    public ImportResultDTO importFromExcel(MultipartFile file) {
        log.info("Début de l'import depuis Excel: {}", file.getOriginalFilename());
        ImportResultDTO result = new ImportResultDTO(true, "Import en cours");

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            int totalRows = 0;
            int successfulRows = 0;
            int failedRows = 0;

            // Import dans l'ordre respectant les dépendances
            // 1. Etablissements
            Sheet etablissementsSheet = workbook.getSheet("Etablissements");
            if (etablissementsSheet != null) {
                ImportResultDTO etablissementsResult = importEtablissements(etablissementsSheet);
                totalRows += etablissementsResult.getTotalRows();
                successfulRows += etablissementsResult.getSuccessfulRows();
                failedRows += etablissementsResult.getFailedRows();
                result.getErrors().addAll(etablissementsResult.getErrors());
                result.getWarnings().addAll(etablissementsResult.getWarnings());
            }

            // 2. Années Scolaires
            Sheet anneesSheet = workbook.getSheet("Annees_Scolaires");
            if (anneesSheet != null) {
                ImportResultDTO anneesResult = importAnneesScolaires(anneesSheet);
                totalRows += anneesResult.getTotalRows();
                successfulRows += anneesResult.getSuccessfulRows();
                failedRows += anneesResult.getFailedRows();
                result.getErrors().addAll(anneesResult.getErrors());
                result.getWarnings().addAll(anneesResult.getWarnings());
            }

            // 3. Niveaux
            Sheet niveauxSheet = workbook.getSheet("Niveaux");
            if (niveauxSheet != null) {
                ImportResultDTO niveauxResult = importNiveaux(niveauxSheet);
                totalRows += niveauxResult.getTotalRows();
                successfulRows += niveauxResult.getSuccessfulRows();
                failedRows += niveauxResult.getFailedRows();
                result.getErrors().addAll(niveauxResult.getErrors());
                result.getWarnings().addAll(niveauxResult.getWarnings());
            }

            // 4. Salles
            Sheet sallesSheet = workbook.getSheet("Salles");
            if (sallesSheet != null) {
                ImportResultDTO sallesResult = importSalles(sallesSheet);
                totalRows += sallesResult.getTotalRows();
                successfulRows += sallesResult.getSuccessfulRows();
                failedRows += sallesResult.getFailedRows();
                result.getErrors().addAll(sallesResult.getErrors());
                result.getWarnings().addAll(sallesResult.getWarnings());
            }

            // 5. Matières
            Sheet matieresSheet = workbook.getSheet("Matieres");
            if (matieresSheet != null) {
                ImportResultDTO matieresResult = importMatieres(matieresSheet);
                totalRows += matieresResult.getTotalRows();
                successfulRows += matieresResult.getSuccessfulRows();
                failedRows += matieresResult.getFailedRows();
                result.getErrors().addAll(matieresResult.getErrors());
                result.getWarnings().addAll(matieresResult.getWarnings());
            }

            // 6. Classes
            Sheet classesSheet = workbook.getSheet("Classes");
            if (classesSheet != null) {
                ImportResultDTO classesResult = importClasses(classesSheet);
                totalRows += classesResult.getTotalRows();
                successfulRows += classesResult.getSuccessfulRows();
                failedRows += classesResult.getFailedRows();
                result.getErrors().addAll(classesResult.getErrors());
                result.getWarnings().addAll(classesResult.getWarnings());
            }

            result.setTotalRows(totalRows);
            result.setSuccessfulRows(successfulRows);
            result.setFailedRows(failedRows);

            if (failedRows > 0) {
                result.setSuccess(false);
                result.setMessage("Import terminé avec erreurs: " + failedRows + " échec(s) sur " + totalRows + " ligne(s)");
            } else {
                result.setSuccess(true);
                result.setMessage("Import terminé avec succès: " + successfulRows + " ligne(s) importée(s)");
            }

            log.info("Import terminé: {} succès, {} échecs", successfulRows, failedRows);
            return result;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier Excel", e);
            result.setSuccess(false);
            result.setMessage("Erreur lors de la lecture du fichier: " + e.getMessage());
            result.getErrors().add(new ImportErrorDTO(0, "Fichier", "lecture", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
            return result;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'import", e);
            result.setSuccess(false);
            result.setMessage("Erreur inattendue: " + e.getMessage());
            result.getErrors().add(new ImportErrorDTO(0, "Système", "import", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
            return result;
        }
    }

    private ImportResultDTO importEtablissements(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import établissements");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String nom = getCellValueAsString(row.getCell(1));
                if (nom == null || nom.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Etablissements", "nom", "Le nom est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                Etablissement etablissement = new Etablissement();
                etablissement.setNom(nom.trim());
                etablissement.setAdresse(getCellValueAsString(row.getCell(2)));
                etablissement.setTelephone(getCellValueAsString(row.getCell(3)));
                etablissement.setEmail(getCellValueAsString(row.getCell(4)));
                etablissement.setLogoUrl(getCellValueAsString(row.getCell(5)));
                etablissement.setCreatedAt(LocalDateTime.now());

                etablissementRepository.save(etablissement);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des établissements", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Etablissements", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private ImportResultDTO importAnneesScolaires(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import années scolaires");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String libelle = getCellValueAsString(row.getCell(2));
                if (libelle == null || libelle.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Annees_Scolaires", "libelle", "Le libellé est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String etablissementIdStr = getCellValueAsString(row.getCell(1));
                Long etablissementId = null;
                if (etablissementIdStr != null && !etablissementIdStr.trim().isEmpty()) {
                    try {
                        etablissementId = Long.parseLong(etablissementIdStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": ID établissement invalide, sera ignoré");
                    }
                }

                Etablissement etablissement = null;
                if (etablissementId != null) {
                    etablissement = etablissementRepository.findById(etablissementId).orElse(null);
                    if (etablissement == null) {
                        result.getWarnings().add("Ligne " + i + ": Établissement ID " + etablissementId + " non trouvé, sera ignoré");
                    }
                }

                String dateDebutStr = getCellValueAsString(row.getCell(3));
                String dateFinStr = getCellValueAsString(row.getCell(4));

                LocalDate dateDebut = null;
                LocalDate dateFin = null;
                try {
                    if (dateDebutStr != null && !dateDebutStr.trim().isEmpty()) {
                        dateDebut = LocalDate.parse(dateDebutStr);
                    }
                    if (dateFinStr != null && !dateFinStr.trim().isEmpty()) {
                        dateFin = LocalDate.parse(dateFinStr);
                    }
                } catch (DateTimeParseException e) {
                    result.getErrors().add(new ImportErrorDTO(i, "Annees_Scolaires", "date", "Format de date invalide (attendu: yyyy-MM-dd)", dateDebutStr + "/" + dateFinStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                AnneeScolaire annee = new AnneeScolaire();
                annee.setEtablissement(etablissement);
                annee.setLibelle(libelle.trim());
                annee.setDateDebut(dateDebut);
                annee.setDateFin(dateFin);
                annee.setEstActive(getCellValueAsBoolean(row.getCell(5)));
                annee.setCreatedAt(LocalDateTime.now());

                anneeScolaireRepository.save(annee);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des années scolaires", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Annees_Scolaires", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private ImportResultDTO importNiveaux(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import niveaux");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String libelle = getCellValueAsString(row.getCell(2));
                if (libelle == null || libelle.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Niveaux", "libelle", "Le libellé est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String etablissementIdStr = getCellValueAsString(row.getCell(1));
                Long etablissementId = null;
                if (etablissementIdStr != null && !etablissementIdStr.trim().isEmpty()) {
                    try {
                        etablissementId = Long.parseLong(etablissementIdStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": ID établissement invalide, sera ignoré");
                    }
                }

                Etablissement etablissement = null;
                if (etablissementId != null) {
                    etablissement = etablissementRepository.findById(etablissementId).orElse(null);
                    if (etablissement == null) {
                        result.getWarnings().add("Ligne " + i + ": Établissement ID " + etablissementId + " non trouvé, sera ignoré");
                    }
                }

                String ordreStr = getCellValueAsString(row.getCell(3));
                Integer ordre = null;
                if (ordreStr != null && !ordreStr.trim().isEmpty()) {
                    try {
                        ordre = Integer.parseInt(ordreStr);
                    } catch (NumberFormatException e) {
                        result.getErrors().add(new ImportErrorDTO(i, "Niveaux", "ordre", "L'ordre doit être un nombre entier", ordreStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                        result.setFailedRows(result.getFailedRows() + 1);
                        continue;
                    }
                }

                Niveau niveau = new Niveau();
                niveau.setEtablissement(etablissement);
                niveau.setLibelle(libelle.trim());
                niveau.setOrdre(ordre);
                niveau.setCreatedAt(LocalDateTime.now());

                niveauRepository.save(niveau);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des niveaux", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Niveaux", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private ImportResultDTO importSalles(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import salles");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String nom = getCellValueAsString(row.getCell(2));
                if (nom == null || nom.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Salles", "nom", "Le nom est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String etablissementIdStr = getCellValueAsString(row.getCell(1));
                Long etablissementId = null;
                if (etablissementIdStr != null && !etablissementIdStr.trim().isEmpty()) {
                    try {
                        etablissementId = Long.parseLong(etablissementIdStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": ID établissement invalide, sera ignoré");
                    }
                }

                Etablissement etablissement = null;
                if (etablissementId != null) {
                    etablissement = etablissementRepository.findById(etablissementId).orElse(null);
                    if (etablissement == null) {
                        result.getWarnings().add("Ligne " + i + ": Établissement ID " + etablissementId + " non trouvé, sera ignoré");
                    }
                }

                String capaciteStr = getCellValueAsString(row.getCell(3));
                Integer capacite = null;
                if (capaciteStr != null && !capaciteStr.trim().isEmpty()) {
                    try {
                        capacite = Integer.parseInt(capaciteStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": Capacité invalide, sera ignorée");
                    }
                }

                Salle salle = new Salle();
                salle.setEtablissement(etablissement);
                salle.setNom(nom.trim());
                salle.setCapacite(capacite);
                salle.setType(getCellValueAsString(row.getCell(4)));
                salle.setIsActive(getCellValueAsBoolean(row.getCell(5)));
                salle.setCreatedAt(LocalDateTime.now());

                salleRepository.save(salle);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des salles", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Salles", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private ImportResultDTO importMatieres(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import matières");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String nom = getCellValueAsString(row.getCell(2));
                if (nom == null || nom.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Matieres", "nom", "Le nom est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String etablissementIdStr = getCellValueAsString(row.getCell(1));
                Long etablissementId = null;
                if (etablissementIdStr != null && !etablissementIdStr.trim().isEmpty()) {
                    try {
                        etablissementId = Long.parseLong(etablissementIdStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": ID établissement invalide, sera ignoré");
                    }
                }

                Etablissement etablissement = null;
                if (etablissementId != null) {
                    etablissement = etablissementRepository.findById(etablissementId).orElse(null);
                    if (etablissement == null) {
                        result.getWarnings().add("Ligne " + i + ": Établissement ID " + etablissementId + " non trouvé, sera ignoré");
                    }
                }

                Matiere matiere = new Matiere();
                matiere.setEtablissement(etablissement);
                matiere.setNom(nom.trim());
                matiere.setCode(getCellValueAsString(row.getCell(3)));
                matiere.setCreatedAt(LocalDateTime.now());

                matiereRepository.save(matiere);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des matières", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Matieres", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private ImportResultDTO importClasses(Sheet sheet) {
        ImportResultDTO result = new ImportResultDTO(true, "Import classes");
        int totalRows = sheet.getPhysicalNumberOfRows() - 1;
        result.setTotalRows(totalRows);

        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String nom = getCellValueAsString(row.getCell(3));
                if (nom == null || nom.trim().isEmpty()) {
                    result.getErrors().add(new ImportErrorDTO(i, "Classes", "nom", "Le nom est obligatoire", "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String niveauIdStr = getCellValueAsString(row.getCell(1));
                Long niveauId = null;
                if (niveauIdStr != null && !niveauIdStr.trim().isEmpty()) {
                    try {
                        niveauId = Long.parseLong(niveauIdStr);
                    } catch (NumberFormatException e) {
                        result.getErrors().add(new ImportErrorDTO(i, "Classes", "niveauId", "ID niveau invalide", niveauIdStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                        result.setFailedRows(result.getFailedRows() + 1);
                        continue;
                    }
                }

                Niveau niveau = niveauRepository.findById(niveauId).orElse(null);
                if (niveau == null) {
                    result.getErrors().add(new ImportErrorDTO(i, "Classes", "niveauId", "Niveau non trouvé", niveauIdStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String anneeScolaireIdStr = getCellValueAsString(row.getCell(2));
                Long anneeScolaireId = null;
                if (anneeScolaireIdStr != null && !anneeScolaireIdStr.trim().isEmpty()) {
                    try {
                        anneeScolaireId = Long.parseLong(anneeScolaireIdStr);
                    } catch (NumberFormatException e) {
                        result.getErrors().add(new ImportErrorDTO(i, "Classes", "anneeScolaireId", "ID année scolaire invalide", anneeScolaireIdStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                        result.setFailedRows(result.getFailedRows() + 1);
                        continue;
                    }
                }

                AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(anneeScolaireId).orElse(null);
                if (anneeScolaire == null) {
                    result.getErrors().add(new ImportErrorDTO(i, "Classes", "anneeScolaireId", "Année scolaire non trouvée", anneeScolaireIdStr, ImportErrorDTO.ErrorSeverity.CRITICAL));
                    result.setFailedRows(result.getFailedRows() + 1);
                    continue;
                }

                String capaciteMaxStr = getCellValueAsString(row.getCell(4));
                Integer capaciteMax = null;
                if (capaciteMaxStr != null && !capaciteMaxStr.trim().isEmpty()) {
                    try {
                        capaciteMax = Integer.parseInt(capaciteMaxStr);
                    } catch (NumberFormatException e) {
                        result.getWarnings().add("Ligne " + i + ": Capacité max invalide, sera ignorée");
                    }
                }

                Classe classe = new Classe();
                classe.setNiveau(niveau);
                classe.setAnneeScolaire(anneeScolaire);
                classe.setNom(nom.trim());
                classe.setCapaciteMax(capaciteMax);
                classe.setCreatedAt(LocalDateTime.now());

                classeRepository.save(classe);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

            } catch (Exception e) {
                log.error("Erreur lors de l'import de la ligne {} des classes", i, e);
                result.getErrors().add(new ImportErrorDTO(i, "Classes", "row", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                result.setFailedRows(result.getFailedRows() + 1);
            }
        }

        return result;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("oui") || value.equals("1")) {
                    return true;
                }
                if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("non") || value.equals("0")) {
                    return false;
                }
                return null;
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            default:
                return null;
        }
    }
}
