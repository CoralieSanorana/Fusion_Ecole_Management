package com.ecole.service;

import com.ecole.dto.Secretaire.*;
import com.ecole.entity.*;
import com.ecole.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EleveService {

    private static final Logger log = LoggerFactory.getLogger(EleveService.class);

    private final ProfilEtudiantRepository etudiantRepo;
    private final InscriptionRepository inscriptionRepo;
    private final ClasseRepository classeRepo;
    private final PaiementRepository paiementRepo;
    private final ProfilParentRepository parentRepo;
    private final AnneeScolaireRepository anneeScolaireRepo;
    private final HistoriqueReinscriptionRepository historiqueReinscriptionRepo;
    private final JdbcTemplate jdbc;

    public EleveService(ProfilEtudiantRepository etudiantRepo,
                        InscriptionRepository inscriptionRepo,
                        ClasseRepository classeRepo,
                        PaiementRepository paiementRepo,
                        ProfilParentRepository parentRepo,
                        AnneeScolaireRepository anneeScolaireRepo,
                        HistoriqueReinscriptionRepository historiqueReinscriptionRepo,
                        JdbcTemplate jdbc) {
        this.etudiantRepo = etudiantRepo;
        this.inscriptionRepo = inscriptionRepo;
        this.classeRepo = classeRepo;
        this.paiementRepo = paiementRepo;
        this.parentRepo = parentRepo;
        this.anneeScolaireRepo = anneeScolaireRepo;
        this.historiqueReinscriptionRepo = historiqueReinscriptionRepo;
        this.jdbc = jdbc;
    }

    public List<EleveListeDTO> listerTousEleves() {
        return buildListeDTO(etudiantRepo.findByIsArchivedFalse());
    }

    public List<EleveListeDTO> listerParNiveau(String niveau) {
        return buildListeDTO(etudiantRepo.findByNiveau(niveau));
    }

    // Long → Integer
    public List<EleveListeDTO> listerParClasse(Long classeId) {
    return buildListeDTO(etudiantRepo.findByClasseId(classeId));
}

    public List<EleveListeDTO> rechercherEleves(String search) {
        return buildListeDTO(etudiantRepo.searchByNomOrPrenomOrMatricule(search));
    }

    public List<Classe> listerClasses() {
        return classeRepo.findAllActiveAnnee();
    }

    public List<AnneeScolaire> listerAnneesScolaires() {
        return anneeScolaireRepo.findAll().stream()
                .sorted(Comparator.comparing(AnneeScolaire::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional
    public void ensureAnneeScolaireDisponible() {
        List<AnneeScolaire> annees = anneeScolaireRepo.findAll();
        if (annees.isEmpty()) {
            creerAnneeScolaire(LocalDate.now().getYear(), true);
            return;
        }

        Optional<AnneeScolaire> activeYear = annees.stream()
                .filter(AnneeScolaire::getEstActive)
                .findFirst();

        if (activeYear.isPresent()) {
            int nextStartYear = activeYear.get().getDateFin() != null
                    ? activeYear.get().getDateFin().getYear()
                    : activeYear.get().getDateDebut() != null ? activeYear.get().getDateDebut().getYear() + 1 : LocalDate.now().getYear();
            String nextLabel = nextStartYear + "-" + (nextStartYear + 1);
            boolean hasNextYear = annees.stream().anyMatch(annee -> Objects.equals(annee.getLibelle(), nextLabel));
            if (!hasNextYear) {
                creerAnneeScolaire(nextStartYear, false);
            }
        }
    }

    private void creerAnneeScolaire(int startYear, boolean active) {
        String label = startYear + "-" + (startYear + 1);
        if (anneeScolaireRepo.findAll().stream().anyMatch(annee -> Objects.equals(annee.getLibelle(), label))) {
            return;
        }

        AnneeScolaire annee = new AnneeScolaire();
        annee.setLibelle(label);
        annee.setDateDebut(LocalDate.of(startYear, 9, 1));
        annee.setDateFin(LocalDate.of(startYear + 1, 7, 31));
        annee.setEstActive(active);
        annee.setCreatedAt(LocalDateTime.now());
        anneeScolaireRepo.save(annee);
    }

    public List<EtudiantRechercheDTO> rechercherEtudiantsPourReinscription(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }
        return etudiantRepo.searchByNomOrPrenomOrMatricule(search).stream()
                .filter(etudiant -> Boolean.TRUE.equals(etudiant.getIsArchived()) == false)
                .map(this::toRechercheDTO)
                .toList();
    }

    @Transactional
    public Inscription reinscrireEtudiant(ReinscriptionRequestDTO dto, Long changePar) {
        if (dto == null || dto.getEtudiantId() == null || dto.getAnneeScolaireId() == null || dto.getClasseId() == null) {
            throw new IllegalArgumentException("Les informations de réinscription sont incomplètes.");
        }
        if (inscriptionRepo.findByEtudiantIdAndAnneeScolaireId(dto.getEtudiantId(), dto.getAnneeScolaireId()).isPresent()) {
            throw new IllegalStateException("Cet élève est déjà inscrit pour cette année scolaire.");
        }

        ProfilEtudiant etudiant = etudiantRepo.findById(dto.getEtudiantId())
                .orElseThrow(() -> new IllegalArgumentException("Élève introuvable."));
        Classe classe = classeRepo.findById(dto.getClasseId())
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
        anneeScolaireRepo.findById(dto.getAnneeScolaireId())
                .orElseThrow(() -> new IllegalArgumentException("Année scolaire introuvable."));

        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setEtudiantId(etudiant.getId());
        inscription.setClasse(classe);
        inscription.setClasseId(classe.getId());
        inscription.setAnneeScolaireId(dto.getAnneeScolaireId());
        inscription.setTypeInscription("reinscription");
        inscription.setDateInscription(dto.getDateInscription() != null ? dto.getDateInscription() : LocalDate.now());
        inscription.setStatut(dto.getStatut() != null && !dto.getStatut().isBlank() ? dto.getStatut() : "active");
        inscription.setCreatedAt(LocalDateTime.now());
        inscription.setUpdatedAt(LocalDateTime.now());
        Inscription saved = inscriptionRepo.save(inscription);

        Inscription ancienneInscription = inscriptionRepo.findByEtudiantId(dto.getEtudiantId()).stream()
                .filter(item -> !Objects.equals(item.getId(), saved.getId()))
                .filter(item -> item.getDateInscription() != null)
                .max(Comparator.comparing(Inscription::getDateInscription, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (ancienneInscription != null) {
            HistoriqueReinscription historique = new HistoriqueReinscription();
            historique.setEtudiantId(dto.getEtudiantId());
            historique.setInscriptionIdOld(ancienneInscription.getId());
            historique.setInscriptionIdNew(saved.getId());
            historique.setAncienneAnneeScolaireId(ancienneInscription.getAnneeScolaireId());
            historique.setNouvelleAnneeScolaireId(saved.getAnneeScolaireId());
            historique.setAncienneClasseId(ancienneInscription.getClasseId());
            historique.setNouvelleClasseId(saved.getClasseId());
            historique.setAncienStatut(ancienneInscription.getStatut());
            historique.setAncienRangFinal(ancienneInscription.getRangFinal());
            historique.setAncienResultat(Boolean.TRUE.equals(ancienneInscription.getEstAdmis()) ? "admis" : "non_admis");
            historique.setAncienneMoyenneGenerale(calculerMoyenneGenerale(ancienneInscription.getId()));
            historique.setAbsencesAnneePrecedente(calculerAbsences(ancienneInscription.getEtudiantId(), ancienneInscription.getAnneeScolaireId()));
            historique.setChangePar(changePar);
            historiqueReinscriptionRepo.save(historique);
        }

        return saved;
    }

    // Long → Integer
    public EleveProfilDTO getProfil(Long etudiantId) {
        ProfilEtudiant etudiant = etudiantRepo.findById(etudiantId).orElse(null);

        log.info("getProfil appelé pour id={} nom={}", etudiantId, etudiant.getNom());

        EleveProfilDTO dto = new EleveProfilDTO();
        dto.setId(etudiant.getId());
        dto.setMatricule(etudiant.getMatricule());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setPhotoUrl(etudiant.getPhotoUrl());
        dto.setDateNaissance(etudiant.getDateNaissance());
        dto.setCommune(etudiant.getCommune());
        dto.setAdresse(etudiant.getAdresse());

        // findActiveByEtudiantId accepte Long
        inscriptionRepo.findActiveByEtudiantId(etudiantId).ifPresent(insc -> {
            dto.setDateInscription(insc.getDateInscription());
            // getClasse() via @ManyToOne — plus besoin de findById(classeId)
            if (insc.getClasseId() != null) {
                dto.setNomClasse(classeRepo.findById(insc.getClasseId()).orElse(null).getNom());
            }
        });

        parentRepo.findContactPrincipalByEtudiantId(etudiantId).ifPresent(parent -> {
            dto.setNomParent(parent.getNom());
            dto.setPrenomParent(parent.getPrenom());
            dto.setTelephoneParent(parent.getTelephone());
            dto.setLienParente(parent.getLienParente());
        });

        List<Paiement> paiements = paiementRepo.findByEtudiantIdCurrentYear(etudiantId);
        List<PaiementHistoriqueDTO> historique = new ArrayList<>();
        for (Paiement p : paiements) {
            String moisLabel = p.getDatePaiement().getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                + " " + p.getDatePaiement().getYear();
            historique.add(new PaiementHistoriqueDTO(
                capitalize(moisLabel),
                p.getDatePaiement(),
                p.getMontant(),
                "Payé"
            ));
        }
        dto.setHistoriquesPaiements(historique);

        return dto;
    }

    @Transactional
    public ProfilEtudiant ajouterEleve(AjoutEleveDTO dto) {
        ProfilEtudiant etudiant = new ProfilEtudiant();
        etudiant.setNom(dto.getNom());
        etudiant.setPrenom(dto.getPrenom());
        etudiant.setDateNaissance(dto.getDateNaissance());
        etudiant.setCommune(dto.getCommune());
        etudiant.setAdresse(dto.getAdresse());
        etudiant.setMatricule(genererMatricule());
        etudiant.setCreatedAt(LocalDateTime.now());
        etudiant.setUpdatedAt(LocalDateTime.now());
        etudiant.setIsArchived(false);
        ProfilEtudiant saved = etudiantRepo.save(etudiant);

        if (dto.getNomParent() != null && !dto.getNomParent().isBlank()) {
            ProfilParent parent = new ProfilParent();
            parent.setNom(dto.getNomParent());
            parent.setPrenom(dto.getPrenomParent() != null ? dto.getPrenomParent() : "");
            parent.setTelephone(dto.getTelephoneParent());
            parent.setLienParente(dto.getLienParente());
            parent.setCreatedAt(LocalDateTime.now());
            parentRepo.save(parent);
        }

        // Long → Integer pour classeId
        Long classeIdInt = dto.getClasseIdAsLong();
        if (classeIdInt != null) {
            Inscription inscription = new Inscription();

            // Utiliser les objets @ManyToOne au lieu des IDs bruts
            etudiantRepo.findById(saved.getId())
            .map(ProfilEtudiant::getId)
            .ifPresent(inscription::setEtudiantId);

            classeRepo.findById(Long.valueOf(classeIdInt))
                    .ifPresent(c -> {
                        inscription.setClasseId(c.getId());
                        inscription.setClasse(c);
                    });

            log.info("ajouterEleve: eleveId={} classeIdForm={} inscriptionEtudiantId={} inscriptionClasseId={}",
                    saved.getId(),
                    classeIdInt,
                    inscription.getEtudiantId(),
                    inscription.getClasseId());


            inscription.setTypeInscription("nouvelle");
            inscription.setDateInscription(LocalDate.now());
            inscription.setStatut("active");
            inscription.setCreatedAt(LocalDateTime.now());
            inscription.setUpdatedAt(LocalDateTime.now());

            anneeScolaireRepo.findByEstActiveTrue().ifPresentOrElse(
                annee -> {
                    inscription.setAnneeScolaireId(annee.getId());
                    log.info("Inscription créée avec année scolaire : {}", annee.getLibelle());
                },
                () -> log.warn("Aucune année scolaire active trouvée")
            );

            inscriptionRepo.save(inscription);
        }

        return saved;
    }

    @Transactional
    public void soumettreDemandeModification(Long etudiantId, String champModifie,
                                              String ancienneValeur, String nouvelleValeur,
                                              String motif) {
        jdbc.update("""
            INSERT INTO demandes_modification_dossier
                (etudiant_id, champ_modifie, ancienne_valeur, nouvelle_valeur, motif, statut, created_at)
            VALUES (?, ?, ?, ?, ?, 'en_attente', NOW())
            """,
            etudiantId, champModifie, ancienneValeur, nouvelleValeur, motif
        );
        log.info("Demande modification soumise — élève {} champ '{}'", etudiantId, champModifie);
    }

    private EtudiantRechercheDTO toRechercheDTO(ProfilEtudiant etudiant) {
        EtudiantRechercheDTO dto = new EtudiantRechercheDTO();
        dto.setId(etudiant.getId());
        dto.setMatricule(etudiant.getMatricule());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setTelephone(etudiant.getTelephone());

        inscriptionRepo.findActiveByEtudiantId(etudiant.getId()).ifPresent(inscription -> {
            dto.setClasseId(inscription.getClasseId());
            Classe classe = inscription.getClasse();
            if (classe == null && inscription.getClasseId() != null) {
                classe = classeRepo.findById(inscription.getClasseId()).orElse(null);
            }
            if (classe != null) {
                dto.setNomClasse(classe.getNom());
            }
        });
        return dto;
    }

    private BigDecimal calculerMoyenneGenerale(Long inscriptionId) {
        try {
            return jdbc.queryForObject(
                    "SELECT ROUND(AVG(valeur), 2) FROM moyennes WHERE inscription_id = ? AND matiere_id IS NULL",
                    BigDecimal.class,
                    inscriptionId);
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer calculerAbsences(Long etudiantId, Long anneeScolaireId) {
        try {
            return jdbc.queryForObject(
                    "SELECT COUNT(*) FROM absences ab " +
                            "JOIN seances s ON s.id = ab.seance_id " +
                            "JOIN emploi_du_temps edt ON edt.id = s.emploi_du_temps_id " +
                            "JOIN affectations_enseignement ae ON ae.id = edt.affectation_id " +
                            "WHERE ab.etudiant_id = ? AND ae.annee_scolaire_id = ?",
                    Integer.class,
                    etudiantId,
                    anneeScolaireId);
        } catch (Exception ex) {
            return 0;
        }
    }

    private List<EleveListeDTO> buildListeDTO(List<ProfilEtudiant> etudiants) {
        List<EleveListeDTO> result = new ArrayList<>();

        for (ProfilEtudiant e : etudiants) {
            String nomClasse = "";
            String niveau = "";

            // findActiveByEtudiantId accepte Long
            Optional<Inscription> inscOpt = inscriptionRepo.findActiveByEtudiantId(e.getId());
            if (inscOpt.isPresent()) {
                // Via @ManyToOne ou colonne classe_id selon ce qui est renseigné
                Inscription insc = inscOpt.get();
                Classe classe = null;
                if (insc.getClasse() != null) {
                    classe = insc.getClasse();
                } else if (insc.getClasseId() != null) {
                    classe = classeRepo.findById(insc.getClasseId()).orElse(null);
                }
                if (classe != null) {
                    nomClasse = classe.getNom();
                    niveau = detecterNiveau(nomClasse);
                }
            }

            List<Paiement> paiements = paiementRepo.findByEtudiantIdCurrentYear(e.getId());
            Map<Integer, Boolean> paiementsMois = new HashMap<>();
            for (int m = 1; m <= 12; m++) paiementsMois.put(m, false);
            for (Paiement p : paiements) {
                paiementsMois.put(p.getDatePaiement().getMonthValue(), true);
            }

            result.add(new EleveListeDTO(
                e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(),
                nomClasse, niveau, e.getPhotoUrl(), paiementsMois
            ));
        }
        return result;
    }

    private String detecterNiveau(String nomClasse) {
        if (nomClasse == null) return "";
        String n = nomClasse.toLowerCase();
        if (n.contains("terminal") || n.contains("tle")) return "Terminale";
        if (n.contains("premi") || n.contains("1ère") || n.contains("1ere")) return "Première";
        if (n.contains("second") || n.contains("2nde")) return "Seconde";
        return "";
    }

    private String genererMatricule() {
        int annee = LocalDate.now().getYear();
        long count = etudiantRepo.count() + 1;
        return String.format("MAT-%d-%04d", annee, count);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}