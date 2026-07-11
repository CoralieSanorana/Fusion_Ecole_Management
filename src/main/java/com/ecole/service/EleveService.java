package com.ecole.service;

import com.ecole.dto.Secretaire.*;
import com.ecole.entity.*;
import com.ecole.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JdbcTemplate jdbc;

    public EleveService(ProfilEtudiantRepository etudiantRepo,
                        InscriptionRepository inscriptionRepo,
                        ClasseRepository classeRepo,
                        PaiementRepository paiementRepo,
                        ProfilParentRepository parentRepo,
                        AnneeScolaireRepository anneeScolaireRepo,
                        JdbcTemplate jdbc) {
        this.etudiantRepo = etudiantRepo;
        this.inscriptionRepo = inscriptionRepo;
        this.classeRepo = classeRepo;
        this.paiementRepo = paiementRepo;
        this.parentRepo = parentRepo;
        this.anneeScolaireRepo = anneeScolaireRepo;
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
                    .map(Classe::getId)
                    .ifPresent(inscription::setClasseId);

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
    public ProfilEtudiant inscrireEleveComplet(AjoutEleveDTO dto) {
        // Vérifier si l'année scolaire active existe
        AnneeScolaire anneeActive = anneeScolaireRepo.findByEstActiveTrue()
                .orElseThrow(() -> new RuntimeException("Aucune année scolaire active. Veuillez d'abord initialiser l'année scolaire."));

        ProfilEtudiant etudiant = new ProfilEtudiant();
        etudiant.setNom(dto.getNom());
        etudiant.setPrenom(dto.getPrenom());
        etudiant.setDateNaissance(dto.getDateNaissance());
        etudiant.setSexe(dto.getSexe());
        etudiant.setLieuNaissance(dto.getLieuNaissance());
        etudiant.setNationalite(dto.getNationalite() != null ? dto.getNationalite() : "Malgache");
        etudiant.setCommune(dto.getCommune());
        etudiant.setAdresse(dto.getAdresse());
        etudiant.setTelephone(dto.getTelephone());
        etudiant.setMatricule(genererMatricule());
        etudiant.setCreatedAt(LocalDateTime.now());
        etudiant.setUpdatedAt(LocalDateTime.now());
        etudiant.setIsArchived(false);
        ProfilEtudiant saved = etudiantRepo.save(etudiant);
        log.info("Nouvel élève créé : {} {} (matricule: {})", saved.getPrenom(), saved.getNom(), saved.getMatricule());

        // Enregistrer le parent
        if (dto.getNomParent() != null && !dto.getNomParent().isBlank()) {
            ProfilParent parent = new ProfilParent();
            parent.setNom(dto.getNomParent());
            parent.setPrenom(dto.getPrenomParent() != null ? dto.getPrenomParent() : "");
            parent.setTelephone(dto.getTelephoneParent());
            parent.setLienParente(dto.getLienParente());
            parent.setCreatedAt(LocalDateTime.now());
            parent = parentRepo.save(parent);
            log.info("Parent enregistré : {} {}", parent.getPrenom(), parent.getNom());
            
            // Lier le parent à l'étudiant via la table de jointure
            jdbc.update("INSERT INTO etudiants_parents (etudiant_id, parent_id, est_contact_principal) VALUES (?, ?, true)",
                    saved.getId(), parent.getId());
        }

        // Créer l'inscription
        Long classeId = dto.getClasseIdAsLong();
        if (classeId != null) {
            Inscription inscription = new Inscription();
            inscription.setEtudiantId(saved.getId());
            inscription.setClasseId(classeId);
            inscription.setAnneeScolaireId(anneeActive.getId());
            inscription.setTypeInscription("nouvelle");
            inscription.setDateInscription(LocalDate.now());
            inscription.setStatut("active");
            inscription.setCreatedAt(LocalDateTime.now());
            inscription.setUpdatedAt(LocalDateTime.now());
            inscriptionRepo.save(inscription);
            log.info("Inscription créée — élève {} dans classe ID {}", saved.getId(), classeId);
        }

        return saved;
    }

    @Transactional
    public Inscription reinscrireEleve(Long etudiantId, Long classeId) {
        // Vérifier que l'élève existe
        ProfilEtudiant etudiant = etudiantRepo.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable (ID: " + etudiantId + ")"));

        // Vérifier année scolaire active
        AnneeScolaire anneeActive = anneeScolaireRepo.findByEstActiveTrue()
                .orElseThrow(() -> new RuntimeException("Aucune année scolaire active."));

        // Vérifier que la classe existe
        Classe classe = classeRepo.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe introuvable (ID: " + classeId + ")"));

        // Vérifier si l'élève n'est pas déjà inscrit cette année
        Optional<Inscription> existante = inscriptionRepo.findByEtudiantIdAndAnneeScolaireId(etudiantId, anneeActive.getId());
        if (existante.isPresent()) {
            throw new RuntimeException("Cet élève est déjà inscrit pour l'année scolaire " + anneeActive.getLibelle());
        }

        Inscription inscription = new Inscription();
        inscription.setEtudiantId(etudiantId);
        inscription.setClasseId(classeId);
        inscription.setAnneeScolaireId(anneeActive.getId());
        inscription.setTypeInscription("reinscription");
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut("active");
        inscription.setCreatedAt(LocalDateTime.now());
        inscription.setUpdatedAt(LocalDateTime.now());
        Inscription saved = inscriptionRepo.save(inscription);

        log.info("Réinscription effectuée — élève {} {} ({}) en classe {} pour l'année {}",
                etudiant.getPrenom(), etudiant.getNom(), etudiant.getMatricule(),
                classe.getNom(), anneeActive.getLibelle());

        return saved;
    }

    public List<ProfilEtudiant> rechercherElevesPourReinscription(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }
        return etudiantRepo.searchByNomOrPrenomOrMatricule(search);
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

    private List<EleveListeDTO> buildListeDTO(List<ProfilEtudiant> etudiants) {
        List<EleveListeDTO> result = new ArrayList<>();

        for (ProfilEtudiant e : etudiants) {
            String nomClasse = "";
            String niveau = "";

            // findActiveByEtudiantId accepte Long
            Optional<Inscription> inscOpt = inscriptionRepo.findActiveByEtudiantId(e.getId());
            if (inscOpt.isPresent()) {
                // Via @ManyToOne directement
                Classe classe = classeRepo.findById(inscOpt.get().getClasseId()).orElse(null);
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
