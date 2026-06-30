package com.ecole.service;

import com.ecole.dto.Directeur.EtudiantFilterCriteria;
import com.ecole.dto.Directeur.EtudiantFilterResult;
import com.ecole.entity.*;
import com.ecole.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EtudiantFilterService {

    private final ProfilEtudiantRepository etudiantRepo;
    private final ClasseRepository         classeRepo;
    private final NiveauRepository         niveauRepo;
    private final AnneeScolaireRepository  anneeRepo;
    private final InscriptionRepository inscriptionRepo;


    // ----------------------------------------------------------------
    // FILTRE PAGINÉ PRINCIPAL
    // ----------------------------------------------------------------

    public Page<EtudiantFilterResult> filtrer(EtudiantFilterCriteria criteria) {

        // Construire le Pageable avec tri
        Sort sort = buildSort(criteria.getSortBy(), criteria.getSortDir());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getPageSize(), sort);

        // Construire la Specification
        EtudiantSpecification spec = new EtudiantSpecification(criteria);

        // Exécuter la requête paginée
        Page<ProfilEtudiant> rawPage = etudiantRepo.findAll(spec, pageable);

        // Mapper vers EtudiantFilterResult
        List<EtudiantFilterResult> results = rawPage.getContent()
            .stream()
            .map(e -> toResult(e, criteria.getAnneeScolaireId()))
            .toList();

        return new PageImpl<>(results, pageable, rawPage.getTotalElements());
    }

    // ----------------------------------------------------------------
    // RÉCUPÉRATION D'UN ÉTUDIANT PAR ID
    // ----------------------------------------------------------------

    public EtudiantFilterResult getEtudiantById(Long id) {
        ProfilEtudiant etudiant = etudiantRepo.findById(id).orElse(null);
        if (etudiant == null) {
            return null;
        }
        return toResult(etudiant, null);
    }

    // ----------------------------------------------------------------
    // DONNÉES POUR LES SELECTS DU FORMULAIRE DE FILTRE
    // ----------------------------------------------------------------

    public Map<String, Object> getFilterFormData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("classes",         classeRepo.findAll(Sort.by("nom")));
        data.put("niveaux",         niveauRepo.findAll(Sort.by("ordre")));
        data.put("annees",          anneeRepo.findAll());
        data.put("anneeActive",     anneeRepo.findByEstActiveTrue().orElse(null));
        data.put("statuts",         List.of("active","transfere","exclu","diplome","abandonne"));
        data.put("typesInscription",List.of("nouvelle","reinscription"));
        return data;
    }

    // ----------------------------------------------------------------
    // HELPERS PRIVÉS
    // ----------------------------------------------------------------

private EtudiantFilterResult toResult(ProfilEtudiant e, Long filtreAnneeId) {

    EtudiantFilterResult r = new EtudiantFilterResult();

    r.setEtudiantId(e.getId());
    r.setMatricule(e.getMatricule());
    r.setNom(e.getNom());
    r.setPrenom(e.getPrenom());
    r.setDateNaissance(e.getDateNaissance());
    r.setLieuNaissance(e.getLieuNaissance());
    r.setSexe(e.getSexe());
    r.setRegion(e.getRegion());
    r.setNationalite(e.getNationalite());
    r.setTelephone(e.getTelephone());
    r.setIsArchived(e.getIsArchived());

    // ❌ supprimé getUser() si pas existant
    r.setEmail(null);

    // 🔥 CHARGEMENT INSCRIPTIONS VIA REPO
    List<Inscription> inscriptions =
            inscriptionRepo.findByEtudiantId(e.getId());

    if (inscriptions != null && !inscriptions.isEmpty()) {

        Inscription insc = choisirInscription(inscriptions, filtreAnneeId);

        if (insc != null) {

            r.setInscriptionId(insc.getId());
            r.setStatutInscription(insc.getStatut());
            r.setTypeInscription(insc.getTypeInscription());
            r.setDateInscription(insc.getDateInscription());

            // 🔥 Classe via repo
            if (insc.getClasseId() != null) {
                classeRepo.findById(insc.getClasseId()).ifPresent(classe -> {
                    r.setClasseNom(classe.getNom());

                    if (classe.getNiveau() != null) {
                        r.setNiveauLibelle(classe.getNiveau().getLibelle());
                    }
                });
            }

            // 🔥 Année via repo
            if (insc.getAnneeScolaireId() != null) {
                anneeRepo.findById(insc.getAnneeScolaireId()).ifPresent(annee -> {
                    r.setAnneeLibelle(annee.getLibelle());
                });
            }
        }
    }

    return r;
}

private Inscription choisirInscription(List<Inscription> inscriptions, Long anneeId) {

    if (inscriptions == null || inscriptions.isEmpty()) {
        return null;
    }

    if (anneeId != null) {
        return inscriptions.stream()
                .filter(i -> anneeId.equals(i.getAnneeScolaireId()))
                .findFirst()
                .orElse(null);
    }

    return inscriptions.get(inscriptions.size() - 1);
}

    private Sort buildSort(String sortBy, String sortDir) {
        String field = switch (sortBy == null ? "nom" : sortBy) {
            case "prenom"          -> "prenom";
            case "matricule"       -> "matricule";
            case "dateNaissance"   -> "dateNaissance";
            default                -> "nom";
        };
        return "desc".equalsIgnoreCase(sortDir)
            ? Sort.by(Sort.Direction.DESC, field)
            : Sort.by(Sort.Direction.ASC,  field);
    }
}
