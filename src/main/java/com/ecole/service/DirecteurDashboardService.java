package com.ecole.service;

import com.ecole.dto.Directeur.*;
import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Depense;
import com.ecole.entity.Paiement;
import com.ecole.entity.VueEmployesDetail;
import com.ecole.repository.AnneeScolaireRepository;
import com.ecole.repository.DepenseRepository;
import com.ecole.repository.InscriptionRepository;
import com.ecole.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DirecteurDashboardService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PaiementRepository paiementRepository;
    private final DepenseRepository depenseRepository;
    private final EmployeService employeService;

    public DashboardResponseDTO getDashboard(Long anneeScolaireId, String moisSelectionne) {
        List<AnneeScolaire> annees = anneeScolaireRepository.findAll().stream()
                .sorted(Comparator.comparing(AnneeScolaire::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        AnneeScolaire annee = resoudreAnnee(anneeScolaireId, annees);
        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setAnnees(annees.stream().map(this::toAnneeOption).toList());

        if (annee == null || annee.getDateDebut() == null || annee.getDateFin() == null) {
            response.setMois(List.of());
            response.setSerieMensuelle(List.of());
            response.setTransactions(List.of());
            response.setKpis(creerKpisVides());
            response.setResume("Aucune année scolaire exploitable n'est disponible.");
            return response;
        }

        List<YearMonth> moisDeAnnee = construireMois(annee.getDateDebut(), annee.getDateFin());
        YearMonth moisSelection = resoudreMoisSelectionne(moisSelectionne, moisDeAnnee);

        List<Paiement> paiementsAnnee = paiementRepository.findDetailedByDatePaiementBetween(
                annee.getDateDebut(), annee.getDateFin());

        Map<YearMonth, List<Paiement>> paiementsParMois = paiementsAnnee.stream()
                .filter(p -> p.getDatePaiement() != null)
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getDatePaiement()), LinkedHashMap::new, Collectors.toList()));

        List<DashboardMonthlyStatDTO> serieMensuelle = new ArrayList<>();
        for (YearMonth mois : moisDeAnnee) {
            List<Paiement> paiementsDuMois = paiementsParMois.getOrDefault(mois, List.of());
            BigDecimal depensesMois = calculerDepensesSalairesProfesseurs(annee, mois);
            DashboardMonthlyStatDTO point = new DashboardMonthlyStatDTO();
            point.setValue(mois.toString());
            point.setLabel(formatMonthLabel(mois));
            point.setRecettes(sommeMontants(paiementsDuMois));
            point.setDepensesSalairesProfesseurs(depensesMois);
            point.setBeneficeNet(point.getRecettes().subtract(depensesMois));
            point.setTransactions(compterTransactions(paiementsDuMois));
            point.setElevesPayes(compterElevesPayes(paiementsDuMois));
            point.setSelected(mois.equals(moisSelection));
            serieMensuelle.add(point);
        }

        List<Paiement> paiementsSelectionnes = paiementsParMois.getOrDefault(moisSelection, List.of());
        BigDecimal depensesSelection = calculerDepensesSalairesProfesseurs(annee, moisSelection);
        List<DashboardTransactionDTO> transactions = construireTransactions(paiementsSelectionnes);

        DashboardKpisDTO kpis = construireKpis(paiementsSelectionnes, annee, moisSelection, depensesSelection);

        response.setAnneeScolaireId(annee.getId());
        response.setAnneeScolaireLibelle(annee.getLibelle());
        response.setMois(construireOptionsMois(moisDeAnnee, moisSelection));
        response.setSelectedMonth(moisSelection.toString());
        response.setSelectedMonthLabel(formatMonthLabel(moisSelection));
        response.setKpis(kpis);
        response.setSerieMensuelle(serieMensuelle);
        response.setTransactions(transactions);
        response.setResume(String.format("%s · %s", annee.getLibelle(), formatMonthLabel(moisSelection)));
        return response;
    }

    private DashboardKpisDTO construireKpis(List<Paiement> paiementsSelectionnes, AnneeScolaire annee, YearMonth moisSelection, BigDecimal depensesSalairesProfesseurs) {
        DashboardKpisDTO kpis = new DashboardKpisDTO();
        BigDecimal totalSelection = sommeMontants(paiementsSelectionnes);
        long transactionsSelection = compterTransactions(paiementsSelectionnes);
        long elevesPayesSelection = compterElevesPayes(paiementsSelectionnes);
        long totalEleves = inscriptionRepository.countByAnneeScolaireIdAndStatut(annee.getId(), "active");
        long professeursActifs = getProfesseursActifs(annee, moisSelection).size();
        BigDecimal beneficeNet = totalSelection.subtract(depensesSalairesProfesseurs != null ? depensesSalairesProfesseurs : BigDecimal.ZERO);

        kpis.setRecettesTotal(totalSelection);
        kpis.setDepensesSalairesProfesseurs(depensesSalairesProfesseurs != null ? depensesSalairesProfesseurs : BigDecimal.ZERO);
        kpis.setBeneficeNet(beneficeNet);
        kpis.setTransactionsTotal(transactionsSelection);
        kpis.setElevesPayes(elevesPayesSelection);
        kpis.setElevesImpayes(Math.max(0, totalEleves - elevesPayesSelection));
        kpis.setElevesTotal(totalEleves);
        kpis.setProfesseursActifs(professeursActifs);
        kpis.setTicketMoyen(transactionsSelection > 0
                ? totalSelection.divide(BigDecimal.valueOf(transactionsSelection), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return kpis;
    }

    private DashboardKpisDTO creerKpisVides() {
        DashboardKpisDTO kpis = new DashboardKpisDTO();
        kpis.setRecettesTotal(BigDecimal.ZERO);
        kpis.setDepensesSalairesProfesseurs(BigDecimal.ZERO);
        kpis.setBeneficeNet(BigDecimal.ZERO);
        kpis.setTicketMoyen(BigDecimal.ZERO);
        kpis.setTransactionsTotal(0);
        kpis.setElevesPayes(0);
        kpis.setElevesImpayes(0);
        kpis.setElevesTotal(0);
        kpis.setProfesseursActifs(0);
        return kpis;
    }

    private List<DashboardMonthOptionDTO> construireOptionsMois(List<YearMonth> moisDeAnnee, YearMonth selection) {
        List<DashboardMonthOptionDTO> options = new ArrayList<>();
        for (YearMonth mois : moisDeAnnee) {
            DashboardMonthOptionDTO option = new DashboardMonthOptionDTO();
            option.setValue(mois.toString());
            option.setLabel(formatMonthLabel(mois));
            option.setSelected(mois.equals(selection));
            options.add(option);
        }
        return options;
    }

    private DashboardAnneeOptionDTO toAnneeOption(AnneeScolaire annee) {
        DashboardAnneeOptionDTO option = new DashboardAnneeOptionDTO();
        option.setId(annee.getId());
        option.setLibelle(annee.getLibelle());
        option.setSelected(Boolean.TRUE.equals(annee.getEstActive()));
        return option;
    }

    private AnneeScolaire resoudreAnnee(Long anneeScolaireId, List<AnneeScolaire> annees) {
        if (anneeScolaireId != null) {
            return annees.stream().filter(a -> anneeScolaireId.equals(a.getId())).findFirst().orElse(null);
        }
        return annees.stream().filter(a -> Boolean.TRUE.equals(a.getEstActive())).findFirst().orElseGet(() -> annees.isEmpty() ? null : annees.get(0));
    }

    private YearMonth resoudreMoisSelectionne(String moisSelectionne, List<YearMonth> moisDeAnnee) {
        if (moisSelectionne != null) {
            try {
                YearMonth mois = YearMonth.parse(moisSelectionne);
                if (moisDeAnnee.contains(mois)) {
                    return mois;
                }
            } catch (Exception ignored) {
            }
        }
        YearMonth now = YearMonth.now();
        if (moisDeAnnee.contains(now)) {
            return now;
        }
        return moisDeAnnee.isEmpty() ? YearMonth.now() : moisDeAnnee.get(moisDeAnnee.size() - 1);
    }

    private List<YearMonth> construireMois(LocalDate dateDebut, LocalDate dateFin) {
        List<YearMonth> mois = new ArrayList<>();
        YearMonth courant = YearMonth.from(dateDebut);
        YearMonth fin = YearMonth.from(dateFin);
        while (!courant.isAfter(fin)) {
            mois.add(courant);
            courant = courant.plusMonths(1);
        }
        return mois;
    }

    private BigDecimal sommeMontants(List<Paiement> paiements) {
        return paiements.stream()
                .map(Paiement::getMontant)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculerDepensesSalairesProfesseurs(AnneeScolaire annee, YearMonth mois) {
        if (annee == null || mois == null) {
            return BigDecimal.ZERO;
        }

        return depenseRepository.sumTotalByAnneeScolaireAndDateBetween(
                annee.getId(),
                mois.atDay(1),
                mois.atEndOfMonth());
    }

    public List<Depense> listerDepenses(AnneeScolaire annee, YearMonth mois) {
        if (annee == null || mois == null) {
            return List.of();
        }
        return depenseRepository.findByAnneeScolaire_IdAndDateDepenseBetweenOrderByDateDepenseDesc(
                annee.getId(),
                mois.atDay(1),
                mois.atEndOfMonth());
    }

    public List<VueEmployesDetail> getProfesseursActifs(AnneeScolaire annee, YearMonth mois) {
        if (annee == null || mois == null) {
            return List.of();
        }
        LocalDate debutMois = mois.atDay(1);
        LocalDate finMois = mois.atEndOfMonth();
        return employeService.getProfesseurs().stream()
                .filter(this::isEligibleForSalaryExpense)
                .filter(e -> periodeActiveSurMois(e, debutMois, finMois))
                .toList();
    }

    public byte[] exportTransactionsPdf(DashboardResponseDTO dashboard) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 42, 42);
        PdfWriter.getInstance(doc, out);
        doc.open();

        BaseColor accent = new BaseColor(15, 110, 86);
        BaseColor accentLight = new BaseColor(225, 245, 238);
        BaseColor grey = new BaseColor(95, 95, 95);
        BaseColor border = new BaseColor(225, 225, 225);

        Font title = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, accent);
        Font subtitle = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, grey);
        Font section = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, accent);
        Font whiteBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        Font label = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, grey);
        Font value = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

        addBrandHeader(doc, "TRANSACTIONS DU MOIS", dashboard.getResume(), title, subtitle, accent, accentLight);
        addKpiCards(doc, dashboard.getKpis(), dashboard.getSelectedMonthLabel(), accent, accentLight, whiteBold, label, value);
        addSectionTitle(doc, "Liste détaillée des paiements", section, 8);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2.2f, 1.2f, 1.3f, 1.0f, 1.0f, 1.3f });
        addHeaderCell(table, "Élève", accent, whiteBold);
        addHeaderCell(table, "Classe", accent, whiteBold);
        addHeaderCell(table, "Référence", accent, whiteBold);
        addHeaderCell(table, "Date", accent, whiteBold);
        addHeaderCell(table, "Mode", accent, whiteBold);
        addHeaderCell(table, "Montant", accent, whiteBold);

        if (dashboard.getTransactions() == null || dashboard.getTransactions().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Aucune transaction disponible pour la période sélectionnée.", label));
            empty.setColspan(6);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(12);
            empty.setBorderColor(border);
            table.addCell(empty);
        } else {
            boolean alternate = false;
            for (DashboardTransactionDTO transaction : dashboard.getTransactions()) {
                BaseColor rowBg = alternate ? new BaseColor(250, 250, 250) : BaseColor.WHITE;
                addBodyCell(table, transaction.getEtudiantNom(), rowBg, border, value);
                addBodyCell(table, transaction.getClasseNom(), rowBg, border, label);
                addBodyCell(table, transaction.getReferenceTransaction(), rowBg, border, label);
                addBodyCell(table, transaction.getDateLibelle(), rowBg, border, label);
                addBodyCell(table, transaction.getModePaiement(), rowBg, border, label);
                addBodyCell(table, formatMoney(transaction.getMontant()), rowBg, border, value);
                alternate = !alternate;
            }
        }
        doc.add(table);

        addFooter(doc, accent, subtitle);
        doc.close();
        return out.toByteArray();
    }

    public byte[] exportDashboardPdf(DashboardResponseDTO dashboard) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 42, 42);
        PdfWriter.getInstance(doc, out);
        doc.open();

        BaseColor accent = new BaseColor(37, 99, 235);
        BaseColor accentLight = new BaseColor(235, 241, 255);
        BaseColor grey = new BaseColor(95, 95, 95);
        BaseColor border = new BaseColor(225, 225, 225);

        Font title = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, accent);
        Font subtitle = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, grey);
        Font section = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, accent);
        Font whiteBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        Font label = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, grey);
        Font value = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

        addBrandHeader(doc, "BILAN DU TABLEAU DE BORD", dashboard.getResume(), title, subtitle, accent, accentLight);
        addKpiCards(doc, dashboard.getKpis(), dashboard.getSelectedMonthLabel(), accent, accentLight, whiteBold, label, value);

        addSectionTitle(doc, "Résumé mensuel", section, 8);
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        summary.setWidths(new float[] { 1.2f, 1.8f });
        addSummaryCell(summary, "Recettes du mois", formatMoney(dashboard.getKpis().getRecettesTotal()), border, label, value);
        addSummaryCell(summary, "Dépenses du mois", formatMoney(dashboard.getKpis().getDepensesSalairesProfesseurs()), border, label, value);
        addSummaryCell(summary, "Bénéfice net", formatMoney(dashboard.getKpis().getBeneficeNet()), border, label, value);
        addSummaryCell(summary, "Transactions uniques", String.valueOf(dashboard.getKpis().getTransactionsTotal()), border, label, value);
        addSummaryCell(summary, "Élèves ayant payé", String.valueOf(dashboard.getKpis().getElevesPayes()), border, label, value);
        addSummaryCell(summary, "Professeurs actifs", String.valueOf(dashboard.getKpis().getProfesseursActifs()), border, label, value);
        doc.add(summary);

        addSectionTitle(doc, "Dépenses détaillées", section, 8);
        PdfPTable depenseTable = new PdfPTable(6);
        depenseTable.setWidthPercentage(100);
        depenseTable.setWidths(new float[] { 1.0f, 1.4f, 2.6f, 0.9f, 0.9f, 1.1f });
        addHeaderCell(depenseTable, "Date", accent, whiteBold);
        addHeaderCell(depenseTable, "Type", accent, whiteBold);
        addHeaderCell(depenseTable, "Description", accent, whiteBold);
        addHeaderCell(depenseTable, "P.U.", accent, whiteBold);
        addHeaderCell(depenseTable, "Qté", accent, whiteBold);
        addHeaderCell(depenseTable, "Total", accent, whiteBold);

        List<Depense> depensesMois = listerDepenses(findAnnee(dashboard.getAnneeScolaireId()), YearMonth.parse(dashboard.getSelectedMonth()));
        if (depensesMois.isEmpty()) {
            PdfPCell emptyDepense = new PdfPCell(new Phrase("Aucune dépense enregistrée pour la période sélectionnée.", label));
            emptyDepense.setColspan(6);
            emptyDepense.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyDepense.setPadding(12);
            emptyDepense.setBorderColor(border);
            depenseTable.addCell(emptyDepense);
        } else {
            boolean alternate = false;
            for (Depense depense : depensesMois) {
                BaseColor rowBg = alternate ? new BaseColor(250, 250, 250) : BaseColor.WHITE;
                addBodyCell(depenseTable, depense.getDateDepense() != null ? depense.getDateDepense().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—", rowBg, border, label);
                addBodyCell(depenseTable, depense.getTypeDepense() != null ? depense.getTypeDepense().getLibelle() : "—", rowBg, border, label);
                addBodyCell(depenseTable, depense.getDescription(), rowBg, border, label);
                addBodyCell(depenseTable, formatMoney(depense.getPrixUnitaire()), rowBg, border, value);
                addBodyCell(depenseTable, formatQuantity(depense.getQuantite()), rowBg, border, value);
                addBodyCell(depenseTable, formatMoney(depense.getTotal()), rowBg, border, value);
                alternate = !alternate;
            }
        }
        doc.add(depenseTable);

        addSectionTitle(doc, "Répartition des mois", section, 12);
        PdfPTable months = new PdfPTable(4);
        months.setWidthPercentage(100);
        months.setWidths(new float[] { 1.5f, 1.5f, 1.5f, 1.5f });
        for (DashboardMonthlyStatDTO point : dashboard.getSerieMensuelle()) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBorderColor(border);
            cell.setBackgroundColor(point.isSelected() ? accentLight : BaseColor.WHITE);
            cell.addElement(new Paragraph(point.getLabel(), new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, accent)));
            cell.addElement(new Paragraph("Recettes: " + formatMoney(point.getRecettes()), label));
            cell.addElement(new Paragraph("Dépenses: " + formatMoney(point.getDepensesSalairesProfesseurs()), label));
            cell.addElement(new Paragraph("Net: " + formatMoney(point.getBeneficeNet()), value));
            months.addCell(cell);
        }
        if (!dashboard.getSerieMensuelle().isEmpty()) {
            doc.add(months);
        }

        addFooter(doc, accent, subtitle);
        doc.close();
        return out.toByteArray();
    }

    private boolean isEligibleForSalaryExpense(VueEmployesDetail employe) {
        return employe != null
                && employe.getIsActive() != null
                && employe.getIsActive()
                && employe.getRoleNom() != null
                && employe.getRoleNom().equalsIgnoreCase("professeur");
    }

    private boolean periodeActiveSurMois(VueEmployesDetail employe, LocalDate debutMois, LocalDate finMois) {
        if (employe == null) {
            return false;
        }
        LocalDate dateDebut = employe.getDateDebut();
        LocalDate dateFin = employe.getDateFin();
        boolean startOk = dateDebut == null || !dateDebut.isAfter(finMois);
        boolean endOk = dateFin == null || !dateFin.isBefore(debutMois);
        return startOk && endOk;
    }

    private void addBrandHeader(Document doc, String titleText, String subtitleText, Font title, Font subtitle,
                                BaseColor accent, BaseColor accentLight) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 2.2f, 1f });
        PdfPCell left = new PdfPCell();
        left.setPadding(16);
        left.setBackgroundColor(accent);
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("LycéePro", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE)));
        left.addElement(new Paragraph(titleText, title));
        left.addElement(new Paragraph(subtitleText != null ? subtitleText : "", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.WHITE)));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setPadding(16);
        right.setBackgroundColor(accentLight);
        right.setBorder(Rectangle.NO_BORDER);
        right.addElement(new Paragraph("Tableau directeur", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, accent)));
        right.addElement(new Paragraph("Document généré automatiquement", subtitle));
        header.addCell(right);
        doc.add(header);

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(6);
        doc.add(spacer);
    }

    private void addKpiCards(Document doc, DashboardKpisDTO kpis, String periodeLabel,
                             BaseColor accent, BaseColor accentLight, Font whiteBold,
                             Font label, Font value) throws Exception {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        table.setWidths(new float[] { 1f, 1f, 1f });

        addKpiCell(table, "Recettes", formatMoney(kpis != null ? kpis.getRecettesTotal() : BigDecimal.ZERO), accent, whiteBold);
        addKpiCell(table, "Dépenses", formatMoney(kpis != null ? kpis.getDepensesSalairesProfesseurs() : BigDecimal.ZERO), accent, whiteBold);
        addKpiCell(table, "Bénéfice net", formatMoney(kpis != null ? kpis.getBeneficeNet() : BigDecimal.ZERO), accent, whiteBold);
        doc.add(table);
    }

    private void addKpiCell(PdfPTable table, String labelText, String valueText, BaseColor accent, Font whiteBold) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBackgroundColor(accent);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph(labelText.toUpperCase(Locale.ROOT), new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE)));
        cell.addElement(new Paragraph(valueText, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE)));
        table.addCell(cell);
    }

    private void addSectionTitle(Document doc, String text, Font section, float spacingAfter) throws Exception {
        Paragraph p = new Paragraph(text, section);
        p.setSpacingBefore(4);
        p.setSpacingAfter(spacingAfter);
        doc.add(p);
    }

    private void addHeaderCell(PdfPTable table, String text, BaseColor bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(bg);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, BaseColor bg, BaseColor border, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(border);
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, String labelText, String valueText, BaseColor border, Font label, Font value) {
        PdfPCell left = new PdfPCell(new Phrase(labelText, label));
        left.setPadding(8);
        left.setBorderColor(border);
        table.addCell(left);

        PdfPCell right = new PdfPCell(new Phrase(valueText, value));
        right.setPadding(8);
        right.setBorderColor(border);
        table.addCell(right);
    }

    private void addFooter(Document doc, BaseColor accent, Font subtitle) throws Exception {
        Paragraph footer = new Paragraph("Document généré automatiquement par le système de gestion scolaire.", subtitle);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(14);
        doc.add(footer);
    }

    private AnneeScolaire findAnnee(Long anneeId) {
        if (anneeId == null) {
            return null;
        }
        return anneeScolaireRepository.findById(anneeId).orElse(null);
    }

    private String formatQuantity(BigDecimal quantity) {
        return String.format(Locale.FRANCE, "%s", quantity != null ? quantity.stripTrailingZeros().toPlainString() : "0");
    }

    private String formatMoney(BigDecimal amount) {
        return String.format(Locale.FRANCE, "%,.0f Ar", amount != null ? amount : BigDecimal.ZERO);
    }

    private String formatMoneyForPdf(BigDecimal amount) {
        return formatMoney(amount);
    }

    private long compterTransactions(List<Paiement> paiements) {
        return paiements.stream()
                .map(Paiement::getReferenceTransaction)
                .filter(ref -> ref != null && !ref.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .size();
    }

    private long compterElevesPayes(List<Paiement> paiements) {
        return paiements.stream()
                .map(Paiement::getInscription)
                .filter(inscription -> inscription != null && inscription.getEtudiantId() != null)
                .map(inscription -> inscription.getEtudiantId())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .size();
    }

    private List<DashboardTransactionDTO> construireTransactions(List<Paiement> paiements) {
        Map<String, List<Paiement>> parReference = paiements.stream()
                .collect(Collectors.groupingBy(p -> {
                    String ref = p.getReferenceTransaction();
                    return ref != null && !ref.isBlank() ? ref : "SANS-REFERENCE-" + p.getId();
                }, LinkedHashMap::new, Collectors.toList()));

        List<DashboardTransactionDTO> transactions = new ArrayList<>();
        for (List<Paiement> groupe : parReference.values()) {
            if (groupe.isEmpty()) {
                continue;
            }
            Paiement premier = groupe.get(0);
            DashboardTransactionDTO dto = new DashboardTransactionDTO();
            dto.setReferenceTransaction(premier.getReferenceTransaction() != null ? premier.getReferenceTransaction() : "Sans référence");
            dto.setDatePaiement(premier.getDatePaiement());
            dto.setDateLibelle(premier.getDatePaiement() != null ? premier.getDatePaiement().toString() : "—");
            dto.setMontant(sommeMontants(groupe));
            dto.setModePaiement(groupe.stream()
                    .map(Paiement::getModePaiement)
                    .filter(mode -> mode != null && !mode.isBlank())
                    .distinct()
                    .collect(Collectors.joining(" / ")));
            dto.setEtudiantNom(construireNomEtudiant(premier));
            dto.setClasseNom(construireClasseNom(premier));
            dto.setMatricule(construireMatricule(premier));
            dto.setEcheanceLabel(construireEcheanceLabel(premier));
            transactions.add(dto);
        }

        transactions.sort(Comparator.comparing(DashboardTransactionDTO::getDatePaiement, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return transactions.stream().limit(8).toList();
    }

    private String construireNomEtudiant(Paiement paiement) {
        if (paiement.getInscription() == null || paiement.getInscription().getEtudiant() == null) {
            return "Élève inconnu";
        }
        return paiement.getInscription().getEtudiant().getNom() + " " + paiement.getInscription().getEtudiant().getPrenom();
    }

    private String construireClasseNom(Paiement paiement) {
        if (paiement.getInscription() == null || paiement.getInscription().getClasse() == null) {
            return "—";
        }
        return paiement.getInscription().getClasse().getNom();
    }

    private String construireMatricule(Paiement paiement) {
        if (paiement.getInscription() == null || paiement.getInscription().getEtudiant() == null) {
            return "";
        }
        return paiement.getInscription().getEtudiant().getMatricule();
    }

    private String construireEcheanceLabel(Paiement paiement) {
        if (paiement.getEcheance() == null || paiement.getEcheance().getNumeroTranche() == null) {
            return "Paiement";
        }
        return "Tranche " + paiement.getEcheance().getNumeroTranche();
    }

    private String formatMonthLabel(YearMonth month) {
        String label = month.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return label.substring(0, 1).toUpperCase(Locale.ROOT) + label.substring(1) + " " + month.getYear();
    }
}