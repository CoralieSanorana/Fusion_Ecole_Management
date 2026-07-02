package com.ecole.service;

import com.ecole.dto.Directeur.*;
import com.ecole.entity.AnneeScolaire;
import com.ecole.entity.Periode;
import com.ecole.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module "Statistiques Élèves" — détection décrochage.
 *
 * Croise, pour chaque étudiant, la moyenne générale sur 3 périodes consécutives
 * et le taux d'absence sur cette même plage, afin de produire :
 *  - une Liste Rouge (baisse ≥ seuil ET absence ≥ seuil)
 *  - une Liste Jaune (baisse ≥ seuil OU absence ≥ seuil)
 *  - un nuage de points (absence × moyenne) par classe + corrélation de Pearson
 *
 * Donnée actionnelle : chaque ligne porte les motifs précis de l'alerte.
 */
@Service
@RequiredArgsConstructor
public class StatistiquesElevesService {

    private final AnneeScolaireRepository anneeScolaireRepo;
    private final PeriodeRepository       periodeRepo;
    private final ClasseRepository        classeRepo;
    private final InscriptionRepository   inscriptionRepo;
    private final MoyenneRepository       moyenneRepo;
    private final AbsenceRepository       absenceRepo;

    // ----------------------------------------------------------------
    // POINT D'ENTRÉE
    // ----------------------------------------------------------------

    public StatistiquesElevesResponse analyser(StatistiquesElevesCriteria criteria) {

        double seuilBaisse  = criteria.getSeuilBaisseMoyenne() != null ? criteria.getSeuilBaisseMoyenne() : 2.0;
        double seuilAbsence = criteria.getSeuilTauxAbsence()  != null ? criteria.getSeuilTauxAbsence()  : 15.0;

        // 1) Résoudre l'année scolaire (paramètre ou année active)
        AnneeScolaire annee = resoudreAnneeScolaire(criteria.getAnneeScolaireId());
        if (annee == null) {
            return reponseVide(seuilBaisse, seuilAbsence);
        }

        // 2) Isoler les 3 dernières périodes consécutives (ordre croissant)
        List<Periode> periodesAnalyse = resoudrePeriodesConsecutives(annee.getId(), criteria.getPeriodeFinId());
        if (periodesAnalyse.isEmpty()) {
            return reponseVide(seuilBaisse, seuilAbsence);
        }

        List<Integer> periodeIds = periodesAnalyse.stream().map(Periode::getId).toList();
        Integer premierePeriodeId  = periodeIds.get(0);
        Integer dernierePeriodeId  = periodeIds.get(periodeIds.size() - 1);
        LocalDate dateDebutPlage = periodesAnalyse.get(0).getDateDebut();
        LocalDate dateFinPlage   = periodesAnalyse.get(periodesAnalyse.size() - 1).getDateFin();

        // 3) Récupérer les élèves actifs concernés (toutes classes ou une classe précise)
        List<Object[]> elevesRaw = inscriptionRepo.findElevesActifsPourStatistiques(annee.getId(), criteria.getClasseId());
        if (elevesRaw.isEmpty()) {
            return reponseVide(seuilBaisse, seuilAbsence);
        }

        List<Long> inscriptionIds = new ArrayList<>();
        for (Object[] row : elevesRaw) {
            inscriptionIds.add(((Number) row[0]).longValue());
        }

        // 4) Récupérer les moyennes générales (matiere_id IS NULL) pour ces périodes/inscriptions
        Map<Long, Map<Integer, BigDecimal>> moyennesParEtudiant = chargerMoyennes(periodeIds, inscriptionIds);

        // 5) Construire les points étudiant par étudiant, classe par classe (les absences se calculent par classe)
        Map<Long, List<EleveStatPoint>> pointsParClasse = new LinkedHashMap<>(); // classeId -> points
        Map<Long, String[]> infosClasse = new LinkedHashMap<>(); // classeId -> [classeNom, niveauLibelle]

        // Regrouper d'abord les lignes brutes par classe pour limiter les requêtes d'absence (1 par classe)
        Map<Long, List<Object[]>> lignesParClasse = new LinkedHashMap<>();
        for (Object[] row : elevesRaw) {
            Long classeId = ((Number) row[6]).longValue();
            lignesParClasse.computeIfAbsent(classeId, k -> new ArrayList<>()).add(row);
            infosClasse.putIfAbsent(classeId, new String[]{ (String) row[7], (String) row[8] });
        }

        for (Map.Entry<Long, List<Object[]>> entry : lignesParClasse.entrySet()) {
            Long classeId = entry.getKey();
            List<Object[]> lignesClasse = entry.getValue();

            Long nbSeancesPrevues = absenceRepo.countSeancesPrevues(classeId, dateDebutPlage, dateFinPlage);
            if (nbSeancesPrevues == null) nbSeancesPrevues = 0L;

            Map<Long, Long> absencesParEtudiant = absenceRepo.countAbsencesParEtudiant(classeId, dateDebutPlage, dateFinPlage)
                    .stream()
                    .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));

