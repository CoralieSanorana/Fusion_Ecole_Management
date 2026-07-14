package com.ecole.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Comparator;
import java.util.Locale;
import com.ecole.entity.Classe;
import com.ecole.entity.Coefficient;
import com.ecole.entity.Devoir;
import com.ecole.entity.Inscription;
import com.ecole.entity.Lecon;
import com.ecole.entity.Note;
import com.ecole.entity.Periode;
import com.ecole.entity.ProfilEtudiant;
import com.ecole.entity.SupportCours;
import com.ecole.entity.User;
import com.ecole.entity.EmploiDuTemps;
import com.ecole.repository.CoefficientRepository;
import com.ecole.repository.DevoirRepository;
import com.ecole.repository.InscriptionRepository;
import com.ecole.repository.LeconRepository;
import com.ecole.repository.NoteRepository;
import com.ecole.repository.PeriodeRepository;
import com.ecole.repository.UserRepository;
import com.ecole.repository.ClasseRepository;
import com.ecole.repository.AbsenceRepository;
import com.ecole.repository.ProfilEtudiantRepository;
import com.ecole.service.UserService;
import com.ecole.service.ProfilEtudiantService;
import com.ecole.repository.EmploiDuTempsRepository;
import com.ecole.repository.SupportCoursRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class EtudiantController {

    @Autowired public UserRepository userRepository;
    @Autowired public UserService userService;
    @Autowired public DevoirRepository devoirRepository;
    @Autowired public InscriptionRepository inscriptionRepository;
    @Autowired public LeconRepository leconRepository;
    @Autowired public NoteRepository noteRepository;
    @Autowired public ClasseRepository classeRepository;
    @Autowired public CoefficientRepository coefficientRepository;
    @Autowired public PeriodeRepository periodeRepository;  
    @Autowired public ProfilEtudiantService profilEtudiantService;
    @Autowired public ProfilEtudiantRepository profilEtudiantRepository;
    @Autowired public EmploiDuTempsRepository emploiDuTempsRepository;
    @Autowired public SupportCoursRepository supportCoursRepository;
    @Autowired public AbsenceRepository absenceRepository;

    private static final List<String> NOMS_JOURS =
            List.of("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

    private static final List<String> COULEURS_MATIERE =
            List.of("math", "french", "science", "history");

    @GetMapping({"/etudiant/abscences", "/etudiant/absences"})
    public String absences(Model model, Authentication authentication) {
        ProfilEtudiant profilEtudiant = getProfilEtudiantConnecte(authentication);
        if (profilEtudiant == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> absences = absenceRepository.findHistoriqueEtudiant(profilEtudiant.getId())
                .stream()
                .map(this::toAbsenceRow)
                .toList();
        long totalJustifiees = absences.stream()
                .filter(absence -> "justifiee".equals(absence.get("type")))
                .count();

        model.addAttribute("pageTitle", "Mes absences");
        model.addAttribute("currentRole", "etudiant");
        model.addAttribute("profilEtudiant", profilEtudiant);
        model.addAttribute("absences", absences);
        model.addAttribute("totalAbsences", absences.size());
        model.addAttribute("totalJustifiees", totalJustifiees);
        model.addAttribute("totalNonJustifiees", absences.size() - totalJustifiees);

        return "Etudiant/abscences";
    }

    @GetMapping("/etudiant/emploi")
    public String emploi(Model model, HttpSession session) {
        
        // Récupération de l'utilisateur connecté depuis la session (Répare 'userLoggedIn cannot be resolved')
        User userLoggedIn = (User) session.getAttribute("user");

        ProfilEtudiant profilEtudiant = profilEtudiantService.findById(1L).orElse(null);
        if (profilEtudiant == null) return "redirect:/";
        
        Long etudiantId = profilEtudiant.getId();

        // Classe de l'étudiant via son inscription active
        List<Inscription> inscriptions = inscriptionRepository.findActiveByEtudiant(etudiantId);
        Long classeId = inscriptions.isEmpty() ? null : inscriptions.get(0).getClasseId();
        Classe classe = (classeId != null) ? classeRepository.findById(classeId).orElse(null) : null;

        // Séances de la classe, valides aujourd'hui
        LocalDate aujourdHui = LocalDate.now();
        List<EmploiDuTemps> seances = (classeId != null)
                ? emploiDuTempsRepository.findByClasseIdAndDate(classeId, aujourdHui)
                : List.of();

        // Colonnes : au minimum Lundi→Vendredi, plus si des séances existent au-delà
        int maxJour = seances.stream()
                .map(EmploiDuTemps::getJourSemaine)
                .filter(j -> j != null)
                .max(Integer::compareTo)
                .orElse(5);
        maxJour = Math.max(maxJour, 5);

        List<Map<String, Object>> jours = new ArrayList<>();
        for (int j = 1; j <= maxJour; j++) {
            Map<String, Object> jour = new LinkedHashMap<>();
            jour.put("numero", j);
            jour.put("nom", NOMS_JOURS.get(j - 1));
            jours.add(jour);
        }

        // Créneaux horaires distincts présents dans l'emploi du temps, triés
        List<LocalTime[]> creneaux = new ArrayList<>();
        for (EmploiDuTemps s : seances) {
            boolean dejaPresent = creneaux.stream()
                    .anyMatch(c -> c[0].equals(s.getHeureDebut()) && c[1].equals(s.getHeureFin()));
            if (!dejaPresent) {
                creneaux.add(new LocalTime[]{s.getHeureDebut(), s.getHeureFin()});
            }
        }
        creneaux.sort(Comparator.comparing(c -> c[0]));

        // Association couleur ↔ matière
        Map<Long, String> couleurParMatiere = new LinkedHashMap<>();

        // Construction de la grille : une ligne par créneau, une case par jour
        List<Map<String, Object>> grille = new ArrayList<>();
        for (LocalTime[] creneau : creneaux) {
            Map<String, Object> ligne = new LinkedHashMap<>();
            ligne.put("label", formatHeure(creneau[0]) + "–" + formatHeure(creneau[1]));

            Map<Integer, Map<String, Object>> cases = new LinkedHashMap<>();
            for (int j = 1; j <= maxJour; j++) {
                final int jourCourant = j;
                EmploiDuTemps trouve = seances.stream()
                        .filter(s -> jourCourant == (s.getJourSemaine() == null ? -1 : s.getJourSemaine()))
                        .filter(s -> creneau[0].equals(s.getHeureDebut()) && creneau[1].equals(s.getHeureFin()))
                        .findFirst()
                        .orElse(null);

                if (trouve != null && trouve.getAffectation() != null
                        && trouve.getAffectation().getMatiere() != null) {
                    Long matiereId = trouve.getAffectation().getMatiere().getId();
                    String couleur = couleurParMatiere.computeIfAbsent(matiereId,
                            k -> COULEURS_MATIERE.get(couleurParMatiere.size() % COULEURS_MATIERE.size()));

                    String profNomComplet = trouve.getAffectation().getProfesseur() != null
                            ? "Prof. " + trouve.getAffectation().getProfesseur().getNom()
                            : "—";

                    Map<String, Object> cellule = new LinkedHashMap<>();
                    cellule.put("matiere", trouve.getAffectation().getMatiere().getNom());
                    cellule.put("professeur", profNomComplet);
                    cellule.put("salle", trouve.getSalle() != null ? trouve.getSalle().getNom() : null);
                    cellule.put("couleur", couleur);
                    cases.put(j, cellule);
                } else {
                    cases.put(j, null);
                }
            }
            ligne.put("cases", cases);
            grille.add(ligne);
        }

        // Libellé "Semaine du ... au ..."
        LocalDate lundi = aujourdHui.with(DayOfWeek.MONDAY);
        LocalDate dernierJour = lundi.plusDays(maxJour - 1);
        DateTimeFormatter fmtJourMois = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
        String semaineLabel = "Semaine du " + lundi.getDayOfMonth() + " au " + dernierJour.format(fmtJourMois);

        model.addAttribute("pageTitle", "Mon Emploi du Temps");
        model.addAttribute("currentRole", "etudiant");
        model.addAttribute("user", userLoggedIn);
        model.addAttribute("profilEtudiant", profilEtudiant);
        model.addAttribute("classe", classe);
        model.addAttribute("semaineLabel", semaineLabel);
        model.addAttribute("jours", jours);
        model.addAttribute("grille", grille);
        return "Etudiant/calendar";
    }

    private String formatHeure(LocalTime t) {
        if (t == null) return "";
        return (t.getMinute() == 0)
                ? t.getHour() + "h"
                : String.format("%dh%02d", t.getHour(), t.getMinute());
    }

    
    
    @GetMapping("/etudiant/notes")
    public String notes(
            Model model,
            HttpSession session,
            @RequestParam(name = "periodeId", required = false) Long periodeId
    ) {
        ProfilEtudiant profilEtudiant = profilEtudiantService.findById(1L).orElse(null);

        if (profilEtudiant == null) return "redirect:/";

        Long etudiantId = profilEtudiant.getId();

        // Inscription active → année scolaire
        List<Inscription> inscriptions = inscriptionRepository.findActiveByEtudiant(etudiantId);
        Long anneeScolaireId = inscriptions.isEmpty() ? null : inscriptions.get(0).getAnneeScolaireId();

        // Périodes disponibles
        List<Periode> periodes = (anneeScolaireId != null)
                ? periodeRepository.findByAnneeScolaire(anneeScolaireId)
                : List.of();

        // Période sélectionnée (défaut = première)
        Periode periodeSelectionnee = null;
        if (!periodes.isEmpty()) {
            periodeSelectionnee = periodes.stream()
                    .filter(p -> p.getId().equals(periodeId))
                    .findFirst()
                    .orElse(periodes.get(0));
        }

        // Coefficients
        Long classeId = inscriptions.isEmpty() ? null : inscriptions.get(0).getClasseId();
        Map<Long, BigDecimal> coefficientsMap = Map.of();
        if (classeId != null) {
            Classe classe = classeRepository.findById(classeId).orElse(null);
            if (classe != null) {
                coefficientsMap = coefficientRepository.findCoefficientsMapByNiveau(classe.getNiveauId());
            }
        }

        // Notes brutes de la période
        List<Note> notesbrutes = List.of();
        if (etudiantId != null && periodeSelectionnee != null) {
            notesbrutes = noteRepository.findNotesParEtudiantEtPeriode(etudiantId, periodeSelectionnee.getId());
        }

        // Grouper par matière : matiereNom → { moyenne, coeff, professeur, détails[] }
        Map<String, Map<String, Object>> groupes = new LinkedHashMap<>();
        for (Note note : notesbrutes) {
            if (note.getAffectation() == null || note.getAffectation().getMatiere() == null) continue;
            String matiereNom = note.getAffectation().getMatiere().getNom();
            Long matiereId    = note.getAffectation().getMatiere().getId();
            BigDecimal coeff  = coefficientsMap.getOrDefault(matiereId, BigDecimal.ONE);
            String prof = note.getAffectation().getProfesseur() != null
                    ? note.getAffectation().getProfesseur().getPrenom() + " " + note.getAffectation().getProfesseur().getNom()
                    : "—";

            groupes.computeIfAbsent(matiereNom, k -> {
                Map<String, Object> g = new LinkedHashMap<>();
                g.put("matiereNom", matiereNom);
                g.put("matiereId",  matiereId);
                g.put("coefficient", coeff);
                g.put("professeur", prof);
                g.put("somme",  BigDecimal.ZERO);
                g.put("count",  0);
                g.put("details", new ArrayList<Map<String, Object>>());
                return g;
            });

            Map<String, Object> g = groupes.get(matiereNom);
            BigDecimal valSur20 = note.getSur() != null && note.getSur().compareTo(BigDecimal.ZERO) > 0
                    ? note.getValeur().multiply(BigDecimal.valueOf(20)).divide(note.getSur(), 2, RoundingMode.HALF_UP)
                    : note.getValeur();

            g.put("somme", ((BigDecimal) g.get("somme")).add(valSur20));
            g.put("count", (int) g.get("count") + 1);

            // Détail d'une note individuelle
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("typeEvaluation", note.getTypeEvaluation() != null
                    ? note.getTypeEvaluation().replace("_", " ") : "Note");
            detail.put("valeur", valSur20);
            detail.put("appreciation", appreciation(valSur20));
            detail.put("cssClass", cssClass(valSur20));
            ((List<Map<String, Object>>) g.get("details")).add(detail);
        }

        // Finaliser chaque groupe : calculer la moyenne
        List<Map<String, Object>> notesGroupees = new ArrayList<>();
        for (Map<String, Object> g : groupes.values()) {
            int count        = (int) g.get("count");
            BigDecimal somme = (BigDecimal) g.get("somme");
            BigDecimal moyenne = count > 0
                    ? somme.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            g.put("moyenne",      moyenne);
            g.put("appreciation", appreciation(moyenne));
            g.put("cssClass",     cssClass(moyenne));
            notesGroupees.add(g);
        }

        model.addAttribute("pageTitle",           "Mes Notes");
        model.addAttribute("currentRole",         "etudiant");
        model.addAttribute("user",                null);
        model.addAttribute("profilEtudiant",      profilEtudiant);
        model.addAttribute("notesGroupees",        notesGroupees);
        model.addAttribute("periodes",            periodes);
        model.addAttribute("periodeSelectionnee", periodeSelectionnee);
        return "Etudiant/notes";
    }

    @GetMapping("/etudiant/devoirs")
    public String devoirs(Model model, HttpSession session) {
        ProfilEtudiant profilEtudiant = profilEtudiantService.findById(1L).orElse(null);

        if (profilEtudiant == null) return "redirect:/";

        List<Inscription> inscriptions = inscriptionRepository.findActiveByEtudiant(profilEtudiant.getId());
        Long classeId = inscriptions.isEmpty() ? null : inscriptions.get(0).getClasseId();
        List<SupportCours> supports = List.of();
       
        try {
            if (classeId != null) {
                 supports = supportCoursRepository.findByClasse(classeId);
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }

        model.addAttribute("pageTitle", "Devoirs");
        model.addAttribute("currentRole", "etudiant");
        model.addAttribute("user", null);
        model.addAttribute("profilEtudiant", profilEtudiant);
        model.addAttribute("supports", supports);
        
        return "Etudiant/devoirs";
    }

    @GetMapping("/etudiant/bulletin")
    public String bulletin(
            Model model,
            HttpSession session,
            @RequestParam(name = "periodeId", required = false) Long periodeId
    ) {
        ProfilEtudiant profilEtudiant = profilEtudiantService.findById(1L).orElse(null);

        if (profilEtudiant == null) return "redirect:/";

        Long etudiantId = profilEtudiant.getId();

        // Inscription active → année scolaire + classe
        List<Inscription> inscriptions = inscriptionRepository.findActiveByEtudiant(etudiantId);
        Inscription inscription = inscriptions.isEmpty() ? null : inscriptions.get(0);
        Long anneeScolaireId = inscription != null ? inscription.getAnneeScolaireId() : null;
        Long classeId = inscription != null ? inscription.getClasseId() : null;

        // Périodes disponibles
        List<Periode> periodes = List.of();
        try {
            if (anneeScolaireId != null) {
                periodes = periodeRepository.findByAnneeScolaire(anneeScolaireId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Période sélectionnée (défaut = première)
        Periode periodeSelectionnee = null;
        if (!periodes.isEmpty()) {
            periodeSelectionnee = periodes.stream()
                    .filter(p -> p.getId().equals(periodeId))
                    .findFirst()
                    .orElse(periodes.get(0));
        }

        // Coefficients par matière pour ce niveau
        Map<Long, BigDecimal> coefficientsMap = Map.of();
        Classe classe = null;
        try {
            if (classeId != null) {
                classe = classeRepository.findById(classeId).orElse(null);
                if (classe != null) {
                    coefficientsMap = coefficientRepository.findCoefficientsMapByNiveau(classe.getNiveauId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Notes de la période
        List<Note> notes = List.of();
        try {
            if (periodeSelectionnee != null) {
                notes = noteRepository.findNotesParEtudiantEtPeriode(etudiantId, periodeSelectionnee.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Grouper les notes par matière (moyenne des notes de la matière)
        // Structure : matiereNom -> {moyenne, coefficient, appreciation}
        Map<String, Map<String, Object>> lignesBulletin = new LinkedHashMap<>();
        for (Note note : notes) {
            if (note.getAffectation() == null || note.getAffectation().getMatiere() == null) continue;
            String matiereNom = note.getAffectation().getMatiere().getNom();
            Long matiereId = note.getAffectation().getMatiere().getId();
            BigDecimal coeff = coefficientsMap.getOrDefault(matiereId, BigDecimal.ONE);

            lignesBulletin.computeIfAbsent(matiereNom, k -> {
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("matiereNom", matiereNom);
                ligne.put("coefficient", coeff);
                ligne.put("somme", BigDecimal.ZERO);
                ligne.put("count", 0);
                return ligne;
            });

            Map<String, Object> ligne = lignesBulletin.get(matiereNom);
            BigDecimal valeurSur20 = note.getSur() != null && note.getSur().compareTo(BigDecimal.ZERO) > 0
                    ? note.getValeur().multiply(BigDecimal.valueOf(20)).divide(note.getSur(), 2, RoundingMode.HALF_UP)
                    : note.getValeur();
            ligne.put("somme", ((BigDecimal) ligne.get("somme")).add(valeurSur20));
            ligne.put("count", (int) ligne.get("count") + 1);
        }

        // Calculer moyenne par matière + appréciation
        List<Map<String, Object>> lignes = new ArrayList<>();
        BigDecimal totalPondere = BigDecimal.ZERO;
        BigDecimal totalCoeffs = BigDecimal.ZERO;

        for (Map<String, Object> ligne : lignesBulletin.values()) {
            int count = (int) ligne.get("count");
            BigDecimal somme = (BigDecimal) ligne.get("somme");
            BigDecimal coeff = (BigDecimal) ligne.get("coefficient");
            BigDecimal moyenne = count > 0
                    ? somme.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            ligne.put("moyenne", moyenne);
            ligne.put("appreciation", appreciation(moyenne));
            ligne.put("cssClass", cssClass(moyenne));
            totalPondere = totalPondere.add(moyenne.multiply(coeff));
            totalCoeffs = totalCoeffs.add(coeff);
            lignes.add(ligne);
        }

        BigDecimal moyenneGenerale = totalCoeffs.compareTo(BigDecimal.ZERO) > 0
                ? totalPondere.divide(totalCoeffs, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── Rang dynamique par période ──────────────────────────────
        // 1. Tous les étudiants actifs de la même classe
        int rang = 1;
        int effectif = 1;
        try {
            if (classeId != null && periodeSelectionnee != null) {
                List<Inscription> classeInscriptions = inscriptionRepository.findActiveByClasse(classeId);
                effectif = classeInscriptions.size();

                // 2. Leurs ids
                List<Long> tousEtudiantIds = classeInscriptions.stream()
                        .map(Inscription::getEtudiantId)
                        .toList();

                // 3. Toutes leurs notes pour cette période
                List<Note> notesClasse = noteRepository.findNotesParEtudiantsEtPeriode(
                        tousEtudiantIds, periodeSelectionnee.getId());

                // 4. Calculer la moyenne pondérée de chaque étudiant
                final Map<Long, BigDecimal> coeffMap = coefficientsMap;
                Map<Long, Map<Long, BigDecimal[]>> notesByEtudiantMatiere = new java.util.HashMap<>();
                for (Note n : notesClasse) {
                    if (n.getAffectation() == null || n.getAffectation().getMatiere() == null) continue;
                    Long eid = n.getEtudiantId();
                    Long mid = n.getAffectation().getMatiere().getId();
                    BigDecimal valSur20 = n.getSur() != null && n.getSur().compareTo(BigDecimal.ZERO) > 0
                            ? n.getValeur().multiply(BigDecimal.valueOf(20)).divide(n.getSur(), 4, RoundingMode.HALF_UP)
                            : n.getValeur();
                    notesByEtudiantMatiere
                            .computeIfAbsent(eid, k -> new java.util.HashMap<>())
                            .computeIfAbsent(mid, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    BigDecimal[] acc = notesByEtudiantMatiere.get(eid).get(mid);
                    acc[0] = acc[0].add(valSur20);
                    acc[1] = acc[1].add(BigDecimal.ONE);
                }

                // Moyenne générale pondérée par étudiant
                Map<Long, BigDecimal> moyenneParEtudiant = new java.util.HashMap<>();
                for (Map.Entry<Long, Map<Long, BigDecimal[]>> entry : notesByEtudiantMatiere.entrySet()) {
                    Long eid = entry.getKey();
                    BigDecimal totalP = BigDecimal.ZERO;
                    BigDecimal totalC = BigDecimal.ZERO;
                    for (Map.Entry<Long, BigDecimal[]> m : entry.getValue().entrySet()) {
                        Long mid = m.getKey();
                        BigDecimal[] acc = m.getValue();
                        BigDecimal moyMat = acc[0].divide(acc[1], 4, RoundingMode.HALF_UP);
                        BigDecimal coeff = coeffMap.getOrDefault(mid, BigDecimal.ONE);
                        totalP = totalP.add(moyMat.multiply(coeff));
                        totalC = totalC.add(coeff);
                    }
                    BigDecimal moy = totalC.compareTo(BigDecimal.ZERO) > 0
                            ? totalP.divide(totalC, 4, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    moyenneParEtudiant.put(eid, moy);
                }

                // 5. Rang de l'étudiant connecté dans cette période
                BigDecimal moyenneEtudiant = moyenneParEtudiant.getOrDefault(etudiantId, BigDecimal.ZERO);
                rang = 1;
                for (BigDecimal m : moyenneParEtudiant.values()) {
                    if (m.compareTo(moyenneEtudiant) > 0) rang++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("pageTitle", "Mon Bulletin");
        model.addAttribute("currentRole", "etudiant");
        model.addAttribute("user", null);
        model.addAttribute("profilEtudiant", profilEtudiant);
        model.addAttribute("periodes", periodes);
        model.addAttribute("periodeSelectionnee", periodeSelectionnee);
        model.addAttribute("classe", classe);
        model.addAttribute("lignes", lignes);
        model.addAttribute("moyenneGenerale", moyenneGenerale);
        model.addAttribute("appreciationGenerale", appreciation(moyenneGenerale));
        model.addAttribute("cssClassGenerale", cssClass(moyenneGenerale));
        model.addAttribute("rang", rang);
        model.addAttribute("effectif", effectif);
        return "Etudiant/bulletin";
    }

    private String appreciation(BigDecimal moy) {
        if (moy == null) return "";
        double v = moy.doubleValue();
        if (v >= 16) return "Excellent";
        if (v >= 14) return "Très bien";
        if (v >= 12) return "Bien";
        if (v >= 10) return "Assez bien";
        if (v >= 8)  return "Passable";
        return "Insuffisant";
    }

    private String cssClass(BigDecimal moy) {
        if (moy == null) return "";
        double v = moy.doubleValue();
        if (v >= 14) return "grade-excellent";
        if (v >= 12) return "grade-good";
        if (v >= 10) return "grade-average";
        return "grade-low";
    }

    @GetMapping("/etudiant/profil")
    public String profil(Model model, HttpSession session) {
        ProfilEtudiant profilEtudiant = profilEtudiantService.findById(1L).orElse(null);

        model.addAttribute("pageTitle", "Mon Profil");
        model.addAttribute("currentRole", "etudiant");
        model.addAttribute("user", null);
        model.addAttribute("profilEtudiant", profilEtudiant);
        return "Etudiant/profil";
    }

    private ProfilEtudiant getProfilEtudiantConnecte(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                ProfilEtudiant profil = profilEtudiantRepository.findByUserId(user.getId()).orElse(null);
                if (profil != null) {
                    return profil;
                }
            }
        }
        return profilEtudiantService.findById(1L).orElse(null);
    }

    private Map<String, Object> toAbsenceRow(Object[] row) {
        Map<String, Object> absence = new LinkedHashMap<>();
        absence.put("id", row[0]);
        absence.put("type", row[1]);
        absence.put("motif", row[2]);
        absence.put("justificatifUrl", row[3]);
        absence.put("createdAt", row[4]);
        absence.put("dateSeance", row[5]);
        absence.put("heureDebut", row[6]);
        absence.put("heureFin", row[7]);
        absence.put("matiere", row[8] != null ? row[8] : "Matiere non definie");
        absence.put("classe", row[9] != null ? row[9] : "-");
        absence.put("salle", row[10] != null ? row[10] : "-");
        absence.put("professeur", formatNomProfesseur(row[11], row[12]));
        absence.put("typeLabel", "justifiee".equals(row[1]) ? "Justifiee" : "Non justifiee");
        absence.put("badgeClass", "justifiee".equals(row[1]) ? "badge badge-green" : "badge badge-red");
        return absence;
    }

    private String formatNomProfesseur(Object nom, Object prenom) {
        String nomTexte = nom != null ? nom.toString() : "";
        String prenomTexte = prenom != null ? prenom.toString() : "";
        String complet = (prenomTexte + " " + nomTexte).trim();
        return complet.isBlank() ? "-" : complet;
    }
}
