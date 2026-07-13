package com.ecole.controller;

import com.ecole.dto.Secretaire.*;
import com.ecole.entity.*;
import com.ecole.service.EleveService;
import com.ecole.service.DepenseService;
import com.ecole.service.EcolageMensuelService;
import com.ecole.service.PaiementService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SecretaireController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private EleveService eleveService;

    @Autowired
    private DepenseService depenseService;

    @Autowired
    private EcolageMensuelService ecolageMensuelService;

    // ─── PAIEMENT ────────────────────────────────────────────────

    @GetMapping("/secretariat/paiement")
    public String paiement(Model model) {
        List<Inscription> inscriptions;
        try {
            inscriptions = paiementService.getInscriptionsActives();
        } catch (RuntimeException e) {
            inscriptions = List.of();
            model.addAttribute("warning",
                    "Aucune année scolaire active ou aucune inscription n'est disponible pour le moment.");
        }

        if (inscriptions.isEmpty() && !model.containsAttribute("warning")) {
            model.addAttribute("warning", "Aucune inscription active n'est disponible pour enregistrer un paiement.");
        }

        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("classes", eleveService.listerClasses());
        model.addAttribute("pageTitle", "Ajouter un Paiement");
        return "Secretaire/paiement";
    }

    @GetMapping("/secretariat/paiement/echeances")
    @ResponseBody
    public List<Echeance> getEcheances(@RequestParam Long inscriptionId) {
        return paiementService.getMoisParInscription(inscriptionId);
    }

    // Enregistrement du paiement
    // Modifier le POST paiement
    @PostMapping("/secretariat/paiement")
    public String enregistrerPaiement(
            @RequestParam Long inscriptionId,
            @RequestParam Long echeanceId,
            @RequestParam List<BigDecimal> montants,
            @RequestParam List<String> modesPaiement,
            RedirectAttributes redirectAttributes) {

        try {
            // Construire le DTO
            PaiementGroupeDTO dto = new PaiementGroupeDTO();
            dto.setInscriptionId(inscriptionId);
            dto.setEcheanceId(echeanceId);

            List<PaiementGroupeDTO.LignePaiement> lignes = new ArrayList<>();
            for (int i = 0; i < montants.size(); i++) {
                PaiementGroupeDTO.LignePaiement ligne = new PaiementGroupeDTO.LignePaiement();
                ligne.setMontant(montants.get(i));
                ligne.setModePaiement(modesPaiement.get(i));
                lignes.add(ligne);
            }
            dto.setLignes(lignes);

            String refGroupe = paiementService.enregistrerPaiementGroupe(dto);
            return "redirect:/secretariat/paiements/groupe/" + refGroupe + "/facture";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/secretariat/paiement";
        }
    }

    // Page facture détail
    @GetMapping("/secretariat/paiements/groupe/{reference}/facture")
    public String factureDetail(@PathVariable String reference, Model model) {
        List<Paiement> lignes = paiementService.getPaiementsByReference(reference);
        if (lignes.isEmpty())
            return "redirect:/secretariat/paiements";

        BigDecimal total = lignes.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("lignes", lignes);
        model.addAttribute("reference", reference);
        model.addAttribute("total", total);
        model.addAttribute("paiement", lignes.get(0));
        model.addAttribute("pageTitle", "Facture — " + reference);
        return "Secretaire/facture_detail";
    }

    // Export PDF facture groupe
    @GetMapping("/secretariat/paiements/groupe/{reference}/pdf")
    public void factureGroupePdf(@PathVariable String reference,
            HttpServletResponse response) throws Exception {
        List<Paiement> lignes = paiementService.getPaiementsByReference(reference);
        BigDecimal total = lignes.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        byte[] pdf = paiementService.exportFactureGroupePdf(reference, lignes, total);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=facture-" + reference + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/secretariat/paiements")
    public String listePaiements(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String classe,
            @RequestParam(required = false) String modePaiement,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Model model) {

        List<Paiement> paiements = paiementService.getPaiementsFiltres(
                nom, classe, modePaiement, dateDebut, dateFin);
        model.addAttribute("paiements", paiements);
        model.addAttribute("nom", nom);
        model.addAttribute("classe", classe);
        model.addAttribute("modePaiement", modePaiement);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("classes", eleveService.listerClasses());
        model.addAttribute("pageTitle", "Liste des Paiements");
        return "Secretaire/liste_paiements";
    }

    // Facture PDF par id paiement simple
    @GetMapping("/secretariat/paiements/simple/{id}/pdf")
    public void factturePdf(@PathVariable Integer id,
            HttpServletResponse response) throws Exception {
        byte[] pdf = paiementService.exportFacturePdf(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=facture-REC-" + id + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    // ─── BILAN ───────────────────────────────────────────────────

    @GetMapping("/secretariat/bilan")
    public String bilan(Model model) {
        BilanGlobalDTO bilan = paiementService.getBilanGlobal();
        if (bilan.getLignes() == null || bilan.getLignes().isEmpty()) {
            model.addAttribute("warning",
                    "Aucune année scolaire active ou aucune donnée de paiement n'est disponible pour générer un bilan.");
        }
        model.addAttribute("bilan", bilan);
        model.addAttribute("pageTitle", "Bilan de Paiement");
        return "Secretaire/bilan";
    }

    @GetMapping("/secretariat/depenses")
    public String depenses(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String periode,
            Model model) {
        List<AnneeScolaire> annees = depenseService.getAnneesScolaires();
        Long selectedAnneeId = anneeScolaireId;
        if (selectedAnneeId == null) {
            selectedAnneeId = annees.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getEstActive()))
                    .map(AnneeScolaire::getId)
                    .findFirst()
                    .orElse(annees.isEmpty() ? null : annees.get(0).getId());
        }

        YearMonth selectedPeriode = selectedAnneeId != null
                ? depenseService.resolvePeriode(periode, selectedAnneeId)
                : YearMonth.now();

        List<Depense> depenses = selectedAnneeId != null
                ? depenseService.getDepenses(selectedAnneeId, selectedPeriode)
                : List.of();

        model.addAttribute("anneesScolaires", annees);
        model.addAttribute("periodes",
                selectedAnneeId != null ? depenseService.getPeriodes(selectedAnneeId) : List.of());
        model.addAttribute("typesDepenses", depenseService.getTypesDepenses());
        model.addAttribute("depenses", depenses);
        model.addAttribute("totalDepenses",
                selectedAnneeId != null ? depenseService.totalDepenses(selectedAnneeId, selectedPeriode)
                        : BigDecimal.ZERO);
        model.addAttribute("selectedAnneeId", selectedAnneeId);
        model.addAttribute("selectedPeriode", selectedPeriode != null ? selectedPeriode.toString() : null);
        model.addAttribute("pageTitle", "Dépenses");
        model.addAttribute("currentRole", "secretariat");
        return "Secretaire/depenses";
    }

    @PostMapping("/secretariat/depenses")
    public String enregistrerDepense(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String periode,
            @RequestParam Long typeDepenseId,
            @RequestParam String description,
            @RequestParam BigDecimal prixUnitaire,
            @RequestParam BigDecimal quantite,
            @RequestParam(required = false) LocalDate dateDepense,
            RedirectAttributes redirectAttributes) {
        try {
            depenseService.saveDepense(anneeScolaireId, typeDepenseId, description, prixUnitaire, quantite,
                    dateDepense);
            redirectAttributes.addFlashAttribute("success", "Dépense enregistrée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return buildDepenseRedirect(anneeScolaireId, periode);
    }

    @PostMapping("/secretariat/depenses/types")
    public String enregistrerTypeDepense(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) String periode,
            @RequestParam String libelle,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        try {
            depenseService.saveTypeDepense(libelle, description);
            redirectAttributes.addFlashAttribute("success", "Type de dépense ajouté avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return buildDepenseRedirect(anneeScolaireId, periode);
    }

    @GetMapping("/secretariat/bilan/export/pdf")
    public void exportPdf(HttpServletResponse response) throws Exception {
        BilanGlobalDTO bilan = paiementService.getBilanGlobal();
        byte[] pdf = paiementService.exportPdf(bilan);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=bilan_paiements.pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/secretariat/bilan/export/excel")
    public void exportExcel(HttpServletResponse response) throws Exception {
        BilanGlobalDTO bilan = paiementService.getBilanGlobal();
        byte[] excel = paiementService.exportExcel(bilan);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=bilan_paiements.xlsx");
        response.setContentLength(excel.length);
        response.getOutputStream().write(excel);
    }

    // ─── INSCRIPTION / RÉINSCRIPTION ─────────────────────────────

    @GetMapping("/secretariat/inscription")
    public String inscription(Model model) {
        model.addAttribute("classes", eleveService.listerClasses());
        model.addAttribute("pageTitle", "Inscription — Nouvel Élève");
        return "Secretaire/inscription";
    }

    @PostMapping("/secretariat/inscription")
    public String enregistrerInscription(@ModelAttribute AjoutEleveDTO dto,
            RedirectAttributes redirectAttrs) {
        try {
            ProfilEtudiant eleve = eleveService.inscrireEleveComplet(dto);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Élève " + eleve.getPrenom() + " " + eleve.getNom()
                            + " inscrit avec succès (matricule : " + eleve.getMatricule() + ") !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/secretariat/inscription";
        }
        return "redirect:/secretariat/eleves";
    }

    @GetMapping("/secretariat/reinscription")
    public String reinscription(
            @RequestParam(required = false) String search,
            Model model) {
        model.addAttribute("classes", eleveService.listerClasses());
        model.addAttribute("search", search);
        if (search != null && !search.isBlank()) {
            model.addAttribute("resultats", eleveService.rechercherElevesPourReinscription(search));
        }
        model.addAttribute("pageTitle", "Réinscription");
        return "Secretaire/reinscription";
    }

    @PostMapping("/secretariat/reinscription/effectuer")
    public String effectuerReinscription(
            @RequestParam Long etudiantId,
            @RequestParam Long classeId,
            RedirectAttributes redirectAttrs) {
        try {
            Inscription insc = eleveService.reinscrireEleve(etudiantId, classeId);
            ProfilEtudiant etudiant = eleveService.getProfil(etudiantId) != null
                    ? new ProfilEtudiant()
                    : null;
            redirectAttrs.addFlashAttribute("successMessage",
                    "Réinscription effectuée avec succès !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/secretariat/eleves";
    }

    // ─── ÉLÈVES ──────────────────────────────────────────────────

    @GetMapping("/secretariat/eleves")
    public String eleves(
            Model model,
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("eleves", eleveService.rechercherEleves(search));
        } else if (classeId != null) {
            model.addAttribute("eleves", eleveService.listerParClasse(classeId));
        } else if (niveau != null && !niveau.isBlank()) {
            model.addAttribute("eleves", eleveService.listerParNiveau(niveau));
        } else {
            model.addAttribute("eleves", eleveService.listerTousEleves());
        }
        model.addAttribute("classes", eleveService.listerClasses());
        model.addAttribute("niveauActif", niveau);
        model.addAttribute("classeIdActif", classeId);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Liste des Élèves");
        return "Secretaire/eleves";
    }

    private String buildDepenseRedirect(Long anneeScolaireId, String periode) {
        StringBuilder redirect = new StringBuilder("redirect:/secretariat/depenses");
        boolean first = true;
        if (anneeScolaireId != null) {
            redirect.append(first ? "?" : "&").append("anneeScolaireId=").append(anneeScolaireId);
            first = false;
        }
        if (periode != null && !periode.isBlank()) {
            redirect.append(first ? "?" : "&").append("periode=").append(periode);
        }
        return redirect.toString();
    }

    @GetMapping("/secretariat/profil/{id}")
    public String profil(@PathVariable Long id, Model model) {
        model.addAttribute("eleve", eleveService.getProfil(id));
        model.addAttribute("pageTitle", "Profil de l'Élève");
        return "Secretaire/profil_eleve";
    }

    @GetMapping("/secretariat/profil")
    public String profilRedirect() {
        return "redirect:/secretariat/eleves";
    }

    @GetMapping("/secretariat/profil/{id}/export/pdf")
    public ResponseEntity<byte[]> exportProfilPdf(@PathVariable Long id) {
        EleveProfilDTO eleve = eleveService.getProfil(id);
        String html = buildHtmlProfil(eleve);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        String filename = "profil_" + eleve.getMatricule() + ".html";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_HTML)
                .body(bytes);
    }

    @GetMapping("/secretariat/profil/{id}/export/csv")
    public ResponseEntity<byte[]> exportProfilCsv(@PathVariable Long id) {
        EleveProfilDTO eleve = eleveService.getProfil(id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true, StandardCharsets.UTF_8);
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);
        pw.println("Matricule;Nom;Prénom;Classe;Date naissance;Quartier;Adresse;Nom parent;Contact parent");
        pw.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                safe(eleve.getMatricule()), safe(eleve.getNom()), safe(eleve.getPrenom()),
                safe(eleve.getNomClasse()),
                eleve.getDateNaissance() != null ? eleve.getDateNaissance().toString() : "",
                safe(eleve.getCommune()), safe(eleve.getAdresse()),
                safe(eleve.getNomParent() != null ? eleve.getPrenomParent() + " " + eleve.getNomParent() : ""),
                safe(eleve.getTelephoneParent()));
        if (eleve.getHistoriquesPaiements() != null && !eleve.getHistoriquesPaiements().isEmpty()) {
            pw.println();
            pw.println("Historique paiements");
            pw.println("Mois;Date paiement;Montant;Statut");
            for (PaiementHistoriqueDTO p : eleve.getHistoriquesPaiements()) {
                pw.printf("%s;%s;%s;%s%n",
                        safe(p.getMoisLabel()),
                        p.getDatePaiement() != null ? p.getDatePaiement().toString() : "",
                        p.getMontant() != null ? p.getMontant().toPlainString() : "",
                        safe(p.getStatut()));
            }
        }
        pw.flush();
        String filename = "profil_" + eleve.getMatricule() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(baos.toByteArray());
    }

    @PostMapping("/secretariat/profil/{id}/demande-modification")
    public String demandeModification(
            @PathVariable Long id,
            @RequestParam String champModifie,
            @RequestParam(required = false) String ancienneValeur,
            @RequestParam String nouvelleValeur,
            @RequestParam(required = false) String motif,
            RedirectAttributes redirectAttrs) {
        try {
            eleveService.soumettreDemandeModification(id, champModifie, ancienneValeur, nouvelleValeur, motif);
            redirectAttrs.addFlashAttribute("successMessage", "Demande de modification soumise avec succès.");
        } catch (Exception e) {
            throw e;
        }
        return "redirect:/secretariat/profil/" + id;
    }

    @PostMapping("/secretariat/eleves/ajouter")
    public String ajouterEleve(@ModelAttribute AjoutEleveDTO dto,
            RedirectAttributes redirectAttrs) {
        try {
            eleveService.ajouterEleve(dto);
            redirectAttrs.addFlashAttribute("successMessage", "Élève ajouté avec succès !");
        } catch (Exception e) {
            throw e;
        }
        return "redirect:/secretariat/eleves";
    }

    private String safe(String s) {
        if (s == null)
            return "";
        return s.replace(";", ",").replace("\n", " ").replace("\r", "");
    }

    private String buildHtmlProfil(EleveProfilDTO e) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="UTF-8"/>
                  <title>Profil – %s %s</title>
                  <style>
                    body { font-family: Arial, sans-serif; padding: 30px; color: #111; }
                    h1 { font-size: 22px; margin-bottom: 4px; }
                    .sub { color: #666; font-size: 13px; margin-bottom: 24px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 16px; }
                    th, td { border: 1px solid #ddd; padding: 8px 12px; font-size: 13px; text-align: left; }
                    th { background: #f4f4f4; }
                    h2 { font-size: 15px; margin-top: 28px; }
                    @media print { body { padding: 0; } }
                  </style>
                </head>
                <body>
                  <h1>%s %s</h1>
                  <div class="sub">%s &nbsp;·&nbsp; %s</div>
                  <h2>Informations personnelles</h2>
                  <table>
                    <tr><th>Date de naissance</th><td>%s</td></tr>
                    <tr><th>Quartier</th><td>%s</td></tr>
                    <tr><th>Adresse</th><td>%s</td></tr>
                    <tr><th>Date d'inscription</th><td>%s</td></tr>
                    <tr><th>Parent / Tuteur</th><td>%s %s</td></tr>
                    <tr><th>Contact parent</th><td>%s</td></tr>
                  </table>
                  <h2>Historique de paiement</h2>
                  <table>
                    <tr><th>Mois</th><th>Date paiement</th><th>Montant</th><th>Statut</th></tr>
                    %s
                  </table>
                  <script>window.onload = function(){ window.print(); }</script>
                </body>
                </html>
                """.formatted(
                e.getPrenom(), e.getNom(),
                e.getPrenom(), e.getNom(),
                e.getMatricule(), e.getNomClasse() != null ? e.getNomClasse() : "",
                e.getDateNaissance() != null ? e.getDateNaissance().toString() : "—",
                e.getCommune() != null ? e.getCommune() : "—",
                e.getAdresse() != null ? e.getAdresse() : "—",
                e.getDateInscription() != null ? e.getDateInscription().toString() : "—",
                e.getPrenomParent() != null ? e.getPrenomParent() : "",
                e.getNomParent() != null ? e.getNomParent() : "—",
                e.getTelephoneParent() != null ? e.getTelephoneParent() : "—",
                buildPaiementsRows(e));
    }

    private String buildPaiementsRows(EleveProfilDTO e) {
        if (e.getHistoriquesPaiements() == null || e.getHistoriquesPaiements().isEmpty()) {
            return "<tr><td colspan='4' style='color:#999;'>Aucun paiement enregistré.</td></tr>";
        }
        StringBuilder sb = new StringBuilder();
        for (PaiementHistoriqueDTO p : e.getHistoriquesPaiements()) {
            sb.append("<tr><td>").append(p.getMoisLabel()).append("</td>")
                    .append("<td>").append(p.getDatePaiement() != null ? p.getDatePaiement() : "—").append("</td>")
                    .append("<td>").append(p.getMontant() != null ? p.getMontant().toPlainString() + " Ar" : "—")
                    .append("</td>")
                    .append("<td>").append(p.getStatut()).append("</td></tr>");
        }
        return sb.toString();
    }

    // ─── ÉCOLAGE MENSUEL ─────────────────────────────────────────

    @GetMapping("/secretariat/ecolage")
    public String ecolage(Model model) {
        model.addAttribute("tarifs", ecolageMensuelService.getTarifsAnneeActive());
        model.addAttribute("niveaux", ecolageMensuelService.getAllNiveaux());
        model.addAttribute("pageTitle", "Configuration Écolage Mensuel");
        return "Secretaire/ecolage";
    }

    @PostMapping("/secretariat/ecolage")
    public String configurerEcolage(
            @RequestParam Long niveauId,
            @RequestParam BigDecimal montant,
            RedirectAttributes redirectAttrs) {
        try {
            ecolageMensuelService.configurer(niveauId, montant);
            redirectAttrs.addFlashAttribute("success", "Écolage configuré avec succès !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/secretariat/ecolage";
    }

}
