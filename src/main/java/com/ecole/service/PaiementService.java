package com.ecole.service;

import com.ecole.entity.*;
// Ajouter cet import en haut
import com.ecole.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecole.dto.Secretaire.BilanClasseDTO;
import com.ecole.dto.Secretaire.BilanGlobalDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.ecole.dto.Secretaire.PaiementGroupeDTO;
import java.util.UUID;

// export pdf
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;

import org.springframework.data.jpa.domain.Specification;

@Service
public class PaiementService {

        @Autowired
        private PaiementRepository paiementRepository;
        @Autowired
        private EcheanceRepository echeanceRepository;
        @Autowired
        private EcheancierRepository echeancierRepository;
        @Autowired
        private InscriptionRepository inscriptionRepository;
        @Autowired
        private AnneeScolaireRepository anneeScolaireRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private ClasseRepository classeRepository;
        @Autowired
        private ProfilEtudiantRepository profilEtudiantRepository;

        // Retourner l'année scolaire active
        public AnneeScolaire getAnneeActive() {
                return anneeScolaireRepository.findByEstActiveTrue()
                                .orElseThrow(() -> new RuntimeException("Aucune année scolaire active"));
        }

        // Retourner toutes les inscriptions actives de l'année en cours
        public List<Inscription> getInscriptionsActives() {
                AnneeScolaire annee = getAnneeActive();
                return inscriptionRepository.findByAnneeScolaireAndStatut(annee.getId(), "active");
        }

        // Retourner les échéances non soldées d'une inscription
        public List<Echeance> getEcheancesOuvertes(Long inscriptionId) {
                Inscription inscription = inscriptionRepository.findById(inscriptionId)
                                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

                List<Echeancier> echeanciers = echeancierRepository.findByInscription(inscription);
                if (echeanciers.isEmpty())
                        return List.of();

                // On prend le premier écheancier (un seul par inscription en général)
                return echeanceRepository.findByEcheancierAndEstSoldeeFalse(echeanciers.get(0));
        }

        // Enregistrer un paiement et vérifier si l'échéance est soldée
        @Transactional
        public String enregistrerPaiementGroupe(PaiementGroupeDTO dto) {

                Inscription inscription = inscriptionRepository.findById(dto.getInscriptionId())
                                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

                Echeance echeance = echeanceRepository.findById(dto.getEcheanceId())
                                .orElseThrow(() -> new RuntimeException("Échéance introuvable"));

                // Récupérer le secrétaire connecté
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User saisiPar = userRepository.findByEmail(email).orElse(null);

                // Générer référence groupe unique
                String refGroupe = "GRP-"
                                + java.time.LocalDate.now()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

                // Sauvegarder chaque ligne
                for (PaiementGroupeDTO.LignePaiement ligne : dto.getLignes()) {
                        Paiement p = new Paiement();
                        p.setInscription(inscription);
                        p.setEcheance(echeance);
                        p.setMontant(ligne.getMontant());
                        p.setModePaiement(ligne.getModePaiement());
                        p.setReferenceTransaction(refGroupe);
                        p.setDatePaiement(java.time.LocalDate.now());
                        p.setSaisiPar(saisiPar);
                        p.setCreatedAt(java.time.LocalDateTime.now());
                        paiementRepository.save(p);
                }

                // Vérifier si l'échéance est soldée
                BigDecimal totalEcheance = paiementRepository.findByEcheance(echeance)
                                .stream()
                                .map(Paiement::getMontant)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalEcheance.compareTo(echeance.getMontantAttendu()) >= 0) {
                        echeance.setEstSoldee(true);
                        echeanceRepository.save(echeance);
                }

