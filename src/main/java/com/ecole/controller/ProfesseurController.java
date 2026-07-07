package com.ecole.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecole.entity.Absence;
import com.ecole.entity.AffectationEnseignement;
import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Classe;
import com.ecole.entity.EmploiDuTemps;
import com.ecole.entity.HoraireEdt;
import com.ecole.entity.Inscription;
import com.ecole.entity.Matiere;
import com.ecole.entity.Note;
import com.ecole.entity.Periode;
import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.ProfilsProfesseurs;
import com.ecole.entity.Salle;
import com.ecole.entity.Seance;
import com.ecole.entity.SupportCours;
import com.ecole.entity.TitulaireClasse;
import com.ecole.entity.User;
import com.ecole.service.AbsenceService;
import com.ecole.service.AffectationEnseignementService;
import com.ecole.service.AnneeScolaireService;
import com.ecole.service.ClasseService;
import com.ecole.service.EmploiDuTempsService;
import com.ecole.service.InscriptionService;
import com.ecole.service.MatiereService;
import com.ecole.service.NoteService;
import com.ecole.service.PeriodeService;
import com.ecole.service.ProfilEtudiantService;
import com.ecole.service.ProfilsProfesseursService;
import com.ecole.service.SalleService;
import com.ecole.service.SeanceService;
import com.ecole.service.SupportCoursService;
import com.ecole.service.TitulaireClasseService;
import com.ecole.service.TypeFichierService;
import com.ecole.service.UserService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@PreAuthorize("hasRole('PROFESSEUR')")
public class ProfesseurController {

    @Autowired
    private AffectationEnseignementService affectationEnseignementService;

    @Autowired
    private InscriptionService inscriptionService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private ClasseService classeService;

    @Autowired
    private MatiereService matiereService;

    @Autowired
    private ProfilEtudiantService profilEtudiantService;

    @Autowired
    private SupportCoursService supportCoursService;

    @Autowired
    private ProfilsProfesseursService ProfilsProfesseursService;

    @Autowired
    private PeriodeService periodeService;

    @Autowired
    private TitulaireClasseService titulaireClasseService;

    @Autowired
    private TypeFichierService typeFichierService;

    @Autowired
    private AnneeScolaireService anneeScolaireService;

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    @Autowired
    private SalleService salleService;

    @Autowired
    private AbsenceService absenceService;

    @Autowired
    private com.ecole.repository.AbsenceRepository absenceRepository;

    @Autowired
    private com.ecole.service.EdtService edtService;

    @Autowired
    private SeanceService seanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/professeur/emploi")
    public String emploi(Model model) {
        model.addAttribute("pageTitle", "Emploi du Temps");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();
        List<HoraireEdt> horaires = edtService.getHoraires(null);
        Map<Integer, String> jours = getJoursSemaine();
        Map<Long, Map<Integer, Map<String, Object>>> parHoraire = buildParHoraire(professeurId, horaires);
        
        model.addAttribute("horaires", horaires);
        model.addAttribute("jours", jours);
        model.addAttribute("parHoraire", parHoraire);
        return "Professeur/calendar";
    }

