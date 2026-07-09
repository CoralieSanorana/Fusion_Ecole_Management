package com.ecole.service;

import com.ecole.dto.ImportExport.ImportResultDTO;
import com.ecole.dto.ImportExport.ImportErrorDTO;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullExportImportService {

    private final EntityManager entityManager;

    // Auto-injection paresseuse de soi-même : nécessaire pour que l'annotation
    // @Transactional(REQUIRES_NEW) sur importTableData() soit bien interceptée
    // par le proxy Spring (un appel "this.importTableData(...)" l'ignorerait).
    @Autowired
    @Lazy
    private FullExportImportService self;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int BATCH_SIZE = 500;

    // Ordre d'import pour respecter les dépendances
    private static final List<String> IMPORT_ORDER = Arrays.asList(
        "Role", "Permission", "RolePermission",
        "User", "UserRole",
        "Etablissement",
        "AnneeScolaire", "Niveau", "Salle", "Matiere",
        "Coefficient", "Periode",
        "ProfilsDirecteurs", "ProfilsSecretariat", "ProfilsComptables",
        "ProfilsProfesseurs", "ProfilsEtudiant", "ProfilsParent",
        "EtudiantParent",
        "Classe", "GrilleTarifaire",
        "AffectationEnseignement", "TitulaireClasse",
        "EmploiDuTemps", "HoraireEdt",
        "Inscription", "Echeancier", "Echeance",
        "Seance", "Absence",
        "Note", "Moyenne",
        "Paiement",
        "TypeDepense", "Depense",
        "CategoriesDepenses", "Fournisseur", "ContratsCharges", "EcheancesContrats",
        "PrevisionsDepenses", "Budget",
        "Evenement", "EvenementInstance",
        "NotificationType", "Notification",
        "Document", "DemandeModificationDossier",
        "AuditLog",
        "TypeFichier", "SupportCours",
        "Devoir", "Lecon",
        "TypesContratsEmployes", "ContratsEmployes",
        "Actualite"
    );

    public byte[] exportAllData() throws IOException {
        log.info("Début de l'export complet de toutes les tables en CSV");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {

            Metamodel metamodel = entityManager.getMetamodel();
            Set<EntityType<?>> entities = metamodel.getEntities();

            // Écrire le résumé
            String[] summaryHeader = {"TABLE", "NOMBRE_ENREGISTREMENTS", "STATUT"};
            writer.writeNext(summaryHeader);

            for (EntityType<?> entityType : entities) {
                String entityName = entityType.getJavaType().getSimpleName();
                String tableName = entityType.getName();
                
                long count = 0;
                String status = "OK";
                try {
                    count = entityManager.createQuery(
                        "SELECT COUNT(e) FROM " + entityName + " e", Long.class
                    ).getSingleResult();
                } catch (Exception e) {
                    log.warn("Impossible de compter les enregistrements pour {}: {}", entityName, e.getMessage());
                    status = "Table inexistante";
                }

                String[] summaryRow = {tableName, String.valueOf(count), status};
                writer.writeNext(summaryRow);

                // Si la table existe et a des données, l'exporter
                if ("OK".equals(status) && count > 0) {
                    try {
                        exportEntityToCSV(writer, entityType, entityName);
                    } catch (Exception e) {
                        log.error("Erreur lors de l'export CSV de l'entité {}", entityName, e);
                    }
                }
            }

            log.info("Export CSV terminé avec succès");
            return outputStream.toByteArray();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void exportEntityToCSV(CSVWriter writer, EntityType<?> entityType, String entityName) {
        try {
            // Récupérer toutes les données
            List<?> data = entityManager.createQuery(
                "SELECT e FROM " + entityName + " e"
            ).getResultList();

            if (data.isEmpty()) {
                return;
            }

            // Écrire le marqueur de table
            writer.writeNext(new String[]{"TABLE_START", entityName});

            // Créer l'en-tête avec les noms des champs
            List<Field> fields = getAllFields(entityType.getJavaType());
            String[] headers = fields.stream().map(Field::getName).toArray(String[]::new);
            writer.writeNext(headers);

            // Remplir les données
            for (Object entity : data) {
                String[] row = new String[fields.size()];
                for (int j = 0; j < fields.size(); j++) {
                    try {
                        fields.get(j).setAccessible(true);
                        Object value = fields.get(j).get(entity);
                        row[j] = formatValue(value);
                    } catch (IllegalAccessException e) {
                        row[j] = "";
                    }
                }
                writer.writeNext(row);
            }

            // Marqueur de fin de table
            writer.writeNext(new String[]{"TABLE_END", entityName});
            log.info("Exporté {} enregistrements pour la table {} en CSV", data.size(), entityName);

        } catch (Exception e) {
            log.error("Erreur lors de l'export CSV de l'entité {}", entityName, e);
        }
    }

    private void createSummarySheet(Set<EntityType<?>> entities) {
        // Cette méthode n'est plus nécessaire pour CSV
    }

    private void exportEntity(EntityType<?> entityType, String entityName) {
        // Cette méthode n'est plus nécessaire pour CSV
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        
        // Gérer les proxies Hibernate
        if (value.getClass().getName().contains("$HibernateProxy$")) {
            try {
                // Essayer de récupérer l'ID si c'est une entité proxy
                Object id = value.getClass().getMethod("getId").invoke(value);
                if (id != null) {
                    return id.toString();
                }
            } catch (Exception e) {
                log.debug("Impossible de récupérer l'ID du proxy: {}", e.getMessage());
            }
            return "[Proxy]";
        }
        
        // Gérer les collections
        if (value instanceof java.util.Collection) {
            java.util.Collection<?> collection = (java.util.Collection<?>) value;
            if (collection.isEmpty()) return "[]";
            return "[" + collection.size() + " éléments]";
        }
        
        // Gérer les entités JPA (objets qui ne sont pas des types primitifs)
        if (!isSimpleType(value.getClass())) {
            try {
                // Essayer de récupérer l'ID
                try {
                    Object id = value.getClass().getMethod("getId").invoke(value);
                    if (id != null) {
                        return id.toString();
                    }
                } catch (NoSuchMethodException e) {
                    // Pas de méthode getId, utiliser toString
                }
                return value.toString();
            } catch (Exception e) {
                log.debug("Erreur lors de la conversion de l'entité: {}", e.getMessage());
                return "[Entité]";
            }
        }
        
        // Types de données standards
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_FORMATTER);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(LOCAL_DATE_FORMATTER);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        
        return value.toString();
    }
    
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Integer.class || clazz == int.class ||
               clazz == Long.class || clazz == long.class ||
               clazz == Double.class || clazz == double.class ||
               clazz == Float.class || clazz == float.class ||
               clazz == Boolean.class || clazz == boolean.class ||
               clazz == Character.class || clazz == char.class ||
               clazz == Short.class || clazz == short.class ||
               clazz == Byte.class || clazz == byte.class ||
               BigDecimal.class.isAssignableFrom(clazz) ||
               LocalDateTime.class.isAssignableFrom(clazz) ||
               LocalDate.class.isAssignableFrom(clazz);
    }

    // Pas de @Transactional ici : chaque table est importée dans SA PROPRE
    // transaction (voir importTableData -> REQUIRES_NEW). Ainsi, une erreur
    // sur une table (ou une ligne) n'annule plus tout l'import précédent.
    public ImportResultDTO importAllData(MultipartFile file) {
        log.info("Début de l'import complet depuis CSV: {}", file.getOriginalFilename());
        ImportResultDTO result = new ImportResultDTO(true, "Import en cours");

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            int totalRows = 0;
            int successfulRows = 0;
            int failedRows = 0;

            String[] nextLine;
            String currentTable = null;
            List<String[]> currentData = new ArrayList<>();
            String[] currentHeaders = null;

            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length >= 2 && "TABLE_START".equals(nextLine[0])) {
                    // Nouvelle table
                    if (currentTable != null && !currentData.isEmpty()) {
                        // Importer la table précédente
                        ImportResultDTO tableResult = self.importTableData(currentTable, currentHeaders, currentData);
                        totalRows += tableResult.getTotalRows();
                        successfulRows += tableResult.getSuccessfulRows();
                        failedRows += tableResult.getFailedRows();
                        result.getErrors().addAll(tableResult.getErrors());
                        result.getWarnings().addAll(tableResult.getWarnings());
                    }
                    currentTable = nextLine[1];
                    currentData.clear();
                    currentHeaders = null;
                } else if (nextLine.length >= 2 && "TABLE_END".equals(nextLine[0])) {
                    // Fin de table
                    if (currentTable != null && !currentData.isEmpty()) {
                        ImportResultDTO tableResult = self.importTableData(currentTable, currentHeaders, currentData);
                        totalRows += tableResult.getTotalRows();
                        successfulRows += tableResult.getSuccessfulRows();
                        failedRows += tableResult.getFailedRows();
                        result.getErrors().addAll(tableResult.getErrors());
                        result.getWarnings().addAll(tableResult.getWarnings());
                    }
                    currentTable = null;
                    currentData.clear();
                    currentHeaders = null;
                } else if (currentTable != null) {
                    if (currentHeaders == null) {
                        currentHeaders = nextLine;
                    } else {
                        currentData.add(nextLine);
                    }
                }
            }

            // Importer la dernière table si nécessaire
            if (currentTable != null && !currentData.isEmpty()) {
                ImportResultDTO tableResult = self.importTableData(currentTable, currentHeaders, currentData);
                totalRows += tableResult.getTotalRows();
                successfulRows += tableResult.getSuccessfulRows();
                failedRows += tableResult.getFailedRows();
                result.getErrors().addAll(tableResult.getErrors());
                result.getWarnings().addAll(tableResult.getWarnings());
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

            log.info("Import complet terminé: {} succès, {} échecs", successfulRows, failedRows);
            return result;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier CSV", e);
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

    /**
     * Importe les données d'une table via INSERT natif en lots (JDBC batch).
     * Contrairement à entityManager.persist(), ceci préserve les IDs d'origine
     * du CSV (nécessaire car ils sont référencés par les FK des autres tables)
     * sans entrer en conflit avec la génération IDENTITY/SERIAL de Postgres.
     *
     * Chaque table est importée dans SA PROPRE transaction (REQUIRES_NEW) :
     * une erreur sur une table n'annule plus les tables déjà importées avant elle.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportResultDTO importTableData(String tableName, String[] headers, List<String[]> data) {
        ImportResultDTO result = new ImportResultDTO(true, "Import " + tableName);
        int totalRows = data.size();
        result.setTotalRows(totalRows);

        if (totalRows <= 0 || headers == null) {
            return result;
        }

        Class<?> entityClass = findEntityClass(tableName);
        if (entityClass == null) {
            result.getWarnings().add("Classe non trouvée pour l'entité: " + tableName);
            return result;
        }

        String physicalTableName = getTableName(entityClass);
        if (physicalTableName == null) {
            result.getErrors().add(new ImportErrorDTO(0, tableName, "entity",
                "Impossible de déterminer la table physique (ajouter @Table sur l'entité)", "",
                ImportErrorDTO.ErrorSeverity.CRITICAL));
            return result;
        }

        // "vue_..." est une VUE SQL (CREATE VIEW), pas une table : elle ne peut
        // pas recevoir d'INSERT. On la saute proprement plutôt que d'échouer.
        // Si d'autres vues existent avec un autre préfixe, les ajouter ici.
        if (physicalTableName.toLowerCase().startsWith("vue_")) {
            result.getWarnings().add(tableName + " est une vue SQL (" + physicalTableName +
                "), ignorée à l'import car elle est recalculée automatiquement.");
            return result;
        }

        List<Field> allFields = getAllFields(entityClass);

        // Ne retenir que les colonnes simples présentes à la fois dans le CSV
        // et dans l'entité (on ignore collections / relations JPA complexes,
        // comme le faisait déjà le code d'origine).
        List<Field> columnFields = new ArrayList<>();
        List<Integer> columnIndexes = new ArrayList<>();
        for (int j = 0; j < headers.length; j++) {
            String columnName = headers[j];
            for (Field field : allFields) {
                if (field.getName().equals(columnName)) {
                    if (java.util.Collection.class.isAssignableFrom(field.getType()) ||
                        !isSimpleType(field.getType())) {
                        break;
                    }
                    field.setAccessible(true);
                    columnFields.add(field);
                    columnIndexes.add(j);
                    break;
                }
            }
        }

        if (columnFields.isEmpty()) {
            result.getWarnings().add("Aucune colonne exploitable trouvée pour: " + tableName);
            return result;
        }

        String sql = buildInsertSql(physicalTableName, columnFields);

        Session session = entityManager.unwrap(Session.class);
        int[] rowOutcome = new int[totalRows]; // 0 = pas encore traité, 1 = ok, -1 = échec
        try {
            session.doWork(connection -> {
                for (int start = 0; start < data.size(); start += BATCH_SIZE) {
                    int end = Math.min(start + BATCH_SIZE, data.size());
                    List<String[]> chunk = data.subList(start, end);
                    executeBatchChunk(connection, sql, columnFields, columnIndexes, chunk, start, rowOutcome, tableName, result);
                }
            });

            // Resynchronise la séquence SERIAL de Postgres après insertion d'IDs explicites
            resyncSequence(session, physicalTableName);

        } catch (Exception e) {
            log.error("Erreur lors de l'import de l'entité {}", tableName, e);
            result.getErrors().add(new ImportErrorDTO(0, tableName, "entity", e.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
        }

        for (int outcome : rowOutcome) {
            if (outcome == 1) result.setSuccessfulRows(result.getSuccessfulRows() + 1);
            else if (outcome == -1) result.setFailedRows(result.getFailedRows() + 1);
        }

        return result;
    }

    /**
     * Exécute un lot (chunk) en JDBC batch. Si le batch entier échoue (ex: une
     * seule ligne invalide dans le lot), on retente ligne par ligne pour isoler
     * précisément la/les lignes fautives sans perdre le reste du lot.
     */
    private void executeBatchChunk(Connection connection, String sql, List<Field> columnFields,
                                    List<Integer> columnIndexes, List<String[]> chunk, int chunkStartIndex,
                                    int[] rowOutcome, String tableName, ImportResultDTO result) throws java.sql.SQLException {
        // SAVEPOINT avant le lot : si le batch échoue, Postgres marque TOUTE la
        // transaction comme "aborted" (contrairement à MySQL). Sans savepoint,
        // absolument plus aucune requête suivante ne passerait sur cette connexion,
        // même pour des lignes/tables parfaitement valides.
        Savepoint batchSavepoint = connection.setSavepoint();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String[] row : chunk) {
                bindRow(ps, columnFields, columnIndexes, row);
                ps.addBatch();
            }
            ps.executeBatch();
            for (int k = 0; k < chunk.size(); k++) {
                rowOutcome[chunkStartIndex + k] = 1;
            }
        } catch (Exception batchEx) {
            log.warn("Échec du lot [{} - {}[ pour {}, nouvelle tentative ligne par ligne: {}",
                chunkStartIndex, chunkStartIndex + chunk.size(), tableName, batchEx.getMessage());
            // On revient à l'état juste avant le lot, la transaction redevient utilisable
            connection.rollback(batchSavepoint);

            // Repli ligne par ligne pour isoler précisément la/les lignes en erreur.
            // Chaque ligne a SON PROPRE savepoint : une ligne fautive n'empêche pas
            // les suivantes de s'insérer.
            for (int k = 0; k < chunk.size(); k++) {
                int rowIndex = chunkStartIndex + k;
                Savepoint rowSavepoint = connection.setSavepoint();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    bindRow(ps, columnFields, columnIndexes, chunk.get(k));
                    ps.executeUpdate();
                    rowOutcome[rowIndex] = 1;
                } catch (Exception rowEx) {
                    connection.rollback(rowSavepoint);
                    log.error("Erreur lors de l'import de la ligne {} de {}", rowIndex, tableName, rowEx);
                    result.getErrors().add(new ImportErrorDTO(rowIndex, tableName, "row",
                        rowEx.getMessage(), "", ImportErrorDTO.ErrorSeverity.CRITICAL));
                    rowOutcome[rowIndex] = -1;
                }
            }
        }
    }

    private void bindRow(PreparedStatement ps, List<Field> columnFields, List<Integer> columnIndexes,
                          String[] row) throws Exception {
        for (int k = 0; k < columnFields.size(); k++) {
            int csvIndex = columnIndexes.get(k);
            Field field = columnFields.get(k);
            String cellValue = csvIndex < row.length ? row[csvIndex] : null;
            Object value = parseCSVValue(cellValue, field.getType());
            setPreparedStatementValue(ps, k + 1, value, field.getType());
        }
    }

    private void setPreparedStatementValue(PreparedStatement ps, int paramIndex, Object value, Class<?> type) throws Exception {
        if (value == null) {
            int sqlType = Types.VARCHAR;
            if (type == Integer.class || type == int.class) sqlType = Types.INTEGER;
            else if (type == Long.class || type == long.class) sqlType = Types.BIGINT;
            else if (type == Double.class || type == double.class) sqlType = Types.DOUBLE;
            else if (type == BigDecimal.class) sqlType = Types.NUMERIC;
            else if (type == Boolean.class || type == boolean.class) sqlType = Types.BOOLEAN;
            else if (type == LocalDate.class) sqlType = Types.DATE;
            else if (type == LocalDateTime.class) sqlType = Types.TIMESTAMP;
            ps.setNull(paramIndex, sqlType);
        } else if (value instanceof LocalDateTime) {
            ps.setTimestamp(paramIndex, Timestamp.valueOf((LocalDateTime) value));
        } else if (value instanceof LocalDate) {
            ps.setObject(paramIndex, value);
        } else {
            ps.setObject(paramIndex, value);
        }
    }

    private String buildInsertSql(String tableName, List<Field> columnFields) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < columnFields.size(); i++) {
            if (i > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(getColumnName(columnFields.get(i)));
            placeholders.append("?");
        }
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ") ON CONFLICT (id) DO NOTHING";
    }

    /**
     * Remet la séquence SERIAL de Postgres au niveau du MAX(id) réellement inséré,
     * sinon les prochaines créations "normales" (hors import) entreraient en
     * collision avec les IDs restaurés.
     */
    private void resyncSequence(Session session, String physicalTableName) {
        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT setval(pg_get_serial_sequence(?, 'id'), COALESCE((SELECT MAX(id) FROM " + physicalTableName + "), 1))")) {
                ps.setString(1, physicalTableName);
                ps.execute();
            } catch (Exception e) {
                log.warn("Impossible de resynchroniser la séquence pour {}: {}", physicalTableName, e.getMessage());
            }
        });
    }

    /**
     * Détermine le nom physique de la table à partir de l'annotation @Table.
     * IMPORTANT: si une entité n'a pas d'annotation @Table explicite, cette
     * méthode retourne null -> à corriger en ajoutant @Table(name = "...")
     * sur l'entité concernée (ex: @Table(name = "supports_cours")).
     */
    private String getTableName(Class<?> entityClass) {
        Table t = entityClass.getAnnotation(Table.class);
        if (t != null && !t.name().isEmpty()) {
            return t.name();
        }
        return null;
    }

    /**
     * Détermine le nom de colonne pour un champ : priorité à @Column(name=...),
     * sinon conversion camelCase -> snake_case (convention par défaut de
     * Spring/Hibernate, qui correspond au schéma de Ecole.sql).
     */
    private String getColumnName(Field field) {
        jakarta.persistence.Column c = field.getAnnotation(jakarta.persistence.Column.class);
        if (c != null && !c.name().isEmpty()) {
            return c.name();
        }
        return field.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    private Class<?> findEntityClass(String entityName) {
        try {
            Metamodel metamodel = entityManager.getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                if (entityType.getJavaType().getSimpleName().equals(entityName)) {
                    return entityType.getJavaType();
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de la classe pour {}", entityName, e);
        }
        return null;
    }

    private Object parseCSVValue(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) return null;
        
        value = value.trim();

        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == BigDecimal.class) return new BigDecimal(value);
        if (targetType == Boolean.class || targetType == boolean.class) return parseBoolean(value);
        if (targetType == LocalDate.class) return LocalDate.parse(value, LOCAL_DATE_FORMATTER);
        if (targetType == LocalDateTime.class) return LocalDateTime.parse(value, DATE_FORMATTER);
        
        return value;
    }

    private Boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("oui") || value.equals("1")) {
            return true;
        }
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("non") || value.equals("0")) {
            return false;
        }
        return null;
    }
}