            List<EleveStatPoint> points = new ArrayList<>();
            for (Object[] row : lignesClasse) {
                Long inscriptionId = ((Number) row[0]).longValue();
                Long etudiantId    = ((Number) row[1]).longValue();
                String matricule   = (String) row[2];
                String nom         = (String) row[3];
                String prenom      = (String) row[4];
                String photoUrl    = (String) row[5];
                String classeNom   = (String) row[7];

                Map<Integer, BigDecimal> moyennesEtudiant = moyennesParEtudiant.getOrDefault(etudiantId, Map.of());
                List<BigDecimal> moyennesOrdonnees = periodeIds.stream()
                        .map(moyennesEtudiant::get)
                        .toList();

                BigDecimal moyenneRecente   = moyennesEtudiant.get(dernierePeriodeId);
                BigDecimal moyennePremiere  = moyennesEtudiant.get(premierePeriodeId);
                BigDecimal deltaMoyenne = (moyennePremiere != null && moyenneRecente != null)
                        ? moyennePremiere.subtract(moyenneRecente)
                        : null;

                long nbAbsences = absencesParEtudiant.getOrDefault(etudiantId, 0L);
                BigDecimal tauxAbsence = (nbSeancesPrevues > 0)
                        ? BigDecimal.valueOf(nbAbsences * 100.0 / nbSeancesPrevues).setScale(1, RoundingMode.HALF_UP)
                        : null;

                List<String> motifs = new ArrayList<>();
                boolean baisseOk  = deltaMoyenne != null && deltaMoyenne.doubleValue() >= seuilBaisse;
                boolean absenceOk = tauxAbsence  != null && tauxAbsence.doubleValue()  >= seuilAbsence;

                if (baisseOk) {
                    motifs.add("Baisse de moyenne de " + deltaMoyenne.setScale(1, RoundingMode.HALF_UP)
                            + " pts entre " + periodesAnalyse.get(0).getLibelle() + " et " + periodesAnalyse.get(periodesAnalyse.size() - 1).getLibelle());
                }
                if (absenceOk) {
                    motifs.add("Taux d'absence " + tauxAbsence + "% (seuil " + seuilAbsence + "%)");
                }

                NiveauAlerte niveau = baisseOk && absenceOk ? NiveauAlerte.ROUGE
                        : (baisseOk || absenceOk) ? NiveauAlerte.JAUNE
                        : NiveauAlerte.AUCUNE;

                EleveStatPoint point = new EleveStatPoint(
                        etudiantId, matricule, nom, prenom, photoUrl,
                        classeId, classeNom, inscriptionId,
                        moyennesOrdonnees, moyenneRecente, deltaMoyenne,
                        nbSeancesPrevues.intValue(), (int) nbAbsences, tauxAbsence,
                        niveau, motifs
                );
                points.add(point);
            }
            pointsParClasse.put(classeId, points);
        }

        // 6) Construire les ClasseStatistique (nuage de points + corrélation) par classe
        List<ClasseStatistique> classes = new ArrayList<>();
        List<EleveStatPoint> tousLesPoints = new ArrayList<>();
        for (Map.Entry<Long, List<EleveStatPoint>> entry : pointsParClasse.entrySet()) {
            Long classeId = entry.getKey();
            List<EleveStatPoint> points = entry.getValue();
            tousLesPoints.addAll(points);

            int nbRouge = (int) points.stream().filter(p -> p.getNiveauAlerte() == NiveauAlerte.ROUGE).count();
            int nbJaune = (int) points.stream().filter(p -> p.getNiveauAlerte() == NiveauAlerte.JAUNE).count();

            ClasseStatistique cs = new ClasseStatistique(
                    classeId,
                    infosClasse.get(classeId)[0],
                    infosClasse.get(classeId)[1],
                    points.size(),
                    nbRouge,
                    nbJaune,
                    calculerCorrelation(points),
                    points
            );
            classes.add(cs);
        }
        // Tri des classes par nom pour un affichage stable
        classes.sort(Comparator.comparing(ClasseStatistique::getClasseNom, Comparator.nullsLast(String::compareTo)));

        // 7) Listes Rouge / Jaune (toutes classes confondues), triées par baisse de moyenne décroissante
        Comparator<EleveStatPoint> parDeltaDesc = Comparator.comparing(
                (EleveStatPoint p) -> p.getDeltaMoyenne() != null ? p.getDeltaMoyenne() : BigDecimal.ZERO
        ).reversed();

        List<EleveStatPoint> listeRouge = tousLesPoints.stream()
                .filter(p -> p.getNiveauAlerte() == NiveauAlerte.ROUGE)
                .sorted(parDeltaDesc)
                .toList();

        List<EleveStatPoint> listeJaune = tousLesPoints.stream()
                .filter(p -> p.getNiveauAlerte() == NiveauAlerte.JAUNE)
                .sorted(parDeltaDesc)
                .toList();

        // 8) Réponse finale
        StatistiquesElevesResponse response = new StatistiquesElevesResponse();
        response.setAnneeLibelle(annee.getLibelle());
        response.setPeriodesAnalysees(periodesAnalyse.stream().map(Periode::getLibelle).toList());
        response.setSeuilBaisseMoyenne(seuilBaisse);
        response.setSeuilTauxAbsence(seuilAbsence);
        response.setEffectifTotal(tousLesPoints.size());
        response.setNbAlerteRouge(listeRouge.size());
        response.setNbAlerteJaune(listeJaune.size());
        response.setCorrelationGlobale(calculerCorrelation(tousLesPoints));
        response.setListeRouge(listeRouge);
        response.setListeJaune(listeJaune);
        response.setClasses(classes);
        return response;
    }

    // ----------------------------------------------------------------
    // HELPER PUBLIC — liste des classes pour peupler le filtre de la vue
    // ----------------------------------------------------------------

    public List<com.ecole.entity.Classe> listerClasses(Long anneeScolaireId) {
        Long anneeId = anneeScolaireId;
        if (anneeId == null) {
            AnneeScolaire active = anneeScolaireRepo.findByEstActiveTrue().orElse(null);
            if (active == null) {
                return List.of();
            }
            anneeId = active.getId();
        }
        return classeRepo.findByAnneeScolaire_Id(anneeId);
    }

    public List<Periode> listerPeriodes(Long anneeScolaireId) {
        Long anneeId = anneeScolaireId;
        if (anneeId == null) {
            AnneeScolaire active = anneeScolaireRepo.findByEstActiveTrue().orElse(null);
            if (active == null) {
                return List.of();
            }
            anneeId = active.getId();
        }
        return periodeRepo.findByAnneeScolaireIdOrderByOrdreAsc(anneeId);
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    private AnneeScolaire resoudreAnneeScolaire(Long anneeScolaireId) {
        if (anneeScolaireId != null) {
            return anneeScolaireRepo.findById(anneeScolaireId).orElse(null);
        }
        return anneeScolaireRepo.findByEstActiveTrue().orElse(null);
    }

    /**
     * Renvoie les 3 dernières périodes consécutives (ordre croissant) se terminant
     * à periodeFinId (ou à la dernière période disponible si null).
     * S'il y a moins de 3 périodes disponibles, renvoie celles qui existent.
     */
    private List<Periode> resoudrePeriodesConsecutives(Long anneeScolaireId, Integer periodeFinId) {
        List<Periode> toutes = periodeRepo.findByAnneeScolaireIdOrderByOrdreAsc(anneeScolaireId);
        if (toutes.isEmpty()) {
            return toutes;
        }
        int indexFin = toutes.size() - 1; // par défaut : la dernière période
        if (periodeFinId != null) {
            for (int i = 0; i < toutes.size(); i++) {
                if (toutes.get(i).getId().equals(periodeFinId)) {
                    indexFin = i;
                    break;
                }
            }
        }
        int indexDebut = Math.max(0, indexFin - 2);
        return toutes.subList(indexDebut, indexFin + 1);
    }

    private Map<Long, Map<Integer, BigDecimal>> chargerMoyennes(List<Integer> periodeIds, List<Long> inscriptionIds) {
        List<Object[]> rows = moyenneRepo.findMoyennesGeneralesParPeriodes(periodeIds, inscriptionIds);
        Map<Long, Map<Integer, BigDecimal>> resultat = new HashMap<>();
        for (Object[] row : rows) {
            Long etudiantId   = ((Number) row[0]).longValue();
            Integer periodeId = ((Number) row[2]).intValue();
            BigDecimal valeur = row[3] != null ? new BigDecimal(row[3].toString()) : null;
            resultat.computeIfAbsent(etudiantId, k -> new HashMap<>()).put(periodeId, valeur);
        }
        return resultat;
    }

    /**
     * Coefficient de corrélation de Pearson entre taux d'absence (X) et moyenne récente (Y).
     * Ne considère que les points où les deux valeurs sont disponibles.
     * Renvoie null si moins de 2 points exploitables.
     */
    private Double calculerCorrelation(List<EleveStatPoint> points) {
        List<double[]> xy = points.stream()
                .filter(p -> p.getTauxAbsence() != null && p.getMoyenneRecente() != null)
                .map(p -> new double[]{ p.getTauxAbsence().doubleValue(), p.getMoyenneRecente().doubleValue() })
                .toList();

        int n = xy.size();
        if (n < 2) {
            return null;
        }

        double sommeX = 0, sommeY = 0;
        for (double[] p : xy) { sommeX += p[0]; sommeY += p[1]; }
        double moyenneX = sommeX / n;
        double moyenneY = sommeY / n;

        double covariance = 0, varianceX = 0, varianceY = 0;
        for (double[] p : xy) {
            double dx = p[0] - moyenneX;
            double dy = p[1] - moyenneY;
            covariance += dx * dy;
            varianceX  += dx * dx;
            varianceY  += dy * dy;
        }

        if (varianceX == 0 || varianceY == 0) {
            return null; // pas de variation => corrélation non définie
        }
        double r = covariance / Math.sqrt(varianceX * varianceY);
        return BigDecimal.valueOf(r).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private StatistiquesElevesResponse reponseVide(double seuilBaisse, double seuilAbsence) {
        StatistiquesElevesResponse response = new StatistiquesElevesResponse();
        response.setAnneeLibelle(null);
        response.setPeriodesAnalysees(List.of());
        response.setSeuilBaisseMoyenne(seuilBaisse);
        response.setSeuilTauxAbsence(seuilAbsence);
        response.setEffectifTotal(0);
        response.setNbAlerteRouge(0);
        response.setNbAlerteJaune(0);
        response.setCorrelationGlobale(null);
        response.setListeRouge(List.of());
        response.setListeJaune(List.of());
        response.setClasses(List.of());
        return response;
    }
}