    @GetMapping("/professeur/emploi/pdf")
    public void exporterEmploiPdf(HttpServletResponse response) throws IOException {
        Long professeurId = getAuthenticatedProfesseurId();
        List<HoraireEdt> horaires = edtService.getHoraires(null);
        Map<Integer, String> jours = getJoursSemaine();
        Map<Long, Map<Integer, Map<String, Object>>> parHoraire = buildParHoraire(professeurId, horaires);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=emploi_du_temps_professeur_" + professeurId + ".pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.WHITE);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.WHITE);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell bannerCell = new PdfPCell();
        bannerCell.setBorder(PdfPCell.NO_BORDER);
        bannerCell.setBackgroundColor(new java.awt.Color(15, 23, 42));
        bannerCell.setPadding(16);
        bannerCell.addElement(new Paragraph("Emploi du temps du professeur", fontTitle));
        Paragraph subtitle = new Paragraph("Planning hebdomadaire consolidé pour impression ou archivage", fontSubtitle);
        subtitle.setSpacingAfter(2);
        bannerCell.addElement(subtitle);
        banner.addCell(bannerCell);
        document.add(banner);

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(1 + jours.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        table.setWidths(buildScheduleColumnWidths(jours.size()));

        addHeaderCell(table, "Heure", fontHeader);
        for (String jourLibelle : jours.values()) {
            addHeaderCell(table, jourLibelle, fontHeader);
        }

        for (HoraireEdt horaire : horaires) {
            PdfPCell timeCell = new PdfPCell();
            timeCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
            timeCell.setBorderColor(new java.awt.Color(226, 232, 240));
            timeCell.setPadding(8);
            timeCell.addElement(new Paragraph(formatTimeRange(horaire.getHeureDebut(), horaire.getHeureFin()), fontHeader));
            timeCell.addElement(new Paragraph(horaire.getLibelle() != null ? horaire.getLibelle() : "", fontCell));
            table.addCell(timeCell);

            Map<Integer, Map<String, Object>> creneauxParJour = parHoraire.get(horaire.getId());
            for (Integer jourIndex : jours.keySet()) {
                PdfPCell cell = new PdfPCell();
                cell.setBorderColor(new java.awt.Color(226, 232, 240));
                cell.setPadding(8);
                cell.setMinimumHeight(48f);

                Map<String, Object> creneau = creneauxParJour != null ? creneauxParJour.get(jourIndex) : null;
                if (creneau == null) {
                    Paragraph libre = new Paragraph("Libre", fontCell);
                    libre.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(libre);
                } else {
                    Paragraph matiere = new Paragraph(String.valueOf(creneau.get("matiereNom")), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
                    Paragraph classe = new Paragraph(String.valueOf(creneau.get("classeNom")) + " · " + String.valueOf(creneau.get("salleNom")), fontCell);
                    Paragraph heures = new Paragraph(String.valueOf(creneau.get("heureDebut")) + " - " + String.valueOf(creneau.get("heureFin")), fontCell);
                    cell.addElement(matiere);
                    cell.addElement(classe);
                    cell.addElement(heures);
                }

                table.addCell(cell);
            }
        }

        document.add(table);

        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        footerCell.setBorderColor(new java.awt.Color(226, 232, 240));
        footerCell.setPadding(10);
        footerCell.addElement(new Paragraph("Document généré automatiquement depuis le planning du professeur.", fontCell));
        footer.addCell(footerCell);
        document.add(footer);

        document.close();
    }

    @GetMapping("/professeur/absences")
    public String absences(@RequestParam(required = false) Long emploiDuTempsId,
                          @RequestParam(required = false) String matiere,
                          @RequestParam(required = false) String classe,
                          @RequestParam(required = false) String salle,
                          @RequestParam(required = false) String heureDebut,
                          @RequestParam(required = false) String heureFin,
                          @RequestParam(required = false) Long classeId,
                          Model model) {
        model.addAttribute("pageTitle", "Gestion des Absences");
        model.addAttribute("currentRole", "professeur");
        
        model.addAttribute("emploiDuTempsId", emploiDuTempsId);
        model.addAttribute("matiere", matiere);
        model.addAttribute("classe", classe);
        model.addAttribute("salle", salle);
        model.addAttribute("heureDebut", heureDebut);
        model.addAttribute("heureFin", heureFin);
        model.addAttribute("classeId", classeId);
        
        // Get students for the class
        List<Inscription> inscriptions = null;
        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        
        if (classeId != null) {
            inscriptions = inscriptionService.findByClasseId(classeId);
            for (Inscription inscription : inscriptions) {
                ProfilEtudiant etudiant = profilEtudiantService.findById(inscription.getEtudiantId()).orElse(null);
                if (etudiant != null) {
                    etudiantProfiles.put(inscription.getEtudiantId(), etudiant);
                }
            }
        }
        
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("etudiantProfiles", etudiantProfiles);
        
        return "Professeur/absences";
    }

    @PostMapping("/professeur/absences/save")
    public String saveAbsences(@RequestParam Long emploiDuTempsId,
                               @RequestParam String matiere,
                               @RequestParam String classe,
                               @RequestParam String salle,
                               @RequestParam String heureDebut,
                               @RequestParam String heureFin,
                               @RequestParam Long classeId,
                               @RequestParam(required = false) List<Long> absents,
                               RedirectAttributes redirectAttributes) {
                    Long professeurId = getAuthenticatedProfesseurId();
        
        // Get or create seance for this emploiDuTemps
        EmploiDuTemps emploiDuTemps = emploiDuTempsService.findById(emploiDuTempsId).orElse(null);
        if (emploiDuTemps == null) {
            redirectAttributes.addFlashAttribute("error", "Emploi du temps non trouvé");
            return "redirect:/professeur/absences";
        }
        
        // Create or find seance for today
        Seance seance = seanceService.findOrCreateSeanceForEmploiDuTemps(emploiDuTempsId);
        Long seanceId = seance.getId().longValue();
        
        // Get all students in the class
        List<Inscription> inscriptions = inscriptionService.findByClasseId(classeId);
        
        // Process absences
        for (Inscription inscription : inscriptions) {
            Long etudiantId = inscription.getEtudiantId();
            boolean isAbsent = absents != null && absents.contains(etudiantId);
            
            if (isAbsent) {
                // Create absence record
                Absence absence = new Absence();
                absence.setSeanceId(seanceId);
                absence.setEtudiantId(etudiantId);
                absence.setType("non_justifiee");
                absence.setSaisiPar(professeurId);
                
                // Save absence
                absenceService.save(absence);
            }
        }
        
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/professeur/absences";
    }

    @GetMapping("/professeur/historique_absences")
    public String historiqueAbsences(@RequestParam Long classeId,
                                     @RequestParam(required = false) String matiere,
                                     Model model) {
        model.addAttribute("pageTitle", "Historique des Absences");
        model.addAttribute("currentRole", "professeur");
        
        // Get class info
        Classe classe = classeService.findById(classeId).orElse(null);
        model.addAttribute("classe", classe);
        model.addAttribute("classeId", classeId);
        model.addAttribute("matiere", matiere);
        
        // Get all seances for this class (through emploi du temps)
        // First, get all emploi du temps for this class
        List<AffectationEnseignement> affectations = affectationEnseignementService.findByClasseId(classeId);
        List<EmploiDuTemps> emploiDuTempsList = new java.util.ArrayList<>();
        for (AffectationEnseignement aff : affectations) {
            emploiDuTempsList.addAll(emploiDuTempsService.findByAffectationId(aff.getId()));
        }
        
        // Get seances for these emploi du temps
        Map<Long, Seance> seanceMap = new HashMap<>();
        List<Seance> allSeances = seanceService.findAll();
        for (EmploiDuTemps edt : emploiDuTempsList) {
            for (Seance seance : allSeances) {
                if (seance.getEmploiDuTempsId().equals(edt.getId().intValue())) {
                    seanceMap.put(seance.getId().longValue(), seance);
                }
            }
        }
        
        // Get absences grouped by seance using repository
        Map<Long, List<Absence>> absencesBySeance = new HashMap<>();
        for (Long seanceId : seanceMap.keySet()) {
            List<Absence> absences = absenceRepository.findBySeanceId(seanceId);
            if (!absences.isEmpty()) {
                absencesBySeance.put(seanceId, absences);
            }
        }
        
        // Get student profiles for absent students
        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        for (List<Absence> absences : absencesBySeance.values()) {
            for (Absence absence : absences) {
                ProfilEtudiant etudiant = profilEtudiantService.findById(absence.getEtudiantId()).orElse(null);
                if (etudiant != null) {
                    etudiantProfiles.put(absence.getEtudiantId(), etudiant);
                }
            }
        }
        
        model.addAttribute("seanceMap", seanceMap);
        model.addAttribute("absencesBySeance", absencesBySeance);
        model.addAttribute("etudiantProfiles", etudiantProfiles);
        
        return "Professeur/historique_absences";
    }

    @GetMapping("/professeur/notes")
    public String notes(Model model) {
        model.addAttribute("pageTitle", "Notes des Élèves");
        model.addAttribute("currentRole", "professeur");
        Long professeurId = getAuthenticatedProfesseurId();
        List<AffectationEnseignement> affectations = affectationEnseignementService.findByProfesseurId(professeurId);
        
        // Fetch related entities for display
        Map<Long, String> classeNames = new HashMap<>();
        Map<Long, String> matiereNames = new HashMap<>();
        for (AffectationEnseignement affectation : affectations) {
            Classe classe = classeService.findById(affectation.getClasseId()).orElse(null);
            Matiere matiere = matiereService.findById(affectation.getMatiereId()).orElse(null);
            if (classe != null) {
                classeNames.put(affectation.getClasseId(), classe.getNom());
            }
            if (matiere != null) {
                matiereNames.put(affectation.getMatiereId(), matiere.getNom());
            }
        }
        
        model.addAttribute("affectations", affectations);
        model.addAttribute("classeNames", classeNames);
        model.addAttribute("matiereNames", matiereNames);
        model.addAttribute("periodes", periodeService.findAll());
        model.addAttribute("inscriptions", null); // Explicitly set to null for class list view
        return "Professeur/notes";
    }

    @GetMapping("/professeur/notes/classe/{classeId}")
    public String notesClasse(@PathVariable Long classeId, Model model) {
        
        model.addAttribute("pageTitle", "Notes des Élèves");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();
        List<AffectationEnseignement> affectations = affectationEnseignementService.findByProfesseurId(professeurId);
        
        // --- ERADICATION DE LA PAGINATION BACKEND ---
        // On récupère TOUS les élèves de la classe d'un seul coup
        List<Inscription> inscriptions = inscriptionService.findByClasseId(classeId);
        // ---------------------------------------------
        
        // Fetch student profiles and notes
        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        Map<Long, List<Note>> etudiantNotes = new HashMap<>();
        for (Inscription inscription : inscriptions) {
            ProfilEtudiant etudiant = profilEtudiantService.findById(inscription.getEtudiantId()).orElse(null);
            if (etudiant != null) {
                etudiantProfiles.put(inscription.getEtudiantId(), etudiant);
            }
            List<Note> notes = noteService.findByEtudiantId(inscription.getEtudiantId());
            etudiantNotes.put(inscription.getEtudiantId(), notes);
        }
        
        // Get unique evaluation types across all students
        java.util.Set<String> evaluationTypes = new java.util.TreeSet<>();
        for (List<Note> notes : etudiantNotes.values()) {
            for (Note note : notes) {
                if (note.getTypeEvaluation() != null) {
                    evaluationTypes.add(note.getTypeEvaluation());
                }
            }
        }
        
        // Organize notes by student and evaluation type
        Map<Long, Map<String, Note>> etudiantNotesByType = new HashMap<>();
        for (Map.Entry<Long, List<Note>> entry : etudiantNotes.entrySet()) {
            Map<String, Note> notesByType = new HashMap<>();
            for (Note note : entry.getValue()) {
                if (note.getTypeEvaluation() != null) {
                    notesByType.put(note.getTypeEvaluation(), note);
                }
            }
            etudiantNotesByType.put(entry.getKey(), notesByType);
        }
        
        // Fetch class and subject names
        Classe classe = classeService.findById(classeId).orElse(null);
        Map<Long, String> matiereNames = new HashMap<>();
        for (AffectationEnseignement affectation : affectations) {
            if (affectation.getClasseId().equals(classeId)) {
                Matiere matiere = matiereService.findById(affectation.getMatiereId()).orElse(null);
                if (matiere != null) {
                    matiereNames.put(affectation.getMatiereId(), matiere.getNom());
                }
            }
        }
        
        // --- ENVOI DES DONNÉES ÉPURÉES À THYMELEAF ---
        model.addAttribute("affectations", affectations);
        model.addAttribute("inscriptions", inscriptions); // Contient la liste complète pour le JS
        model.addAttribute("etudiantProfiles", etudiantProfiles);
        model.addAttribute("etudiantNotes", etudiantNotes);
        model.addAttribute("etudiantNotesByType", etudiantNotesByType);
        model.addAttribute("evaluationTypes", evaluationTypes);
        model.addAttribute("classe", classe);
        model.addAttribute("classeId", classeId);
        model.addAttribute("matiereNames", matiereNames);
        model.addAttribute("periodes", periodeService.findAll());
        
        return "Professeur/notes";
    }

    @GetMapping("/professeur/notes/classe/{classeId}/pdf")
    public void exporterNotesPDF(@PathVariable Long classeId, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=notes_classe_" + classeId + ".pdf");

        // 1. Récupérer les données (comme dans notesClasse)
        Classe classe = classeService.findById(classeId).orElse(null);
        List<Inscription> inscriptions = inscriptionService.findByClasseId(classeId);

        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        Map<Long, List<Note>> etudiantNotes = new HashMap<>();
        for (Inscription inscription : inscriptions) {
            ProfilEtudiant etudiant = profilEtudiantService.findById(inscription.getEtudiantId()).orElse(null);
            if (etudiant != null) {
                etudiantProfiles.put(inscription.getEtudiantId(), etudiant);
            }
            List<Note> notes = noteService.findByEtudiantId(inscription.getEtudiantId());
            etudiantNotes.put(inscription.getEtudiantId(), notes);
        }

        // Types d'évaluation uniques (triés), comme dans notesClasse
        java.util.Set<String> evaluationTypes = new java.util.TreeSet<>();
        for (List<Note> notes : etudiantNotes.values()) {
            for (Note note : notes) {
                if (note.getTypeEvaluation() != null) {
                    evaluationTypes.add(note.getTypeEvaluation());
                }
            }
        }

        // Organisation des notes par étudiant et par type
        Map<Long, Map<String, Note>> etudiantNotesByType = new HashMap<>();
        for (Map.Entry<Long, List<Note>> entry : etudiantNotes.entrySet()) {
            Map<String, Note> notesByType = new HashMap<>();
            for (Note note : entry.getValue()) {
                if (note.getTypeEvaluation() != null) {
                    notesByType.put(note.getTypeEvaluation(), note);
                }
            }
            etudiantNotesByType.put(entry.getKey(), notesByType);
        }

        // 2. Générer le document PDF avec OpenPDF
        Document document = new Document(PageSize.A4.rotate()); // Paysage pour les tableaux de notes
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Ajouter le nom de la classe en haut
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Notes de la classe : " + (classe != null ? classe.getNom() : "Classe"), fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 3. Créer et remplir le tableau PDF (PdfPTable) : 1 colonne "Élève" + 1 colonne par type d'évaluation
        int nbColonnes = 1 + Math.max(evaluationTypes.size(), 1);
        PdfPTable table = new PdfPTable(nbColonnes);
        table.setWidthPercentage(100);

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, java.awt.Color.WHITE);
        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // En-tête du tableau
        PdfPCell headerEleve = new PdfPCell(new Paragraph("Élève", fontHeader));
        headerEleve.setBackgroundColor(new java.awt.Color(51, 51, 51));
        headerEleve.setPadding(6);
        table.addCell(headerEleve);

        if (evaluationTypes.isEmpty()) {
            PdfPCell headerVide = new PdfPCell(new Paragraph("Notes", fontHeader));
            headerVide.setBackgroundColor(new java.awt.Color(51, 51, 51));
            headerVide.setPadding(6);
            headerVide.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerVide);
        } else {
            for (String type : evaluationTypes) {
                PdfPCell headerType = new PdfPCell(new Paragraph(type.replace("_", " ").toUpperCase(), fontHeader));
                headerType.setBackgroundColor(new java.awt.Color(51, 51, 51));
                headerType.setPadding(6);
                headerType.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerType);
            }
        }

        // Une ligne par étudiant inscrit dans la classe
        for (Inscription inscription : inscriptions) {
            ProfilEtudiant etudiant = etudiantProfiles.get(inscription.getEtudiantId());
            String nomComplet = (etudiant != null) ? etudiant.getNom() + " " + etudiant.getPrenom() : "Étudiant";
            String matricule = (etudiant != null && etudiant.getMatricule() != null) ? etudiant.getMatricule() : "";

            PdfPCell cellEleve = new PdfPCell(new Paragraph(matricule.isEmpty() ? nomComplet : nomComplet + "\n" + matricule, fontCell));
            cellEleve.setPadding(6);
            table.addCell(cellEleve);

            Map<String, Note> notesByType = etudiantNotesByType.get(inscription.getEtudiantId());

            if (evaluationTypes.isEmpty()) {
                PdfPCell cellVide = new PdfPCell(new Paragraph("-", fontCell));
                cellVide.setPadding(6);
                cellVide.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellVide);
            } else {
                for (String type : evaluationTypes) {
                    Note note = (notesByType != null) ? notesByType.get(type) : null;
                    String valeurTxt = (note != null && note.getValeur() != null)
                            ? note.getValeur() + "/" + note.getSur()
                            : "-";
                    PdfPCell cellNote = new PdfPCell(new Paragraph(valeurTxt, fontCell));
                    cellNote.setPadding(6);
                    cellNote.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cellNote);
                }
            }
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/professeur/saisir_notes/{classeId}/{affectationId}")
    public String saisirNotes(@PathVariable Long classeId, @PathVariable Long affectationId, Model model) {
        model.addAttribute("pageTitle", "Saisir des Notes");
        model.addAttribute("currentRole", "professeur");
        
        List<Inscription> inscriptions = inscriptionService.findByClasseId(classeId);
        
        // Fetch student profiles
        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        for (Inscription inscription : inscriptions) {
            ProfilEtudiant etudiant = profilEtudiantService.findById(inscription.getEtudiantId()).orElse(null);
            if (etudiant != null) {
                etudiantProfiles.put(inscription.getEtudiantId(), etudiant);
            }
        }
        
        // Fetch class and subject info
        Classe classe = classeService.findById(classeId).orElse(null);
        AffectationEnseignement affectation = affectationEnseignementService.findById(affectationId).orElse(null);
        Matiere matiere = null;
        if (affectation != null) {
            matiere = matiereService.findById(affectation.getMatiereId()).orElse(null);
        }
        
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("etudiantProfiles", etudiantProfiles);
        model.addAttribute("classe", classe);
        model.addAttribute("classeId", classeId);
        model.addAttribute("affectationId", affectationId);
        model.addAttribute("affectation", affectation);
        model.addAttribute("matiere", matiere);
        model.addAttribute("periodes", periodeService.findAll());
        return "Professeur/saisir_notes";
    }

    @PostMapping("/professeur/saisir_notes")
    public String enregistrerNotes(
            @RequestParam Long affectationId,
            @RequestParam Long periodeId,
            @RequestParam String typeEvaluation,
            @RequestParam BigDecimal sur,
            @RequestParam String commentaire,
            @RequestParam(required = false) List<Long> etudiantIds,
            @RequestParam(required = false) List<BigDecimal> valeurs,
            Model model) {
        
        if (etudiantIds != null && valeurs != null && etudiantIds.size() == valeurs.size()) {
            for (int i = 0; i < etudiantIds.size(); i++) {
                if (valeurs.get(i) != null) {
                    Note note = new Note();
                    note.setEtudiantId(etudiantIds.get(i));
                    note.setAffectationId(affectationId);
                    note.setPeriodeId(periodeId);
                    note.setTypeEvaluation(typeEvaluation);
                    note.setValeur(valeurs.get(i));
                    note.setSur(sur);
                    note.setCommentaire(commentaire);
                    // TODO: Set saisi_par from authentication
                    note.setSaisiPar(1L);
                    noteService.save(note);
                }
            }
        }
        
        return "redirect:/professeur/notes";
    }

    // Page profil professeur
    @GetMapping("/professeur/profil")
    public String profil(Model model) {
        Long professeurId = getAuthenticatedProfesseurId();

        ProfilsProfesseursService.findById(professeurId).ifPresent(professeur -> {
            model.addAttribute("professeur", professeur);
        });
        // Si le professeur n'est pas trouvé, l'attribut "professeur" ne sera pas dans le modèle,
        // et la vue devra gérer ce cas (ex: afficher un message d'erreur).
        return "Professeur/profil";
    }

