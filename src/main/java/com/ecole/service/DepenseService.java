package com.ecole.service;

import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Depense;
import com.ecole.entity.TypeDepense;
import com.ecole.entity.User;
import com.ecole.dto.Secretaire.PeriodeOptionDTO;
import com.ecole.repository.AnneeScolaireRepository;
import com.ecole.repository.DepenseRepository;
import com.ecole.repository.TypeDepenseRepository;
import com.ecole.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final TypeDepenseRepository typeDepenseRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final UserRepository userRepository;

    public List<TypeDepense> getTypesDepenses() {
        return typeDepenseRepository.findAllByOrderByLibelleAsc();
    }

    public List<AnneeScolaire> getAnneesScolaires() {
        return anneeScolaireRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getDateDebut() == null && b.getDateDebut() == null) return 0;
                    if (a.getDateDebut() == null) return 1;
                    if (b.getDateDebut() == null) return -1;
                    return b.getDateDebut().compareTo(a.getDateDebut());
                })
                .toList();
    }

    public List<Depense> getDepenses(Long anneeScolaireId, YearMonth mois) {
        Long resolvedAnnee = resolveAnneeId(anneeScolaireId);
        YearMonth resolvedMonth = mois != null ? mois : YearMonth.now();
        return depenseRepository.findByAnneeScolaire_IdAndDateDepenseBetweenOrderByDateDepenseDesc(
                resolvedAnnee,
                resolvedMonth.atDay(1),
                resolvedMonth.atEndOfMonth());
    }

    public BigDecimal totalDepenses(Long anneeScolaireId, YearMonth mois) {
        Long resolvedAnnee = resolveAnneeId(anneeScolaireId);
        YearMonth resolvedMonth = mois != null ? mois : YearMonth.now();
        return depenseRepository.sumTotalByAnneeScolaireAndDateBetween(
                resolvedAnnee,
                resolvedMonth.atDay(1),
                resolvedMonth.atEndOfMonth());
    }

    public List<PeriodeOptionDTO> getPeriodes(Long anneeScolaireId) {
        AnneeScolaire annee = anneeScolaireRepository.findById(resolveAnneeId(anneeScolaireId))
                .orElseThrow(() -> new IllegalArgumentException("Année scolaire introuvable."));

        if (annee.getDateDebut() == null || annee.getDateFin() == null) {
            return List.of();
        }

        List<PeriodeOptionDTO> periodes = new ArrayList<>();
        YearMonth debut = YearMonth.from(annee.getDateDebut());
        YearMonth fin = YearMonth.from(annee.getDateFin());
        YearMonth courant = debut;
        while (!courant.isAfter(fin)) {
            PeriodeOptionDTO option = new PeriodeOptionDTO();
            option.setValue(courant.toString());
            option.setLabel(formatMonth(courant));
            option.setSelected(courant.equals(YearMonth.now()));
            periodes.add(option);
            courant = courant.plusMonths(1);
        }
        return periodes;
    }

    public YearMonth resolvePeriode(String periode, Long anneeScolaireId) {
        List<PeriodeOptionDTO> periodes = getPeriodes(anneeScolaireId);
        if (periode != null && !periode.isBlank()) {
            try {
                YearMonth candidate = YearMonth.parse(periode);
                boolean exists = periodes.stream().anyMatch(p -> p.getValue().equals(candidate.toString()));
                if (exists) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }
        if (!periodes.isEmpty()) {
            return YearMonth.parse(periodes.get(periodes.size() - 1).getValue());
        }
        return YearMonth.now();
    }

    @Transactional
    public Depense saveDepense(Long anneeScolaireId,
                               Long typeDepenseId,
                               String description,
                               BigDecimal prixUnitaire,
                               BigDecimal quantite,
                               LocalDate dateDepense) {
        if (typeDepenseId == null) {
            throw new IllegalArgumentException("Le type de dépense est obligatoire.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire.");
        }
        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire doit être positif.");
        }
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }

        TypeDepense typeDepense = typeDepenseRepository.findById(typeDepenseId)
                .orElseThrow(() -> new IllegalArgumentException("Type de dépense introuvable."));
        AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(resolveAnneeId(anneeScolaireId))
                .orElseThrow(() -> new IllegalArgumentException("Année scolaire introuvable."));

        Depense depense = new Depense();
        depense.setAnneeScolaire(anneeScolaire);
        depense.setIntitule(typeDepense.getLibelle());
        depense.setTypeCharge("variable");
        depense.setTypeDepense(typeDepense);
        depense.setMotif(description.trim());
        depense.setDescription(description.trim());
        depense.setMontant(prixUnitaire.multiply(quantite).setScale(2, RoundingMode.HALF_UP));
        depense.setPrixUnitaire(prixUnitaire);
        depense.setQuantite(quantite);
        depense.setTotal(depense.getMontant());
        depense.setDateDepense(dateDepense != null ? dateDepense : LocalDate.now());
        depense.setSaisiPar(resolveCurrentUser().orElse(null));
        return depenseRepository.save(depense);
    }

    @Transactional
    public TypeDepense saveTypeDepense(String libelle, String description) {
        if (libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("Le libellé du type est obligatoire.");
        }
        TypeDepense typeDepense = new TypeDepense();
        typeDepense.setLibelle(libelle.trim());
        typeDepense.setDescription(description != null ? description.trim() : null);
        return typeDepenseRepository.save(typeDepense);
    }

    private Long resolveAnneeId(Long anneeScolaireId) {
        if (anneeScolaireId != null) {
            return anneeScolaireId;
        }
        return anneeScolaireRepository.findByEstActiveTrue()
                .map(AnneeScolaire::getId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune année scolaire active n'est disponible."));
    }

    private Optional<User> resolveCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    private String formatMonth(YearMonth month) {
        String libelle = month.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return libelle.substring(0, 1).toUpperCase(Locale.ROOT) + libelle.substring(1) + " " + month.getYear();
    }
}
