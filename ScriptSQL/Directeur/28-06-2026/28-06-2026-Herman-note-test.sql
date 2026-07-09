-- =============================================================
-- statistiques_eleves_test.sql
-- Données de test pour le module "Statistiques Élèves" (détection décrochage).
--
-- Prérequis (déjà créés normalement via l'application) :
--   etablissement id=1, niveau id=1, classe id=1, une annee_scolaire
--   marquée est_active = TRUE.
--   Les 25 étudiants + inscriptions du fichier
--   28-06-2026-Herman-etudiants_test.sql doivent être exécutés AVANT
--   ce script (classe_id=1, même année scolaire active).
--
-- Ce script crée : 3 périodes, 3 matières, 1 professeur, ses
-- affectations + emploi du temps, un an de séances, des absences
-- différenciées par profil d'élève, et les moyennes générales par
-- période, afin d'obtenir des cas Rouge / Jaune / Normal réalistes.
--
-- Répartition des 25 étudiants (ETU20240001 → ETU20240025) :
--   0001-0005 : ROUGE  (grosse baisse de moyenne ET absentéisme élevé)
--   0006-0010 : JAUNE  (absentéisme élevé, moyenne stable)
--   0011-0015 : JAUNE  (grosse baisse de moyenne, peu d'absences)
--   0016-0025 : NORMAL (stable, peu d'absences)
-- =============================================================

BEGIN;

DO $$
DECLARE
    v_etab_id      INT := 1;
    v_niveau_id    INT := 1;
    v_classe_id    INT := 1;
    v_annee_id     INT;
    v_annee_debut  DATE;
    v_annee_fin    DATE;
    v_len_jours    INT;

    v_p1_id INT; v_p1_debut DATE; v_p1_fin DATE;
    v_p2_id INT; v_p2_debut DATE; v_p2_fin DATE;
    v_p3_id INT; v_p3_debut DATE; v_p3_fin DATE;

    v_prof_user_id INT;
    v_prof_id      INT;

    v_mat_math_id INT;
    v_mat_fran_id INT;
    v_mat_svt_id  INT;

    v_aff_math_id INT;
    v_aff_fran_id INT;
    v_aff_svt_id  INT;

    v_edt_math_id INT;
    v_edt_fran_id INT;
    v_edt_svt_id  INT;

    rec        RECORD;
    v_rank     INT;
    v_total_seances INT;
    v_nb_absences   INT;
    v_moy_p1 NUMERIC; v_moy_p2 NUMERIC; v_moy_p3 NUMERIC;
BEGIN

    -- ── 1) Résoudre l'année scolaire active ─────────────────────────
    SELECT id, date_debut, date_fin INTO v_annee_id, v_annee_debut, v_annee_fin
    FROM annees_scolaires
    WHERE est_active = TRUE
    LIMIT 1;

    IF v_annee_id IS NULL THEN
        RAISE EXCEPTION 'Aucune année scolaire active trouvée. Créez-en une via l''application avant de lancer ce script.';
    END IF;

    v_len_jours := (v_annee_fin - v_annee_debut) / 3;

    -- ── 2) Créer les 3 périodes (si pas déjà présentes) ─────────────
    v_p1_debut := v_annee_debut;
    v_p1_fin   := v_annee_debut + v_len_jours;
    v_p2_debut := v_p1_fin + 1;
    v_p2_fin   := v_p2_debut + v_len_jours;
    v_p3_debut := v_p2_fin + 1;
    v_p3_fin   := v_annee_fin;

    SELECT id INTO v_p1_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 1;
    IF v_p1_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '1er Trimestre', 'trimestre', 1, v_p1_debut, v_p1_fin, v_p1_fin, true)
        RETURNING id INTO v_p1_id;
    END IF;

    SELECT id INTO v_p2_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 2;
    IF v_p2_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '2ème Trimestre', 'trimestre', 2, v_p2_debut, v_p2_fin, v_p2_fin, true)
        RETURNING id INTO v_p2_id;
    END IF;

    SELECT id INTO v_p3_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 3;
    IF v_p3_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '3ème Trimestre', 'trimestre', 3, v_p3_debut, v_p3_fin, v_p3_fin, false)
        RETURNING id INTO v_p3_id;
    END IF;

    -- Toujours relire les dates réelles (au cas où les périodes existaient déjà avant ce script)
    SELECT date_debut, date_fin INTO v_p1_debut, v_p1_fin FROM periodes WHERE id = v_p1_id;
    SELECT date_debut, date_fin INTO v_p2_debut, v_p2_fin FROM periodes WHERE id = v_p2_id;
    SELECT date_debut, date_fin INTO v_p3_debut, v_p3_fin FROM periodes WHERE id = v_p3_id;

    -- ── 3) Matières de test ──────────────────────────────────────────
    SELECT id INTO v_mat_math_id FROM matieres WHERE etablissement_id = v_etab_id AND code = 'MATH';
    IF v_mat_math_id IS NULL THEN
        INSERT INTO matieres (etablissement_id, nom, code) VALUES (v_etab_id, 'Mathématiques', 'MATH')
        RETURNING id INTO v_mat_math_id;
    END IF;

    SELECT id INTO v_mat_fran_id FROM matieres WHERE etablissement_id = v_etab_id AND code = 'FRAN';
    IF v_mat_fran_id IS NULL THEN
        INSERT INTO matieres (etablissement_id, nom, code) VALUES (v_etab_id, 'Français', 'FRAN')
        RETURNING id INTO v_mat_fran_id;
    END IF;

    SELECT id INTO v_mat_svt_id FROM matieres WHERE etablissement_id = v_etab_id AND code = 'SVT';
    IF v_mat_svt_id IS NULL THEN
        INSERT INTO matieres (etablissement_id, nom, code) VALUES (v_etab_id, 'Sciences de la Vie et de la Terre', 'SVT')
        RETURNING id INTO v_mat_svt_id;
    END IF;

    -- ── 4) Professeur de test ────────────────────────────────────────
    SELECT id INTO v_prof_user_id FROM users WHERE email = 'prof.test.decrochage@ecole.mg';
    IF v_prof_user_id IS NULL THEN
        INSERT INTO users (email, password, is_active) VALUES ('prof.test.decrochage@ecole.mg', 'prof123', true)
        RETURNING id INTO v_prof_user_id;
    END IF;

    SELECT id INTO v_prof_id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR';
    IF v_prof_id IS NULL THEN
        INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, sexe, specialite, type_contrat, is_archived)
        VALUES (v_prof_user_id, 'PROF_TEST_DECR', 'RAZAFY', 'Michel', 'H', 'Polyvalent', 'permanent', false)
        RETURNING id INTO v_prof_id;
    END IF;

    -- ── 5) Affectations d'enseignement (une par matière, sur la classe 1) ─
    SELECT id INTO v_aff_math_id FROM affectations_enseignement
        WHERE matiere_id = v_mat_math_id AND classe_id = v_classe_id AND annee_scolaire_id = v_annee_id;
    IF v_aff_math_id IS NULL THEN
        INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo)
        VALUES (v_prof_id, v_mat_math_id, v_classe_id, v_annee_id, 2)
        RETURNING id INTO v_aff_math_id;
    END IF;

    SELECT id INTO v_aff_fran_id FROM affectations_enseignement
        WHERE matiere_id = v_mat_fran_id AND classe_id = v_classe_id AND annee_scolaire_id = v_annee_id;
    IF v_aff_fran_id IS NULL THEN
        INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo)
        VALUES (v_prof_id, v_mat_fran_id, v_classe_id, v_annee_id, 2)
        RETURNING id INTO v_aff_fran_id;
    END IF;

    SELECT id INTO v_aff_svt_id FROM affectations_enseignement
        WHERE matiere_id = v_mat_svt_id AND classe_id = v_classe_id AND annee_scolaire_id = v_annee_id;
    IF v_aff_svt_id IS NULL THEN
        INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo)
        VALUES (v_prof_id, v_mat_svt_id, v_classe_id, v_annee_id, 2)
        RETURNING id INTO v_aff_svt_id;
    END IF;

    -- ── 6) Emploi du temps : 1 créneau hebdo par matière ──────────────
    SELECT id INTO v_edt_math_id FROM emploi_du_temps WHERE affectation_id = v_aff_math_id;
    IF v_edt_math_id IS NULL THEN
        INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin)
        VALUES (v_aff_math_id, NULL, 1, '08:00', '10:00') -- Lundi
        RETURNING id INTO v_edt_math_id;
    END IF;

    SELECT id INTO v_edt_fran_id FROM emploi_du_temps WHERE affectation_id = v_aff_fran_id;
    IF v_edt_fran_id IS NULL THEN
        INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin)
        VALUES (v_aff_fran_id, NULL, 2, '08:00', '10:00') -- Mardi
        RETURNING id INTO v_edt_fran_id;
    END IF;

    SELECT id INTO v_edt_svt_id FROM emploi_du_temps WHERE affectation_id = v_aff_svt_id;
    IF v_edt_svt_id IS NULL THEN
        INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin)
        VALUES (v_aff_svt_id, NULL, 3, '08:00', '10:00') -- Mercredi
        RETURNING id INTO v_edt_svt_id;
    END IF;

    -- ── 7) Génération des séances (1 par semaine et par matière, sur les 3 périodes) ─
    INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
    SELECT v_edt_math_id, d::date, '08:00', '10:00', true
    FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
    WHERE NOT EXISTS (
        SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_math_id AND date_seance = d::date
    );

    INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
    SELECT v_edt_fran_id, d::date, '08:00', '10:00', true
    FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
    WHERE NOT EXISTS (
        SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_fran_id AND date_seance = d::date
    );

    INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
    SELECT v_edt_svt_id, d::date, '08:00', '10:00', true
    FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
    WHERE NOT EXISTS (
        SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_svt_id AND date_seance = d::date
    );

    SELECT COUNT(*) INTO v_total_seances
    FROM seances
    WHERE emploi_du_temps_id IN (v_edt_math_id, v_edt_fran_id, v_edt_svt_id);

    RAISE NOTICE 'Total séances générées pour la classe % : %', v_classe_id, v_total_seances;

    -- ── 8) Moyennes générales + absences, par profil d'élève ──────────
    -- Rang 1..25 dans l'ordre du matricule (ETU20240001 → ETU20240025)
    v_rank := 0;
    FOR rec IN
        SELECT pe.id AS etudiant_id, i.id AS inscription_id, pe.matricule
        FROM profils_etudiants pe
        JOIN inscriptions i ON i.etudiant_id = pe.id AND i.annee_scolaire_id = v_annee_id
        WHERE pe.matricule BETWEEN 'ETU20240001' AND 'ETU20240025'
        ORDER BY pe.matricule
    LOOP
        v_rank := v_rank + 1;

        IF v_rank BETWEEN 1 AND 5 THEN
            -- ROUGE : grosse baisse (≥2 pts) ET absentéisme élevé (≥15%)
            v_moy_p1 := 14.5 - (v_rank * 0.2);
            v_moy_p2 := v_moy_p1 - 1.8;
            v_moy_p3 := v_moy_p2 - 1.7;              -- delta total ≈ 3.5 pts
            v_nb_absences := round(v_total_seances * (0.18 + (v_rank * 0.02)));  -- ~18-26%

        ELSIF v_rank BETWEEN 6 AND 10 THEN
            -- JAUNE (absence seule) : absentéisme élevé, moyenne stable
            v_moy_p1 := 12.0 + (v_rank * 0.1);
            v_moy_p2 := v_moy_p1 - 0.3;
            v_moy_p3 := v_moy_p2 - 0.3;               -- delta total < 1 pt
            v_nb_absences := round(v_total_seances * (0.16 + ((v_rank - 5) * 0.015))); -- ~16-22%

        ELSIF v_rank BETWEEN 11 AND 15 THEN
            -- JAUNE (moyenne seule) : grosse baisse, peu d'absences
            v_moy_p1 := 15.5 - ((v_rank - 10) * 0.1);
            v_moy_p2 := v_moy_p1 - 1.4;
            v_moy_p3 := v_moy_p2 - 1.4;               -- delta total ≈ 2.8 pts
            v_nb_absences := round(v_total_seances * (0.03 + ((v_rank - 10) * 0.005))); -- ~3-6%

        ELSE
            -- NORMAL : stable, peu d'absences
            v_moy_p1 := 11.0 + ((v_rank - 15) * 0.3);
            v_moy_p2 := v_moy_p1 + 0.2;
            v_moy_p3 := v_moy_p2 + 0.3;                -- progression positive
            v_nb_absences := round(v_total_seances * (0.02 + ((v_rank - 15) * 0.004))); -- ~2-6%
        END IF;

        -- Bornes de sécurité /20
        v_moy_p1 := GREATEST(LEAST(v_moy_p1, 20), 0);
        v_moy_p2 := GREATEST(LEAST(v_moy_p2, 20), 0);
        v_moy_p3 := GREATEST(LEAST(v_moy_p3, 20), 0);
        v_nb_absences := GREATEST(LEAST(v_nb_absences, v_total_seances), 0);

        -- Moyennes générales (matiere_id NULL) pour les 3 périodes
        INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, effectif_classe)
        VALUES
            (rec.etudiant_id, rec.inscription_id, v_p1_id, NULL, ROUND(v_moy_p1, 2), 25),
            (rec.etudiant_id, rec.inscription_id, v_p2_id, NULL, ROUND(v_moy_p2, 2), 25),
            (rec.etudiant_id, rec.inscription_id, v_p3_id, NULL, ROUND(v_moy_p3, 2), 25)
        ON CONFLICT (etudiant_id, inscription_id, periode_id, matiere_id)
        DO UPDATE SET valeur = EXCLUDED.valeur, effectif_classe = EXCLUDED.effectif_classe, calculated_at = NOW();

        -- Absences : on prend les N premières séances (chronologiquement) non déjà marquées pour cet élève
        INSERT INTO absences (seance_id, etudiant_id, type, saisi_par)
        SELECT s.id, rec.etudiant_id, 'non_justifiee', v_prof_user_id
        FROM seances s
        WHERE s.emploi_du_temps_id IN (v_edt_math_id, v_edt_fran_id, v_edt_svt_id)
          AND NOT EXISTS (
              SELECT 1 FROM absences a WHERE a.seance_id = s.id AND a.etudiant_id = rec.etudiant_id
          )
        ORDER BY s.date_seance, s.id
        LIMIT v_nb_absences
        ON CONFLICT (seance_id, etudiant_id) DO NOTHING;

    END LOOP;

    RAISE NOTICE 'Données de test "Statistiques Élèves" générées avec succès pour l''année scolaire id=%.', v_annee_id;

END $$;

COMMIT;