@GetMapping("/professeur/profil/pdf")
    public void exporterProfilPDF(HttpServletResponse response) throws IOException {
        Long professeurId = getAuthenticatedProfesseurId();
        ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
        
        if (professeur == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=profil_professeur_" + professeurId + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.WHITE);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.WHITE);
        Font fontSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.DARK_GRAY);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLACK);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(PdfPCell.NO_BORDER);
        headerCell.setBackgroundColor(new java.awt.Color(15, 23, 42));
        headerCell.setPadding(18);

        Paragraph title = new Paragraph("Profil du Professeur", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        Paragraph subtitle = new Paragraph("Résumé professionnel et compte utilisateur lié", fontSubtitle);
        subtitle.setAlignment(Paragraph.ALIGN_CENTER);
        subtitle.setSpacingAfter(4);
        headerCell.addElement(title);
        headerCell.addElement(subtitle);
        header.addCell(headerCell);
        document.add(header);

        document.add(new Paragraph(" "));

        PdfPTable identityTable = new PdfPTable(2);
        identityTable.setWidthPercentage(100);
        identityTable.setSpacingBefore(8);
        identityTable.setSpacingAfter(12);
        identityTable.setWidths(new float[]{1.25f, 2.75f});

        PdfPCell identityHeader = new PdfPCell(new Paragraph("INFORMATIONS PRINCIPALES", fontSection));
        identityHeader.setColspan(2);
        identityHeader.setBackgroundColor(new java.awt.Color(51, 65, 85));
        identityHeader.setBorder(PdfPCell.NO_BORDER);
        identityHeader.setPadding(8);
        identityTable.addCell(identityHeader);

        addPdfField(identityTable, "Nom", professeur.getNom(), fontLabel, fontValue);
        addPdfField(identityTable, "Prénom", professeur.getPrenom(), fontLabel, fontValue);
        addPdfField(identityTable, "Matricule", professeur.getMatricule(), fontLabel, fontValue);
        addPdfField(identityTable, "Spécialité", professeur.getSpecialite(), fontLabel, fontValue);
        addPdfField(identityTable, "Téléphone", professeur.getTelephone(), fontLabel, fontValue);
        addPdfField(identityTable, "Adresse", professeur.getAdresse(), fontLabel, fontValue);
        addPdfField(identityTable, "Date de naissance", professeur.getDateNaissance() != null ? professeur.getDateNaissance().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")) : null, fontLabel, fontValue);
        addPdfField(identityTable, "Type de contrat", professeur.getTypeContrat(), fontLabel, fontValue);
        addPdfField(identityTable, "Email du compte", professeur.getUser() != null ? professeur.getUser().getEmail() : null, fontLabel, fontValue);

        document.add(identityTable);

        PdfPTable noteTable = new PdfPTable(1);
        noteTable.setWidthPercentage(100);
        PdfPCell noteCell = new PdfPCell();
        noteCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        noteCell.setBorderColor(new java.awt.Color(226, 232, 240));
        noteCell.setPadding(12);
        Paragraph noteTitle = new Paragraph("PHOTO DE PROFIL", fontSection);
        noteTitle.setSpacingAfter(6);
        noteCell.addElement(noteTitle);
        noteCell.addElement(new Paragraph("La photo de profil est gérée depuis la fiche professeur. Le PDF reprend les informations du profil et du compte associé.", fontValue));
        noteTable.addCell(noteCell);
        document.add(noteTable);

        document.close();
    }

    @PostMapping("/professeur/profil/update")
    public String updateProfil(@RequestParam String nom,
                               @RequestParam String prenom,
                               @RequestParam String email,
                               @RequestParam String telephone,
                               @RequestParam String adresse,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
                               @RequestParam(required = false) String motDePasse,
                               @RequestParam(required = false) MultipartFile photo,
                               RedirectAttributes redirectAttributes) {
        Long professeurId = getAuthenticatedProfesseurId();
        ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
        
        if (professeur != null) {
            professeur.setNom(nom);
            professeur.setPrenom(prenom);
            professeur.setTelephone(telephone);
            professeur.setAdresse(adresse);
            professeur.setDateNaissance(dateNaissance);

            User profUser = professeur.getUser();
            if (profUser != null) {
                User existingUser = userService.getUser(email);
                if (existingUser != null && !existingUser.getId().equals(profUser.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé par un autre compte.");
                    return "redirect:/professeur/profil";
                }

                profUser.setEmail(email);

                if (motDePasse != null && !motDePasse.isBlank()) {
                    profUser.setPassword(passwordEncoder.encode(motDePasse));
                }

                try {
                    userService.save(profUser);
                } catch (DataIntegrityViolationException e) {
                    redirectAttributes.addFlashAttribute("error", "Impossible de mettre à jour l'email. Vérifiez qu'il n'existe pas déjà.");
                    return "redirect:/professeur/profil";
                }
            }
            
            if (photo != null && !photo.isEmpty()) {
                try {
                    String photoUrl = ProfilsProfesseursService.uploadProfessorPhoto(professeurId, photo);
                    professeur.setPhotoUrl(photoUrl);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload de la photo: " + e.getMessage());
                }
            }
            
            ProfilsProfesseursService.save(professeur);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Professeur non trouvé.");
        }
        
        return "redirect:/professeur/profil";
    }

    private void addPdfField(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setBorderColor(new java.awt.Color(226, 232, 240));
        labelCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        labelCell.setPadding(8);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value != null && !value.isBlank() ? value : "N/A", valueFont));
        valueCell.setBorderColor(new java.awt.Color(226, 232, 240));
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(new java.awt.Color(51, 65, 85));
        cell.setBorderColor(new java.awt.Color(51, 65, 85));
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null && !text.isBlank() ? text : "-", font));
        cell.setBorderColor(new java.awt.Color(226, 232, 240));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private Map<Integer, String> getJoursSemaine() {
        Map<Integer, String> jours = new LinkedHashMap<>();
        jours.put(1, "Lundi");
        jours.put(2, "Mardi");
        jours.put(3, "Mercredi");
        jours.put(4, "Jeudi");
        jours.put(5, "Vendredi");
        jours.put(6, "Samedi");
        return jours;
    }

    private Map<Long, Map<Integer, Map<String, Object>>> buildParHoraire(Long professeurId, List<HoraireEdt> horaires) {
        List<EmploiDuTemps> edtList = emploiDuTempsService.getCalendarProf(professeurId);
        Map<Long, Map<Integer, Map<String, Object>>> parHoraire = new HashMap<>();

        for (EmploiDuTemps edt : edtList) {
            Long horaireId = resolveHoraireId(edt, horaires);
            if (horaireId == null) {
                continue;
            }

            parHoraire.putIfAbsent(horaireId, new HashMap<>());

            Long affectationId = edt.getAffectationId();
            Long classeId = affectationEnseignementService.findById(affectationId).map(AffectationEnseignement::getClasseId).orElse(null);
            Long matiereId = affectationEnseignementService.findById(affectationId).map(AffectationEnseignement::getMatiereId).orElse(null);

            String classeNom = (classeId != null) ? classeService.findById(classeId).map(Classe::getNom).orElse("Classe") : "Classe";
            String matiereNom = (matiereId != null) ? matiereService.findById(matiereId).map(Matiere::getNom).orElse("Matière") : "Matière";
            String salleNom = (edt.getSalleId() != null) ? salleService.findById(edt.getSalleId()).map(Salle::getNom).orElse("Salle " + edt.getSalleId()) : "N/A";

            Map<String, Object> creneau = new HashMap<>();
            creneau.put("id", edt.getId());
            creneau.put("matiereNom", matiereNom);
            creneau.put("classeNom", classeNom);
            creneau.put("salleNom", salleNom);
            creneau.put("heureDebut", formatTime(edt.getHeureDebut()));
            creneau.put("heureFin", formatTime(edt.getHeureFin()));
            creneau.put("classeId", classeId);

            parHoraire.get(horaireId).put(edt.getJourSemaine(), creneau);
        }

        return parHoraire;
    }

    private Long resolveHoraireId(EmploiDuTemps edt, List<HoraireEdt> horaires) {
        if (edt.getHeureDebut() == null) {
            return null;
        }

        for (HoraireEdt horaire : horaires) {
            if (horaire.getHeureDebut() == null) {
                continue;
            }

            if (formatTime(edt.getHeureDebut()).equals(formatTime(horaire.getHeureDebut()))) {
                return horaire.getId();
            }
        }

        return null;
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    private String formatTimeRange(LocalTime debut, LocalTime fin) {
        return formatTime(debut) + " - " + formatTime(fin);
    }

    private float[] buildScheduleColumnWidths(int joursCount) {
        float[] widths = new float[1 + joursCount];
        widths[0] = 1.2f;
        for (int i = 1; i < widths.length; i++) {
            widths[i] = 1.0f;
        }
        return widths;
    }

    private Long getAuthenticatedProfesseurId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        User user = userService.getUser(authentication.getName());
        if (user == null) {
            throw new AccessDeniedException("Utilisateur connecté introuvable");
        }

        ProfilsProfesseurs profil = ProfilsProfesseursService.findByUserId(user.getId())
            .orElseThrow(() -> new AccessDeniedException("Profil professeur introuvable"));

        return profil.getId();
    }

    @PostMapping("/professeur/profil/password")
    public String updatePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        Long professeurId = getAuthenticatedProfesseurId();
        ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
        
        if (professeur != null) {
            User profUser = professeur.getUser();
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
                return "redirect:/professeur/profil";
            }
            
            // TODO: Verify current password (requires password hashing/verification)
            // For now, just update the password
            profUser.setPassword(passwordEncoder.encode(newPassword));
            userService.save(profUser);
            redirectAttributes.addFlashAttribute("success", "Mot de passe mis à jour avec succès.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Professeur non trouvé.");
        }
        
        return "redirect:/professeur/profil";
    }

    @PostMapping("/professeur/profil/photo")
    public String uploadProfessorPhoto(@RequestParam("photo") MultipartFile file, RedirectAttributes redirectAttributes) {
        Long professeurId = getAuthenticatedProfesseurId();
        try {
            String photoUrl = ProfilsProfesseursService.uploadProfessorPhoto(professeurId, file);
            ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
            if (professeur != null) {
                professeur.setPhotoUrl(photoUrl);
                ProfilsProfesseursService.save(professeur);
            }
            redirectAttributes.addFlashAttribute("success", "Photo de profil enregistrée avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement de la photo: " + e.getMessage());
        }
        return "redirect:/professeur/profil";
    }

    @PostMapping("/professeur/profil/photo/delete")
    public String deleteProfessorPhoto(RedirectAttributes redirectAttributes) {
        Long professeurId = getAuthenticatedProfesseurId();
        ProfilsProfesseurs professeur = ProfilsProfesseursService.findById(professeurId).orElse(null);
        if (professeur != null) {
            professeur.setPhotoUrl(null);
            ProfilsProfesseursService.save(professeur);
            redirectAttributes.addFlashAttribute("success", "La photo de profil a été supprimée avec succès.");
        }
        return "redirect:/professeur/profil";
    }

    // Page devoirs
    @GetMapping("/professeur/devoirs")
    public String devoirs(Model model) {
        model.addAttribute("pageTitle", "Supports de Cours & Devoirs");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();

        List<AffectationEnseignement> affectations = affectationEnseignementService.findByProfesseurId(professeurId);
        model.addAttribute("affectations", affectations);
        
        // Fetch related entities for display
        Map<Long, String> classeNames = new HashMap<>();
        Map<Long, String> matiereNames = new HashMap<>();
        for (AffectationEnseignement affectation : affectations) {
            Classe classe = classeService.findById(affectation.getClasseId()).orElse(null);
            Matiere matiere = matiereService.findById(affectation.getMatiereId()).orElse(null);
            if (classe != null) {
                classeNames.put(affectation.getClasseId(), classe.getNom());
            }
            if (matiere != null) {
                matiereNames.put(affectation.getMatiereId(), matiere.getNom());
            }
        }
        
        model.addAttribute("classeNames", classeNames);
        model.addAttribute("matiereNames", matiereNames);

        return "Professeur/devoirs";
    }

    // Page devoirs détails
    @GetMapping("/professeur/devoirs/details")
    public String devoirsDetails(@RequestParam Long affectationId, Model model) {
        model.addAttribute("pageTitle", "Supports de Cours & Devoirs");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();

        List<AffectationEnseignement> affectations = affectationEnseignementService.findByProfesseurId(professeurId);
        model.addAttribute("affectations", affectations);
        
        // Fetch related entities for display
        Map<Long, String> classeNames = new HashMap<>();
        Map<Long, String> matiereNames = new HashMap<>();
        for (AffectationEnseignement affectation : affectations) {
            Classe classe = classeService.findById(affectation.getClasseId()).orElse(null);
            Matiere matiere = matiereService.findById(affectation.getMatiereId()).orElse(null);
            if (classe != null) {
                classeNames.put(affectation.getClasseId(), classe.getNom());
            }
            if (matiere != null) {
                matiereNames.put(affectation.getMatiereId(), matiere.getNom());
            }
        }
        
        model.addAttribute("classeNames", classeNames);
        model.addAttribute("matiereNames", matiereNames);
        
        // Récupérer les types de fichiers pour le select du formulaire
        model.addAttribute("typesFichiers", typeFichierService.findAll());

        // Récupérer l'affectation sélectionnée et ses supports
        affectationEnseignementService.findById(affectationId).ifPresent(aff -> {
            model.addAttribute("selectedClasse", aff); 
            model.addAttribute("supports", supportCoursService.findByAffectationId(affectationId));
        });

        return "Professeur/devoirs_details";
    }

    // POST - Publier un nouveau support (Cours ou Devoir)
    @PostMapping("/professeur/devoirs/save")
    public String saveSupport(@ModelAttribute SupportCours support,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            supportCoursService.save(support, file);
            redirectAttributes.addFlashAttribute("success", "Le support a été publié avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'envoi du fichier: " + e.getMessage());
        }

        return "redirect:/professeur/devoirs/details?affectationId=" + support.getAffectationId();
    }

    @GetMapping("/professeur/bulletins")
    public String bulletins(Model model) {
        model.addAttribute("pageTitle", "Bulletins");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();
        
        // Get current active school year
        AnneeScolaire anneeScolaire = anneeScolaireService.findByEstActive(true).orElse(null);
        Long anneeScolaireId = (anneeScolaire != null) ? anneeScolaire.getId() : null;
        
        // Get the professor's titular class for the current year
        TitulaireClasse titulaireClasse = null;
        Classe classe = null;
        List<Inscription> inscriptions = null;
        Map<Long, ProfilEtudiant> etudiantProfiles = new HashMap<>();
        
        if (anneeScolaireId != null) {
            titulaireClasse = titulaireClasseService.findByProfesseurIdAndAnneeScolaireId(professeurId, anneeScolaireId).orElse(null);
            if (titulaireClasse != null) {
                classe = classeService.findById(titulaireClasse.getClasseId()).orElse(null);
                if (classe != null) {
                    inscriptions = inscriptionService.findByClasseId(classe.getId());
                    for (Inscription inscription : inscriptions) {
                        ProfilEtudiant etudiant = profilEtudiantService.findById(inscription.getEtudiantId()).orElse(null);
                        if (etudiant != null) {
                            etudiantProfiles.put(inscription.getEtudiantId(), etudiant);
                        }
                    }
                }
            }
        }
        
        model.addAttribute("titulaireClasse", titulaireClasse);
        model.addAttribute("classe", classe);
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("etudiantProfiles", etudiantProfiles);
        model.addAttribute("anneeScolaire", anneeScolaire);
        return "Professeur/bulletin";
    }

    @GetMapping("/professeur/bulletin/{etudiantId}")
    public String bulletinDetails(@PathVariable Long etudiantId, @RequestParam(required = false) Long periodeId, Model model) {
        model.addAttribute("pageTitle", "Bulletin de l'Élève");
        model.addAttribute("currentRole", "professeur");
        
        Long professeurId = getAuthenticatedProfesseurId();
        
        // Get current active school year
        AnneeScolaire anneeScolaire = anneeScolaireService.findByEstActive(true).orElse(null);
        Long anneeScolaireId = (anneeScolaire != null) ? anneeScolaire.getId() : null;
        
        // Get the professor's titular class
        TitulaireClasse titulaireClasse = null;
        Classe classe = null;
        if (anneeScolaireId != null) {
            titulaireClasse = titulaireClasseService.findByProfesseurIdAndAnneeScolaireId(professeurId, anneeScolaireId).orElse(null);
            if (titulaireClasse != null) {
                classe = classeService.findById(titulaireClasse.getClasseId()).orElse(null);
            }
        }
        
        // Get student profile
        ProfilEtudiant etudiant = profilEtudiantService.findById(etudiantId).orElse(null);
        
        // Get all periods
        List<Periode> periodes = periodeService.findAll();
        
        // Get bulletin data if period is selected
        Map<String, Object> bulletin = null;
        if (periodeId != null && classe != null) {
            bulletin = noteService.getBulletinEtudiant(etudiantId, periodeId, classe.getId());
        }
        
        model.addAttribute("etudiant", etudiant);
        model.addAttribute("etudiantId", etudiantId);
        model.addAttribute("classe", classe);
        model.addAttribute("titulaireClasse", titulaireClasse);
        model.addAttribute("anneeScolaire", anneeScolaire);
        model.addAttribute("periodes", periodes);
        model.addAttribute("bulletin", bulletin);
        model.addAttribute("selectedPeriodeId", periodeId);
        return "Professeur/bulletin_details";
    }

    @GetMapping("/professeur/bulletin/{etudiantId}/pdf")
    public void exporterBulletinPdf(@PathVariable Long etudiantId,
                                    @RequestParam(required = false) Long periodeId,
                                    HttpServletResponse response) throws IOException {
        Long professeurId = getAuthenticatedProfesseurId();

        AnneeScolaire anneeScolaire = anneeScolaireService.findByEstActive(true).orElse(null);
        Long anneeScolaireId = (anneeScolaire != null) ? anneeScolaire.getId() : null;

        TitulaireClasse titulaireClasse = null;
        Classe classe = null;
        if (anneeScolaireId != null) {
            titulaireClasse = titulaireClasseService.findByProfesseurIdAndAnneeScolaireId(professeurId, anneeScolaireId).orElse(null);
            if (titulaireClasse != null) {
                classe = classeService.findById(titulaireClasse.getClasseId()).orElse(null);
            }
        }

        ProfilEtudiant etudiant = profilEtudiantService.findById(etudiantId).orElse(null);
        Periode periode = (periodeId != null) ? periodeService.findById(periodeId).orElse(null) : null;
        Map<String, Object> bulletin = (periodeId != null && classe != null) ? noteService.getBulletinEtudiant(etudiantId, periodeId, classe.getId()) : null;

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=bulletin_etudiant_" + etudiantId + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.WHITE);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.WHITE);
        Font fontSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.DARK_GRAY);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLACK);

        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell bannerCell = new PdfPCell();
        bannerCell.setBorder(PdfPCell.NO_BORDER);
        bannerCell.setBackgroundColor(new java.awt.Color(15, 23, 42));
        bannerCell.setPadding(18);
        bannerCell.addElement(new Paragraph("Bulletin de notes", fontTitle));
        String periodeLabel = periode != null ? periode.getLibelle() : "Période";
        String anneeLabel = anneeScolaire != null ? anneeScolaire.getLibelle() : "Année scolaire";
        bannerCell.addElement(new Paragraph(anneeLabel + " · " + periodeLabel + " · " + (classe != null ? classe.getNom() : "Classe"), fontSubtitle));
        banner.addCell(bannerCell);
        document.add(banner);

        document.add(new Paragraph(" "));

        PdfPTable studentTable = new PdfPTable(2);
        studentTable.setWidthPercentage(100);
        studentTable.setSpacingAfter(10);
        studentTable.setWidths(new float[]{1.3f, 2.7f});

        PdfPCell studentHeader = new PdfPCell(new Paragraph("INFORMATIONS ÉLÈVE", fontSection));
        studentHeader.setColspan(2);
        studentHeader.setBackgroundColor(new java.awt.Color(51, 65, 85));
        studentHeader.setBorder(PdfPCell.NO_BORDER);
        studentHeader.setPadding(8);
        studentTable.addCell(studentHeader);

        addPdfField(studentTable, "Nom complet", etudiant != null ? etudiant.getNom() + " " + etudiant.getPrenom() : null, fontLabel, fontValue);
        addPdfField(studentTable, "Matricule", etudiant != null ? etudiant.getMatricule() : null, fontLabel, fontValue);
        addPdfField(studentTable, "Classe", classe != null ? classe.getNom() : null, fontLabel, fontValue);
        addPdfField(studentTable, "Période", periodeLabel, fontLabel, fontValue);

        document.add(studentTable);

        PdfPTable noteTable = new PdfPTable(4);
        noteTable.setWidthPercentage(100);
        noteTable.setWidths(new float[]{3.3f, 1.1f, 0.9f, 2.7f});

        addHeaderCell(noteTable, "Matière", fontSection);
        addHeaderCell(noteTable, "Note /20", fontSection);
        addHeaderCell(noteTable, "Coeff.", fontSection);
        addHeaderCell(noteTable, "Appréciation", fontSection);

        Object matieresObj = bulletin != null ? bulletin.get("matieres") : null;
        if (matieresObj instanceof List<?> matieres) {
            for (Object item : matieres) {
                if (item instanceof Map<?, ?> rawMatiereData) {
                    Object matiereNomValue = rawMatiereData.get("matiereNom");
                    Object moyenneValue = rawMatiereData.get("moyenne");
                    Object coefficientValue = rawMatiereData.get("coefficient");
                    Object appreciationValue = rawMatiereData.get("appreciation");

                    String matiereNom = matiereNomValue != null ? String.valueOf(matiereNomValue) : "Matière";
                    String moyenne = moyenneValue != null ? String.valueOf(moyenneValue) : "-";
                    String coefficient = coefficientValue != null ? String.valueOf(coefficientValue) : "-";
                    String appreciation = appreciationValue != null ? String.valueOf(appreciationValue) : "";

                    addBodyCell(noteTable, matiereNom, fontValue, Element.ALIGN_LEFT);
                    addBodyCell(noteTable, moyenne, fontValue, Element.ALIGN_CENTER);
                    addBodyCell(noteTable, coefficient, fontValue, Element.ALIGN_CENTER);
                    addBodyCell(noteTable, appreciation, fontValue, Element.ALIGN_LEFT);
                }
            }
        } else {
            PdfPCell empty = new PdfPCell(new Paragraph("Aucune note disponible pour la période sélectionnée.", fontValue));
            empty.setColspan(4);
            empty.setPadding(10);
            noteTable.addCell(empty);
        }

        document.add(noteTable);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        summaryTable.setWidths(new float[]{1.6f, 2.4f});

        PdfPCell moyenneCell = new PdfPCell();
        moyenneCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        moyenneCell.setBorderColor(new java.awt.Color(226, 232, 240));
        moyenneCell.setPadding(10);
        moyenneCell.addElement(new Paragraph("MOYENNE GÉNÉRALE", fontLabel));
        moyenneCell.addElement(new Paragraph(bulletin != null && bulletin.get("moyenneGenerale") != null ? String.valueOf(bulletin.get("moyenneGenerale")) + " / 20" : "-", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));

        PdfPCell appreciationCell = new PdfPCell();
        appreciationCell.setBackgroundColor(new java.awt.Color(248, 250, 252));
        appreciationCell.setBorderColor(new java.awt.Color(226, 232, 240));
        appreciationCell.setPadding(10);
        appreciationCell.addElement(new Paragraph("APPRÉCIATION DU PROFESSEUR PRINCIPAL", fontLabel));
        appreciationCell.addElement(new Paragraph(bulletin != null && bulletin.get("appreciationGenerale") != null ? String.valueOf(bulletin.get("appreciationGenerale")) : "-", fontValue));

        summaryTable.addCell(moyenneCell);
        summaryTable.addCell(appreciationCell);
        document.add(summaryTable);

        document.close();
    }

}