                return refGroupe;
        }

        // Récupérer toutes les lignes d'un groupe
        public List<Paiement> getPaiementsByReference(String reference) {
                return paiementRepository.findByReferenceTransaction(reference);
        }

        // Total encaissé pour une inscription
        public BigDecimal getTotalEncaisse(Long inscriptionId) {
                Inscription inscription = inscriptionRepository.findById(inscriptionId)
                                .orElseThrow(() -> new RuntimeException("Inscription introuvable"));
                return paiementRepository.totalEncaisseParInscription(inscription);
        }

        // ===================================== BILAN
        // =================================== //

        public BilanGlobalDTO getBilanGlobal() {
                AnneeScolaire annee = getAnneeActive();
                List<Inscription> inscriptions = inscriptionRepository
                                .findByAnneeScolaireAndStatut(annee.getId(), "active");

                // Regrouper par classe
                Map<String, List<Inscription>> parClasse = new java.util.LinkedHashMap<>();
                for (Inscription insc : inscriptions) {
                        String nomClasse = classeRepository.findById(insc.getClasseId()).get().getNom();
                        parClasse.computeIfAbsent(nomClasse, k -> new ArrayList<>()).add(insc);
                }

                List<BilanClasseDTO> lignes = new ArrayList<>();
                BigDecimal totalAttenduGlobal = BigDecimal.ZERO;
                BigDecimal totalEncaisseGlobal = BigDecimal.ZERO;

                for (Map.Entry<String, List<Inscription>> entry : parClasse.entrySet()) {
                        String nomClasse = entry.getKey();
                        List<Inscription> inscsClasse = entry.getValue();

                        BigDecimal totalAttendu = BigDecimal.ZERO;
                        BigDecimal totalEncaisse = BigDecimal.ZERO;
                        int nombreSoldes = 0;

                        for (Inscription insc : inscsClasse) {
                                // Total attendu = somme des échéances de l'écheancier
                                List<Echeancier> echeanciers = echeancierRepository.findByInscription(insc);
                                if (!echeanciers.isEmpty()) {
                                        List<Echeance> echeances = echeanceRepository
                                                        .findByEcheancier(echeanciers.get(0));
                                        for (Echeance e : echeances) {
                                                totalAttendu = totalAttendu.add(e.getMontantAttendu());
                                        }
                                }

                                // Total encaissé
                                BigDecimal encaisse = paiementRepository.totalEncaisseParInscription(insc);
                                totalEncaisse = totalEncaisse.add(encaisse);

                                // Solde = toutes les échéances soldées
                                if (!echeanciers.isEmpty()) {
                                        List<Echeance> ouvertes = echeanceRepository
                                                        .findByEcheancierAndEstSoldeeFalse(echeanciers.get(0));
                                        if (ouvertes.isEmpty())
                                                nombreSoldes++;
                                }
                        }

                        lignes.add(new BilanClasseDTO(
                                        nomClasse,
                                        totalAttendu,
                                        totalEncaisse,
                                        inscsClasse.size(),
                                        nombreSoldes));

                        totalAttenduGlobal = totalAttenduGlobal.add(totalAttendu);
                        totalEncaisseGlobal = totalEncaisseGlobal.add(totalEncaisse);
                }

                return new BilanGlobalDTO(totalAttenduGlobal, totalEncaisseGlobal, lignes);
        }

        // ─── EXPORT PDF ──────────────────────────────────────────────
        public byte[] exportPdf(BilanGlobalDTO bilan) throws Exception {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                com.itextpdf.text.Document doc = new com.itextpdf.text.Document(
                                com.itextpdf.text.PageSize.A4, 40, 40, 60, 40);
                com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();

                // Couleurs
                com.itextpdf.text.BaseColor vertFonce = new com.itextpdf.text.BaseColor(15, 110, 86);
                com.itextpdf.text.BaseColor vertClair = new com.itextpdf.text.BaseColor(225, 245, 238);
                com.itextpdf.text.BaseColor grisClair = new com.itextpdf.text.BaseColor(245, 245, 245);
                com.itextpdf.text.BaseColor rouge = new com.itextpdf.text.BaseColor(180, 30, 30);

                // Polices
                com.itextpdf.text.Font fontTitre = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 20,
                                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fontSousTitre = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA,
                                10, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fontSection = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 12,
                                com.itextpdf.text.Font.BOLD, vertFonce);
                com.itextpdf.text.Font fontEntete = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fontNormal = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                                com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.DARK_GRAY);
                com.itextpdf.text.Font fontBold = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.DARK_GRAY);
                com.itextpdf.text.Font fontRouge = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                                com.itextpdf.text.Font.BOLD, rouge);

                // Bannière titre
                com.itextpdf.text.pdf.PdfPTable banner = new com.itextpdf.text.pdf.PdfPTable(1);
                banner.setWidthPercentage(100);
                com.itextpdf.text.pdf.PdfPCell bannerCell = new com.itextpdf.text.pdf.PdfPCell();
                bannerCell.setBackgroundColor(vertFonce);
                bannerCell.setPadding(18);
                bannerCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                com.itextpdf.text.Paragraph titrePara = new com.itextpdf.text.Paragraph("BILAN DES PAIEMENTS",
                                fontTitre);
                titrePara.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                bannerCell.addElement(titrePara);
                com.itextpdf.text.Paragraph datePara = new com.itextpdf.text.Paragraph(
                                "Généré le " + java.time.LocalDate.now()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                fontSousTitre);
                datePara.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                bannerCell.addElement(datePara);
                banner.addCell(bannerCell);
                doc.add(banner);
                doc.add(com.itextpdf.text.Chunk.NEWLINE);

                // Cartes résumé global
                com.itextpdf.text.pdf.PdfPTable resume = new com.itextpdf.text.pdf.PdfPTable(3);
                resume.setWidthPercentage(100);
                resume.setSpacingAfter(16);
                resume.setWidths(new float[] { 1, 1, 1 });
                resume.addCell(carteResume("Total attendu",
                                String.format("%,.0f Ar", bilan.getTotalAttenduGlobal()),
                                vertClair, vertFonce));
                resume.addCell(carteResume("Total encaissé",
                                String.format("%,.0f Ar", bilan.getTotalEncaisseGlobal()),
                                new com.itextpdf.text.BaseColor(220, 240, 255),
                                new com.itextpdf.text.BaseColor(10, 77, 200)));
                resume.addCell(carteResume("Reste à encaisser",
                                String.format("%,.0f Ar", bilan.getResteAEncaisser()),
                                new com.itextpdf.text.BaseColor(255, 230, 230), rouge));
                doc.add(resume);

                // Taux global
                com.itextpdf.text.Paragraph taux = new com.itextpdf.text.Paragraph(
                                String.format("Taux de recouvrement global : %.1f%%", bilan.getPourcentageGlobal()),
                                fontSection);
                taux.setSpacingAfter(10);
                doc.add(taux);

                // Tableau par classe
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 2.5f, 1.8f, 1.8f, 1.2f, 1.2f, 1.5f });
                table.setSpacingBefore(6);

                String[] entetes = { "Classe", "Attendu (Ar)", "Encaissé (Ar)", "Élèves", "Soldés", "Taux" };
                for (String e : entetes) {
                        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                                        new com.itextpdf.text.Phrase(e, fontEntete));
                        cell.setBackgroundColor(vertFonce);
                        cell.setPadding(8);
                        cell.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
                        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        table.addCell(cell);
                }

                boolean altRow = false;
                for (BilanClasseDTO ligne : bilan.getLignes()) {
                        com.itextpdf.text.BaseColor bg = altRow ? grisClair : com.itextpdf.text.BaseColor.WHITE;
                        altRow = !altRow;
                        double pct = ligne.getPourcentage();
                        com.itextpdf.text.Font fontPct = pct >= 80 ? fontBold : fontRouge;

                        table.addCell(cellule(ligne.getNomClasse(), fontBold, bg,
                                        com.itextpdf.text.Element.ALIGN_LEFT));
                        table.addCell(cellule(String.format("%,.0f", ligne.getTotalAttendu()), fontNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_RIGHT));
                        table.addCell(cellule(String.format("%,.0f", ligne.getTotalEncaisse()), fontNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_RIGHT));
                        table.addCell(cellule(String.valueOf(ligne.getNombreEleves()), fontNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_CENTER));
                        table.addCell(cellule(String.valueOf(ligne.getNombreSoldes()), fontNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_CENTER));
                        table.addCell(cellule(String.format("%.1f%%", pct), fontPct, bg,
                                        com.itextpdf.text.Element.ALIGN_CENTER));
                }

                // Ligne total
                com.itextpdf.text.pdf.PdfPCell[] totaux = {
                                cellule("TOTAL", fontEntete, vertClair, com.itextpdf.text.Element.ALIGN_LEFT),
                                cellule(String.format("%,.0f", bilan.getTotalAttenduGlobal()), fontEntete, vertClair,
                                                com.itextpdf.text.Element.ALIGN_RIGHT),
                                cellule(String.format("%,.0f", bilan.getTotalEncaisseGlobal()), fontEntete, vertClair,
                                                com.itextpdf.text.Element.ALIGN_RIGHT),
                                cellule("", fontEntete, vertClair, com.itextpdf.text.Element.ALIGN_CENTER),
                                cellule("", fontEntete, vertClair, com.itextpdf.text.Element.ALIGN_CENTER),
                                cellule(String.format("%.1f%%", bilan.getPourcentageGlobal()), fontEntete, vertClair,
                                                com.itextpdf.text.Element.ALIGN_CENTER)
                };
                for (com.itextpdf.text.pdf.PdfPCell c : totaux)
                        table.addCell(c);

                doc.add(table);
                doc.close();
                return out.toByteArray();
        }

        // Méthodes utilitaires PDF
        private com.itextpdf.text.pdf.PdfPCell carteResume(
                        String label, String valeur,
                        com.itextpdf.text.BaseColor bg,
                        com.itextpdf.text.BaseColor couleurTexte) {
                com.itextpdf.text.Font fl = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
                                com.itextpdf.text.Font.NORMAL, couleurTexte);
                com.itextpdf.text.Font fv = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 13,
                                com.itextpdf.text.Font.BOLD, couleurTexte);
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell();
                cell.setBackgroundColor(bg);
                cell.setPadding(12);
                cell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.addElement(new com.itextpdf.text.Paragraph(label, fl));
                cell.addElement(new com.itextpdf.text.Paragraph(valeur, fv));
                return cell;
        }

        private com.itextpdf.text.pdf.PdfPCell cellule(
                        String texte,
                        com.itextpdf.text.Font font,
                        com.itextpdf.text.BaseColor bg,
                        int align) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase(texte, font));
                cell.setBackgroundColor(bg);
                cell.setPadding(7);
                cell.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(align);
                return cell;
        }

        // ─── EXPORT EXCEL ────────────────────────────────────────────
        public byte[] exportExcel(BilanGlobalDTO bilan) throws Exception {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XSSFWorkbook wb = new XSSFWorkbook();
                Sheet sheet = wb.createSheet("Bilan Paiements");

                // Styles
                CellStyle styleEntete = wb.createCellStyle();
                Font fontEntete = wb.createFont();
                fontEntete.setBold(true);
                fontEntete.setColor(IndexedColors.WHITE.getIndex());
                styleEntete.setFont(fontEntete);
                styleEntete.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
                styleEntete.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                styleEntete.setAlignment(HorizontalAlignment.CENTER);
                styleEntete.setBorderBottom(BorderStyle.THIN);

                CellStyle styleTitre = wb.createCellStyle();
                Font fontTitre = wb.createFont();
                fontTitre.setBold(true);
                fontTitre.setFontHeightInPoints((short) 14);
                fontTitre.setColor(IndexedColors.DARK_GREEN.getIndex());
                styleTitre.setFont(fontTitre);

                CellStyle styleTotal = wb.createCellStyle();
                Font fontTotal = wb.createFont();
                fontTotal.setBold(true);
                styleTotal.setFont(fontTotal);
                styleTotal.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                styleTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellStyle styleNombre = wb.createCellStyle();
                DataFormat format = wb.createDataFormat();
                styleNombre.setDataFormat(format.getFormat("#,##0"));

                // Titre
                Row rowTitre = sheet.createRow(0);
                Cell cellTitre = rowTitre.createCell(0);
                cellTitre.setCellValue("BILAN DES PAIEMENTS — " + java.time.LocalDate.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cellTitre.setCellStyle(styleTitre);

                // Résumé global
                Row rowResume = sheet.createRow(2);
                rowResume.createCell(0).setCellValue("Total attendu");
                Cell cAtt = rowResume.createCell(1);
                cAtt.setCellValue(bilan.getTotalAttenduGlobal().doubleValue());
                cAtt.setCellStyle(styleNombre);

                Row rowResume2 = sheet.createRow(3);
                rowResume2.createCell(0).setCellValue("Total encaissé");
                Cell cEnc = rowResume2.createCell(1);
                cEnc.setCellValue(bilan.getTotalEncaisseGlobal().doubleValue());
                cEnc.setCellStyle(styleNombre);

                Row rowResume3 = sheet.createRow(4);
                rowResume3.createCell(0).setCellValue("Taux global");
                rowResume3.createCell(1).setCellValue(
                                String.format("%.1f%%", bilan.getPourcentageGlobal()));

                // En-têtes tableau
                Row rowEntete = sheet.createRow(6);
                String[] cols = { "Classe", "Total Attendu (Ar)", "Total Encaissé (Ar)",
                                "Nb Élèves", "Nb Soldés", "Nb Impayés", "Taux (%)" };
                for (int i = 0; i < cols.length; i++) {
                        Cell c = rowEntete.createCell(i);
                        c.setCellValue(cols[i]);
                        c.setCellStyle(styleEntete);
                }

                // Données par classe
                int rowNum = 7;
                for (BilanClasseDTO ligne : bilan.getLignes()) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(ligne.getNomClasse());

                        Cell cA = row.createCell(1);
                        cA.setCellValue(ligne.getTotalAttendu().doubleValue());
                        cA.setCellStyle(styleNombre);

                        Cell cE = row.createCell(2);
                        cE.setCellValue(ligne.getTotalEncaisse().doubleValue());
                        cE.setCellStyle(styleNombre);

                        row.createCell(3).setCellValue(ligne.getNombreEleves());
                        row.createCell(4).setCellValue(ligne.getNombreSoldes());
                        row.createCell(5).setCellValue(ligne.getNombreImpayes());
                        row.createCell(6).setCellValue(
                                        Math.round(ligne.getPourcentage() * 10.0) / 10.0);
                }

                // Ligne total
                Row rowTotal = sheet.createRow(rowNum);
                Cell ctLabel = rowTotal.createCell(0);
                ctLabel.setCellValue("TOTAL");
                ctLabel.setCellStyle(styleTotal);

                Cell ctAtt = rowTotal.createCell(1);
                ctAtt.setCellValue(bilan.getTotalAttenduGlobal().doubleValue());
                ctAtt.setCellStyle(styleTotal);

                Cell ctEnc = rowTotal.createCell(2);
                ctEnc.setCellValue(bilan.getTotalEncaisseGlobal().doubleValue());
                ctEnc.setCellStyle(styleTotal);

                Cell ctTaux = rowTotal.createCell(6);
                ctTaux.setCellValue(Math.round(bilan.getPourcentageGlobal() * 10.0) / 10.0);
                ctTaux.setCellStyle(styleTotal);

                // Largeur colonnes automatique
                for (int i = 0; i < cols.length; i++)
                        sheet.autoSizeColumn(i);

                wb.write(out);
                wb.close();
                return out.toByteArray();
        }

        public List<Paiement> getPaiementsFiltres(String nom, String classe,
                        String modePaiement,
                        LocalDate dateDebut,
                        LocalDate dateFin) {
                Specification<Paiement> spec = PaiementSpecification.filtrer(
                                nom, classe, modePaiement, dateDebut, dateFin);
                return paiementRepository.findAll(spec);
        }

        public Paiement getPaiementById(Integer id) {
                return paiementRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + id));
        }

        public byte[] exportFacturePdf(Integer paiementId) throws Exception {
                Paiement p = getPaiementById(paiementId);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                com.itextpdf.text.Document doc = new com.itextpdf.text.Document(
                                com.itextpdf.text.PageSize.A5, 40, 40, 40, 40);
                com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();

                // Couleurs
                com.itextpdf.text.BaseColor vert = new com.itextpdf.text.BaseColor(15, 110, 86);
                com.itextpdf.text.BaseColor vertClair = new com.itextpdf.text.BaseColor(225, 245, 238);
                com.itextpdf.text.BaseColor gris = new com.itextpdf.text.BaseColor(245, 245, 245);
                com.itextpdf.text.BaseColor grisFonce = new com.itextpdf.text.BaseColor(80, 80, 80);

                // Polices
                com.itextpdf.text.Font fNomEcole = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                                com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fAdresse = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                                com.itextpdf.text.Font.NORMAL, grisFonce);
                com.itextpdf.text.Font fTitreRecu = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 14,
                                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fNumero = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9,
                                com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fSection = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
                                com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fLabel = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9,
                                com.itextpdf.text.Font.NORMAL, grisFonce);
                com.itextpdf.text.Font fValeur = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9,
                                com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.BLACK);
                com.itextpdf.text.Font fMontant = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                                com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fFooter = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                7,
                                com.itextpdf.text.Font.ITALIC, grisFonce);

                // ── EN-TÊTE : logo + nom école ──
                com.itextpdf.text.pdf.PdfPTable entete = new com.itextpdf.text.pdf.PdfPTable(2);
                entete.setWidthPercentage(100);
                entete.setWidths(new float[] { 1f, 3f });
                entete.setSpacingAfter(12);

                // Logo — image si disponible, sinon cercle vert avec E
                com.itextpdf.text.pdf.PdfPCell cellLogo = new com.itextpdf.text.pdf.PdfPCell();
                cellLogo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellLogo.setPadding(4);
                try {
                        String logoPath = getClass().getClassLoader()
                                        .getResource("static/images/logo.png").getPath();
                        com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                        logo.scaleToFit(60, 60);
                        cellLogo.addElement(logo);
                } catch (Exception e) {
                        // Pas de logo — carré vert avec E
                        com.itextpdf.text.pdf.PdfPTable logoFallback = new com.itextpdf.text.pdf.PdfPTable(1);
                        com.itextpdf.text.pdf.PdfPCell lf = new com.itextpdf.text.pdf.PdfPCell(
                                        new com.itextpdf.text.Phrase("E",
                                                        new com.itextpdf.text.Font(
                                                                        com.itextpdf.text.Font.FontFamily.HELVETICA, 28,
                                                                        com.itextpdf.text.Font.BOLD,
                                                                        com.itextpdf.text.BaseColor.WHITE)));
                        lf.setBackgroundColor(vert);
                        lf.setFixedHeight(56);
                        lf.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        lf.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                        lf.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                        logoFallback.addCell(lf);
                        cellLogo.addElement(logoFallback);
                }
                entete.addCell(cellLogo);

                // Nom école + adresse
                com.itextpdf.text.pdf.PdfPCell cellEcole = new com.itextpdf.text.pdf.PdfPCell();
                cellEcole.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellEcole.setPadding(4);
                cellEcole.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cellEcole.addElement(new com.itextpdf.text.Paragraph("LycéePro", fNomEcole));
                cellEcole.addElement(new com.itextpdf.text.Paragraph("Antananarivo, Madagascar", fAdresse));
                cellEcole.addElement(new com.itextpdf.text.Paragraph("contact@lycee.mg  |  020 22 000 00", fAdresse));
                entete.addCell(cellEcole);
                doc.add(entete);

                // Ligne séparatrice verte
                com.itextpdf.text.pdf.PdfPTable ligne1 = new com.itextpdf.text.pdf.PdfPTable(1);
                ligne1.setWidthPercentage(100);
                ligne1.setSpacingAfter(10);
                com.itextpdf.text.pdf.PdfPCell lignCell = new com.itextpdf.text.pdf.PdfPCell();
                lignCell.setBackgroundColor(vert);
                lignCell.setFixedHeight(3);
                lignCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                ligne1.addCell(lignCell);
                doc.add(ligne1);

                // ── BANNIÈRE REÇU ──
                String numeroRecu = String.format("REC-%s-%04d",
                                java.time.LocalDate.now()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                p.getId());
                com.itextpdf.text.pdf.PdfPTable banniere = new com.itextpdf.text.pdf.PdfPTable(2);
                banniere.setWidthPercentage(100);
                banniere.setWidths(new float[] { 2f, 1f });
                banniere.setSpacingAfter(14);

                com.itextpdf.text.pdf.PdfPCell cellTitre = new com.itextpdf.text.pdf.PdfPCell();
                cellTitre.setBackgroundColor(vert);
                cellTitre.setPadding(10);
                cellTitre.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellTitre.addElement(new com.itextpdf.text.Paragraph("REÇU DE PAIEMENT", fTitreRecu));
                cellTitre.addElement(new com.itextpdf.text.Paragraph("N° " + numeroRecu, fNumero));
                banniere.addCell(cellTitre);

                com.itextpdf.text.pdf.PdfPCell cellDate = new com.itextpdf.text.pdf.PdfPCell();
                cellDate.setBackgroundColor(vertClair);
                cellDate.setPadding(10);
                cellDate.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellDate.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                cellDate.addElement(new com.itextpdf.text.Paragraph("Date",
                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                                                com.itextpdf.text.Font.NORMAL, grisFonce)));
                cellDate.addElement(new com.itextpdf.text.Paragraph(
                                p.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                                                com.itextpdf.text.Font.BOLD,
                                                vert)));
                banniere.addCell(cellDate);
                doc.add(banniere);

                // ── INFOS ÉLÈVE ──
                com.itextpdf.text.Paragraph sectionEleve = new com.itextpdf.text.Paragraph("ÉLÈVE", fSection);
                sectionEleve.setSpacingAfter(4);
                doc.add(sectionEleve);

                com.itextpdf.text.pdf.PdfPTable tableEleve = new com.itextpdf.text.pdf.PdfPTable(2);
                tableEleve.setWidthPercentage(100);
                tableEleve.setWidths(new float[] { 1f, 2f });
                tableEleve.setSpacingAfter(12);

                ProfilEtudiant etudiant = profilEtudiantRepository.findById(p.getInscription().getEtudiantId()).orElse(null);
                String nomComplet = etudiant.getNom().toUpperCase()
                                + " " + etudiant.getPrenom();

                Classe classe = classeRepository.findById(p.getInscription().getClasseId()).orElse(null);


                ajouterLigneInfo(tableEleve, "Nom complet", nomComplet, fLabel, fValeur, gris);
                ajouterLigneInfo(tableEleve, "Matricule", etudiant.getMatricule(), fLabel,
                                fValeur,com.itextpdf.text.BaseColor.WHITE);
                ajouterLigneInfo(tableEleve, "Classe", classe.getNom(), fLabel, fValeur, gris);
                ajouterLigneInfo(tableEleve, "Année scolaire", anneeScolaireRepository.findById(p.getInscription().getAnneeScolaireId()).orElse(null).getLibelle(),
                                fLabel,
                                fValeur, com.itextpdf.text.BaseColor.WHITE);
                doc.add(tableEleve);

                // ── DÉTAIL PAIEMENT ──
                com.itextpdf.text.Paragraph sectionPaiement = new com.itextpdf.text.Paragraph("DÉTAIL DU PAIEMENT",
                                fSection);
                sectionPaiement.setSpacingAfter(4);
                doc.add(sectionPaiement);

                com.itextpdf.text.pdf.PdfPTable tablePaiement = new com.itextpdf.text.pdf.PdfPTable(2);
                tablePaiement.setWidthPercentage(100);
                tablePaiement.setWidths(new float[] { 1f, 2f });
                tablePaiement.setSpacingAfter(14);

                String tranche = "Tranche " + p.getEcheance().getNumeroTranche();
                String statut = p.getEcheance().getEstSoldee() ? "✔ SOLDÉE" : "⏳ EN ATTENTE";
                String dateLimite = p.getEcheance().getDateLimite()
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                ajouterLigneInfo(tablePaiement, "Tranche", tranche, fLabel, fValeur, gris);
                ajouterLigneInfo(tablePaiement, "Date limite", dateLimite, fLabel, fValeur,
                                com.itextpdf.text.BaseColor.WHITE);
                ajouterLigneInfo(tablePaiement, "Mode de paiement",
                                p.getModePaiement().toUpperCase(), fLabel, fValeur, gris);
                ajouterLigneInfo(tablePaiement, "Statut", statut, fLabel, fValeur, com.itextpdf.text.BaseColor.WHITE);
                doc.add(tablePaiement);

                // ── MONTANT ENCADRÉ ──
                com.itextpdf.text.pdf.PdfPTable tableMontant = new com.itextpdf.text.pdf.PdfPTable(1);
                tableMontant.setWidthPercentage(100);
                tableMontant.setSpacingAfter(16);
                com.itextpdf.text.pdf.PdfPCell cellMontant = new com.itextpdf.text.pdf.PdfPCell();
                cellMontant.setBackgroundColor(vertClair);
                cellMontant.setPadding(14);
                cellMontant.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellMontant.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                com.itextpdf.text.Paragraph labelMontant = new com.itextpdf.text.Paragraph("MONTANT PAYÉ", fLabel);
                labelMontant.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cellMontant.addElement(labelMontant);
                com.itextpdf.text.Paragraph valMontant = new com.itextpdf.text.Paragraph(
                                String.format("%,.0f Ar", p.getMontant()), fMontant);
                valMontant.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cellMontant.addElement(valMontant);
                tableMontant.addCell(cellMontant);
                doc.add(tableMontant);

                // ── SIGNATURE ──
                com.itextpdf.text.pdf.PdfPTable tableSig = new com.itextpdf.text.pdf.PdfPTable(2);
                tableSig.setWidthPercentage(100);
                tableSig.setSpacingAfter(10);

                com.itextpdf.text.pdf.PdfPCell sigEleve = new com.itextpdf.text.pdf.PdfPCell();
                sigEleve.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                sigEleve.addElement(new com.itextpdf.text.Paragraph("Signature de l'élève / parent", fLabel));
                sigEleve.addElement(new com.itextpdf.text.Paragraph("\n\n_______________________", fLabel));
                tableSig.addCell(sigEleve);

                com.itextpdf.text.pdf.PdfPCell sigSecr = new com.itextpdf.text.pdf.PdfPCell();
                sigSecr.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                sigSecr.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                sigSecr.addElement(new com.itextpdf.text.Paragraph("Cachet et signature du secrétariat", fLabel));
                sigSecr.addElement(new com.itextpdf.text.Paragraph("\n\n_______________________", fLabel));
                tableSig.addCell(sigSecr);
                doc.add(tableSig);

                // ── FOOTER ──
                com.itextpdf.text.pdf.PdfPTable footer = new com.itextpdf.text.pdf.PdfPTable(1);
                footer.setWidthPercentage(100);
                com.itextpdf.text.pdf.PdfPCell footerCell = new com.itextpdf.text.pdf.PdfPCell();
                footerCell.setBackgroundColor(vert);
                footerCell.setPadding(6);
                footerCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                com.itextpdf.text.Paragraph footerText = new com.itextpdf.text.Paragraph(
                                "Ce reçu est un document officiel. Veuillez le conserver précieusement.", fFooter);
                footerText.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                footerCell.addElement(footerText);
                footer.addCell(footerCell);
                doc.add(footer);

                doc.close();
                return out.toByteArray();
        }

        // Méthode utilitaire lignes info
        private void ajouterLigneInfo(
                        com.itextpdf.text.pdf.PdfPTable table,
                        String label, String valeur,
                        com.itextpdf.text.Font fLabel,
                        com.itextpdf.text.Font fValeur,
                        com.itextpdf.text.BaseColor bg) {

                com.itextpdf.text.pdf.PdfPCell cLabel = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase(label, fLabel));
                cLabel.setBackgroundColor(bg);
                cLabel.setPadding(6);
                cLabel.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);

                com.itextpdf.text.pdf.PdfPCell cValeur = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase(valeur, fValeur));
                cValeur.setBackgroundColor(bg);
                cValeur.setPadding(6);
                cValeur.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);

                table.addCell(cLabel);
                table.addCell(cValeur);
        }

        public byte[] exportFactureGroupePdf(String reference, List<Paiement> lignes, BigDecimal total)
                        throws Exception {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Paiement premier = lignes.get(0);

                com.itextpdf.text.Document doc = new com.itextpdf.text.Document(
                                com.itextpdf.text.PageSize.A5, 40, 40, 40, 40);
                com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();

                // Couleurs
                com.itextpdf.text.BaseColor vert = new com.itextpdf.text.BaseColor(15, 110, 86);
                com.itextpdf.text.BaseColor vertClair = new com.itextpdf.text.BaseColor(225, 245, 238);
                com.itextpdf.text.BaseColor gris = new com.itextpdf.text.BaseColor(245, 245, 245);
                com.itextpdf.text.BaseColor grisFonce = new com.itextpdf.text.BaseColor(80, 80, 80);

                // Polices
                com.itextpdf.text.Font fNomEcole = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fAdresse = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL,
                                grisFonce);
                com.itextpdf.text.Font fTitreRecu = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD,
                                com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fNumero = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fSection = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fLabel = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9, com.itextpdf.text.Font.NORMAL, grisFonce);
                com.itextpdf.text.Font fValeur = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.BLACK);
                com.itextpdf.text.Font fMontant = new com.itextpdf.text.Font(
                                com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, vert);
                com.itextpdf.text.Font fFooter = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                7, com.itextpdf.text.Font.ITALIC, grisFonce);
                com.itextpdf.text.Font fEntete = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                9, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.DARK_GRAY);

                // ── EN-TÊTE logo + nom école ──
                com.itextpdf.text.pdf.PdfPTable entete = new com.itextpdf.text.pdf.PdfPTable(2);
                entete.setWidthPercentage(100);
                entete.setWidths(new float[] { 1f, 3f });
                entete.setSpacingAfter(12);

                com.itextpdf.text.pdf.PdfPCell cellLogo = new com.itextpdf.text.pdf.PdfPCell();
                cellLogo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellLogo.setPadding(4);
                try {
                        String logoPath = getClass().getClassLoader()
                                        .getResource("static/images/logo.png").getPath();
                        com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                        logo.scaleToFit(60, 60);
                        cellLogo.addElement(logo);
                } catch (Exception e) {
                        com.itextpdf.text.pdf.PdfPTable lf = new com.itextpdf.text.pdf.PdfPTable(1);
                        com.itextpdf.text.pdf.PdfPCell lfCell = new com.itextpdf.text.pdf.PdfPCell(
                                        new com.itextpdf.text.Phrase("E",
                                                        new com.itextpdf.text.Font(
                                                                        com.itextpdf.text.Font.FontFamily.HELVETICA, 28,
                                                                        com.itextpdf.text.Font.BOLD,
                                                                        com.itextpdf.text.BaseColor.WHITE)));
                        lfCell.setBackgroundColor(vert);
                        lfCell.setFixedHeight(56);
                        lfCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        lfCell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                        lfCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                        lf.addCell(lfCell);
                        cellLogo.addElement(lf);
                }
                entete.addCell(cellLogo);

                com.itextpdf.text.pdf.PdfPCell cellEcole = new com.itextpdf.text.pdf.PdfPCell();
                cellEcole.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellEcole.setPadding(4);
                cellEcole.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cellEcole.addElement(new com.itextpdf.text.Paragraph("LycéePro", fNomEcole));
                cellEcole.addElement(new com.itextpdf.text.Paragraph("Antananarivo, Madagascar", fAdresse));
                cellEcole.addElement(new com.itextpdf.text.Paragraph("contact@lycee.mg  |  020 22 000 00", fAdresse));
                entete.addCell(cellEcole);
                doc.add(entete);

                // Ligne verte
                com.itextpdf.text.pdf.PdfPTable sep = new com.itextpdf.text.pdf.PdfPTable(1);
                sep.setWidthPercentage(100);
                sep.setSpacingAfter(10);
                com.itextpdf.text.pdf.PdfPCell sepCell = new com.itextpdf.text.pdf.PdfPCell();
                sepCell.setBackgroundColor(vert);
                sepCell.setFixedHeight(3);
                sepCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                sep.addCell(sepCell);
                doc.add(sep);

                // ── BANNIÈRE REÇU ──
                com.itextpdf.text.pdf.PdfPTable banniere = new com.itextpdf.text.pdf.PdfPTable(2);
                banniere.setWidthPercentage(100);
                banniere.setWidths(new float[] { 2f, 1f });
                banniere.setSpacingAfter(14);

                com.itextpdf.text.pdf.PdfPCell cellTitre = new com.itextpdf.text.pdf.PdfPCell();
                cellTitre.setBackgroundColor(vert);
                cellTitre.setPadding(10);
                cellTitre.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellTitre.addElement(new com.itextpdf.text.Paragraph("REÇU DE PAIEMENT", fTitreRecu));
                cellTitre.addElement(new com.itextpdf.text.Paragraph("N° " + reference, fNumero));
                banniere.addCell(cellTitre);

                com.itextpdf.text.pdf.PdfPCell cellDate = new com.itextpdf.text.pdf.PdfPCell();
                cellDate.setBackgroundColor(vertClair);
                cellDate.setPadding(10);
                cellDate.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cellDate.addElement(new com.itextpdf.text.Paragraph("Date",
                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                                                com.itextpdf.text.Font.NORMAL, grisFonce)));
                cellDate.addElement(new com.itextpdf.text.Paragraph(
                                premier.getDatePaiement()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                                                com.itextpdf.text.Font.BOLD, vert)));
                banniere.addCell(cellDate);
                doc.add(banniere);

                // ── INFOS ÉLÈVE ──
                com.itextpdf.text.Paragraph sectionEleve = new com.itextpdf.text.Paragraph("ÉLÈVE", fSection);
                sectionEleve.setSpacingAfter(4);
                doc.add(sectionEleve);

                com.itextpdf.text.pdf.PdfPTable tableEleve = new com.itextpdf.text.pdf.PdfPTable(2);
                tableEleve.setWidthPercentage(100);
                tableEleve.setWidths(new float[] { 1f, 2f });
                tableEleve.setSpacingAfter(12);

                ProfilEtudiant etudiant = profilEtudiantRepository.findById(premier.getInscription().getEtudiantId()).orElse(null);
                
                String nomComplet = etudiant.getNom().toUpperCase()
                                + " " + etudiant.getPrenom();

                ajouterLigneInfo(tableEleve, "Nom complet", nomComplet, fLabel, fValeur, gris);
                ajouterLigneInfo(tableEleve, "Matricule", etudiant.getMatricule(), fLabel, fValeur, com.itextpdf.text.BaseColor.WHITE);
                ajouterLigneInfo(tableEleve, "Classe", classeRepository.findById(premier.getInscription().getClasseId()).orElse(null).getNom(), fLabel, fValeur,
                                gris);
                ajouterLigneInfo(tableEleve, "Année scolaire", anneeScolaireRepository.findById(premier.getInscription().getAnneeScolaireId()).orElse(null).getLibelle(),
                                fLabel, fValeur, com.itextpdf.text.BaseColor.WHITE);
                ajouterLigneInfo(tableEleve, "Tranche", "Tranche " + premier.getEcheance().getNumeroTranche(), fLabel,
                                fValeur, gris);
                ajouterLigneInfo(tableEleve, "Date limite",
                                premier.getEcheance().getDateLimite()
                                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                fLabel, fValeur, com.itextpdf.text.BaseColor.WHITE);
                doc.add(tableEleve);

                // ── TABLEAU LIGNES PAIEMENT ──
                com.itextpdf.text.Paragraph sectionPaiement = new com.itextpdf.text.Paragraph("DÉTAIL DU PAIEMENT",
                                fSection);
                sectionPaiement.setSpacingAfter(4);
                doc.add(sectionPaiement);

                com.itextpdf.text.pdf.PdfPTable tableLignes = new com.itextpdf.text.pdf.PdfPTable(3);
                tableLignes.setWidthPercentage(100);
                tableLignes.setWidths(new float[] { 1f, 2f, 2f });
                tableLignes.setSpacingAfter(14);

                // En-têtes
                for (String h : new String[] { "#", "Mode de paiement", "Montant (Ar)" }) {
                        com.itextpdf.text.pdf.PdfPCell hCell = new com.itextpdf.text.pdf.PdfPCell(
                                        new com.itextpdf.text.Phrase(h, fEntete));
                        hCell.setBackgroundColor(vert);
                        hCell.setPadding(7);
                        hCell.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
                        hCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        tableLignes.addCell(hCell);
                }

                // Lignes
                boolean alt = false;
                int i = 1;
                for (Paiement p : lignes) {
                        com.itextpdf.text.BaseColor bg = alt ? gris : com.itextpdf.text.BaseColor.WHITE;
                        alt = !alt;

                        tableLignes.addCell(cellule(String.valueOf(i++), fNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_CENTER));
                        tableLignes.addCell(cellule(p.getModePaiement().toUpperCase(), fNormal, bg,
                                        com.itextpdf.text.Element.ALIGN_LEFT));
                        tableLignes.addCell(cellule(String.format("%,.0f", p.getMontant()), fValeur, bg,
                                        com.itextpdf.text.Element.ALIGN_RIGHT));
                }

                // Ligne total
                com.itextpdf.text.pdf.PdfPCell totalLabel = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase("TOTAL", fEntete));
                totalLabel.setBackgroundColor(vertClair);
                totalLabel.setPadding(7);
                totalLabel.setColspan(2);
                totalLabel.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                tableLignes.addCell(totalLabel);

                com.itextpdf.text.pdf.PdfPCell totalVal = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase(String.format("%,.0f Ar", total), fMontant));
                totalVal.setBackgroundColor(vertClair);
                totalVal.setPadding(7);
                totalVal.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                totalVal.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                tableLignes.addCell(totalVal);
                doc.add(tableLignes);

                // ── STATUT ──
                String statut = premier.getEcheance().getEstSoldee() ? "✔ ÉCHÉANCE SOLDÉE" : "⏳ PAIEMENT PARTIEL";
                com.itextpdf.text.BaseColor statutBg = premier.getEcheance().getEstSoldee() ? vertClair
                                : new com.itextpdf.text.BaseColor(255, 243, 220);
                com.itextpdf.text.pdf.PdfPTable tableStatut = new com.itextpdf.text.pdf.PdfPTable(1);
                tableStatut.setWidthPercentage(100);
                tableStatut.setSpacingAfter(14);
                com.itextpdf.text.pdf.PdfPCell statutCell = new com.itextpdf.text.pdf.PdfPCell(
                                new com.itextpdf.text.Phrase(statut,
                                                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                                                                11,
                                                                com.itextpdf.text.Font.BOLD, vert)));
                statutCell.setBackgroundColor(statutBg);
                statutCell.setPadding(10);
                statutCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                statutCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                tableStatut.addCell(statutCell);
                doc.add(tableStatut);

                // ── SIGNATURES ──
                com.itextpdf.text.pdf.PdfPTable tableSig = new com.itextpdf.text.pdf.PdfPTable(2);
                tableSig.setWidthPercentage(100);
                tableSig.setSpacingAfter(10);

                com.itextpdf.text.pdf.PdfPCell sigEleve = new com.itextpdf.text.pdf.PdfPCell();
                sigEleve.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                sigEleve.addElement(new com.itextpdf.text.Paragraph("Signature élève / parent", fLabel));
                sigEleve.addElement(new com.itextpdf.text.Paragraph("\n\n_______________________", fLabel));
                tableSig.addCell(sigEleve);

                com.itextpdf.text.pdf.PdfPCell sigSecr = new com.itextpdf.text.pdf.PdfPCell();
                sigSecr.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                sigSecr.addElement(new com.itextpdf.text.Paragraph("Cachet et signature secrétariat", fLabel));
                sigSecr.addElement(new com.itextpdf.text.Paragraph("\n\n_______________________", fLabel));
                tableSig.addCell(sigSecr);
                doc.add(tableSig);

                // ── FOOTER ──
                com.itextpdf.text.pdf.PdfPTable footer = new com.itextpdf.text.pdf.PdfPTable(1);
                footer.setWidthPercentage(100);
                com.itextpdf.text.pdf.PdfPCell footerCell = new com.itextpdf.text.pdf.PdfPCell();
                footerCell.setBackgroundColor(vert);
                footerCell.setPadding(6);
                footerCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                com.itextpdf.text.Paragraph footerText = new com.itextpdf.text.Paragraph(
                                "Ce reçu est un document officiel. Veuillez le conserver précieusement.", fFooter);
                footerText.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                footerCell.addElement(footerText);
                footer.addCell(footerCell);
                doc.add(footer);

                doc.close();
                return out.toByteArray();
        }

}