-- ============================================================
-- DONNÉES DE TEST — MODULE SECRÉTARIAT
-- À exécuter APRÈS le démarrage de l'application au moins une
-- fois (spring.jpa.hibernate.ddl-auto=update crée/maj les tables),
-- ou après exécution de schema_ecole_v2.sql.
--
-- Couvre les écrans :
--   /secretariat/eleves
--   /secretariat/eleves/ajouter
--   /secretariat/profil/{id}
--   /secretariat/profil/{id}/demande-modification
--   /secretariat/profil/{id}/export.csv
--
-- Ce script est ré-exécutable : il nettoie ses propres données
-- de test avant de les recréer (cf. SECTION 0).
-- ============================================================

BEGIN;

-- ============================================================
-- SECTION -1 — MIGRATION (colonnes ajoutées à l'entité Java
-- ProfilEtudiant mais absentes de schema_ecole_v2.sql).
-- Idempotent : sans effet si déjà présentes (ex: après un
-- premier démarrage de l'appli avec ddl-auto=update).
-- ============================================================

ALTER TABLE profils_etudiants ADD COLUMN IF NOT EXISTS quartier       VARCHAR(150);
ALTER TABLE profils_etudiants ADD COLUMN IF NOT EXISTS nom_parent     VARCHAR(200);
ALTER TABLE profils_etudiants ADD COLUMN IF NOT EXISTS contact_parent VARCHAR(100);

-- ============================================================
-- SECTION 0 — NETTOYAGE (ré-exécution sûre)
-- ============================================================

DELETE FROM paiements
 WHERE inscription_id IN (
   SELECT i.id FROM inscriptions i
   JOIN profils_etudiants pe ON pe.id = i.etudiant_id
   WHERE pe.matricule LIKE 'ID-TEST-%'
 );

DELETE FROM echeances
 WHERE echeancier_id IN (
   SELECT ec.id FROM echeanciers ec
   JOIN inscriptions i ON i.id = ec.inscription_id
   JOIN profils_etudiants pe ON pe.id = i.etudiant_id
   WHERE pe.matricule LIKE 'ID-TEST-%'
 );

DELETE FROM echeanciers
 WHERE inscription_id IN (
   SELECT i.id FROM inscriptions i
   JOIN profils_etudiants pe ON pe.id = i.etudiant_id
   WHERE pe.matricule LIKE 'ID-TEST-%'
 );

DELETE FROM demandes_modification_dossier
 WHERE etudiant_id IN (SELECT id FROM profils_etudiants WHERE matricule LIKE 'ID-TEST-%');

DELETE FROM inscriptions
 WHERE etudiant_id IN (SELECT id FROM profils_etudiants WHERE matricule LIKE 'ID-TEST-%');

DELETE FROM profils_etudiants WHERE matricule LIKE 'ID-TEST-%';

DELETE FROM classes WHERE nom IN ('Seconde A', 'Première A', 'Terminale A');

DELETE FROM niveaux WHERE libelle IN ('Seconde', 'Première', 'Terminale');

DELETE FROM annees_scolaires WHERE libelle = '2025-2026';

DELETE FROM etablissements WHERE nom = 'Lycée Test';


-- ============================================================
-- SECTION 1 — ÉTABLISSEMENT & ANNÉE SCOLAIRE ACTIVE
-- ============================================================

INSERT INTO etablissements (nom, adresse, telephone, email)
VALUES ('Lycée Test', 'Lot II A 12, Antananarivo', '034 00 000 00', 'contact@lyceetest.test');

INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active)
SELECT id, '2025-2026', DATE '2025-09-01', DATE '2026-06-30', TRUE
FROM etablissements WHERE nom = 'Lycée Test';


-- ============================================================
-- SECTION 2 — NIVEAUX
-- ============================================================

INSERT INTO niveaux (etablissement_id, libelle, ordre)
SELECT id, 'Seconde', 1 FROM etablissements WHERE nom = 'Lycée Test';

INSERT INTO niveaux (etablissement_id, libelle, ordre)
SELECT id, 'Première', 2 FROM etablissements WHERE nom = 'Lycée Test';

INSERT INTO niveaux (etablissement_id, libelle, ordre)
SELECT id, 'Terminale', 3 FROM etablissements WHERE nom = 'Lycée Test';


-- ============================================================
-- SECTION 3 — CLASSES (rattachées à l'année active)
-- ============================================================

INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max)
SELECT n.id, a.id, 'Seconde A', 40
FROM niveaux n, annees_scolaires a
WHERE n.libelle = 'Seconde' AND a.libelle = '2025-2026';

INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max)
SELECT n.id, a.id, 'Première A', 40
FROM niveaux n, annees_scolaires a
WHERE n.libelle = 'Première' AND a.libelle = '2025-2026';

INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max)
SELECT n.id, a.id, 'Terminale A', 40
FROM niveaux n, annees_scolaires a
WHERE n.libelle = 'Terminale' AND a.libelle = '2025-2026';


-- ============================================================
-- SECTION 4 — ÉLÈVES (profils_etudiants)
-- ============================================================

INSERT INTO profils_etudiants
  (matricule, nom, prenom, date_naissance, quartier, adresse, nom_parent, contact_parent, is_archived)
VALUES
  ('ID-TEST-0001', 'Rakoto', 'Jean',    DATE '2009-03-12', 'Tsaralalàna', 'Lot 24, Tsaralalàna',      'Rakoto Pierre',  '034 56 789 01', FALSE),
  ('ID-TEST-0002', 'Rabe',   'Marie',   DATE '2008-11-05', 'Analakely',   'Lot 7, Analakely',         'Rabe Hanta',     '033 12 345 67', FALSE),
  ('ID-TEST-0003', 'Randria','Hery',    DATE '2007-07-21', 'Ankorondrano','Lot 3B, Ankorondrano',     'Randria Tojo',   '032 98 765 43', FALSE),
  ('ID-TEST-0004', 'Andry',  'Lalaina', DATE '2009-01-30', 'Isotry',      'Lot 15, Isotry',           'Andry Voahangy', '034 11 222 33', FALSE),
  ('ID-TEST-0005', 'Ravelo', 'Tojo',    DATE '2008-05-18', 'Ambohipo',    'Lot 9, Ambohipo',          'Ravelo Faly',    '033 44 555 66', FALSE),
  ('ID-TEST-0006', 'Rasoa',  'Nirina',  DATE '2007-09-09', 'Ivandry',     'Lot 21, Ivandry',          'Rasoa Voahirana','032 77 888 99', FALSE);


-- ============================================================
-- SECTION 5 — INSCRIPTIONS (année active, statut 'active')
--   2 élèves en Seconde A, 2 en Première A, 2 en Terminale A
-- ============================================================

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'nouvelle', DATE '2025-08-28', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0001' AND c.nom = 'Seconde A' AND a.libelle = '2025-2026';

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'nouvelle', DATE '2025-08-28', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0002' AND c.nom = 'Seconde A' AND a.libelle = '2025-2026';

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'reinscription', DATE '2025-08-29', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0003' AND c.nom = 'Première A' AND a.libelle = '2025-2026';

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'reinscription', DATE '2025-08-29', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0004' AND c.nom = 'Première A' AND a.libelle = '2025-2026';

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'reinscription', DATE '2025-08-30', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0005' AND c.nom = 'Terminale A' AND a.libelle = '2025-2026';

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT pe.id, c.id, a.id, 'reinscription', DATE '2025-08-30', 'active'
FROM profils_etudiants pe, classes c, annees_scolaires a
WHERE pe.matricule = 'ID-TEST-0006' AND c.nom = 'Terminale A' AND a.libelle = '2025-2026';


-- ============================================================
-- SECTION 6 — ÉCHÉANCIERS (un par inscription, mensuel)
-- ============================================================

INSERT INTO echeanciers (inscription_id, type, montant_total)
SELECT i.id, 'echelonne_10', 800000
FROM inscriptions i
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%';


-- ============================================================
-- SECTION 7 — ÉCHÉANCES (4 mois : Sept., Oct., Nov., Déc.)
--   -> alimentent les colonnes "mois" de /secretariat/eleves
--   -> alimentent l'historique de paiement de /secretariat/profil/{id}
-- ============================================================

-- Septembre 2025 : tous payés
INSERT INTO echeances (echeancier_id, numero_tranche, montant_attendu, date_limite, est_soldee)
SELECT ec.id, 1, 80000, DATE '2025-09-05', TRUE
FROM echeanciers ec
JOIN inscriptions i ON i.id = ec.inscription_id
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%';

-- Octobre 2025 : tous payés
INSERT INTO echeances (echeancier_id, numero_tranche, montant_attendu, date_limite, est_soldee)
SELECT ec.id, 2, 80000, DATE '2025-10-05', TRUE
FROM echeanciers ec
JOIN inscriptions i ON i.id = ec.inscription_id
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%';

-- Novembre 2025 : payé sauf pour 0003 et 0006 (retard)
INSERT INTO echeances (echeancier_id, numero_tranche, montant_attendu, date_limite, est_soldee)
SELECT ec.id, 3, 80000, DATE '2025-11-05', (pe.matricule NOT IN ('ID-TEST-0003', 'ID-TEST-0006'))
FROM echeanciers ec
JOIN inscriptions i ON i.id = ec.inscription_id
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%';

-- Décembre 2025 : pas encore payé pour personne (échéance future / en attente)
INSERT INTO echeances (echeancier_id, numero_tranche, montant_attendu, date_limite, est_soldee)
SELECT ec.id, 4, 80000, DATE '2025-12-05', FALSE
FROM echeanciers ec
JOIN inscriptions i ON i.id = ec.inscription_id
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%';


-- ============================================================
-- SECTION 8 — PAIEMENTS (versements réels pour les échéances soldées)
--   -> alimentent la colonne "Date de paiement" du profil élève
-- ============================================================

INSERT INTO paiements (echeance_id, inscription_id, montant, date_paiement, mode_paiement, reference_transaction, notes)
SELECT ech.id, ech_i.inscription_id, ech.montant_attendu, ech.date_limite - INTERVAL '2 day', 'especes', 'REC-' || ech.id, 'Paiement de test'
FROM echeances ech
JOIN echeanciers ech_i ON ech_i.id = ech.echeancier_id
JOIN inscriptions i ON i.id = ech_i.inscription_id
JOIN profils_etudiants pe ON pe.id = i.etudiant_id
WHERE pe.matricule LIKE 'ID-TEST-%'
  AND ech.est_soldee = TRUE;


COMMIT;

-- ============================================================
-- VÉRIFICATION RAPIDE
-- ============================================================
-- SELECT pe.matricule, pe.nom, pe.prenom, c.nom AS classe, a.libelle AS annee
-- FROM profils_etudiants pe
-- JOIN inscriptions i ON i.etudiant_id = pe.id AND i.statut = 'active'
-- JOIN classes c ON c.id = i.classe_id
-- JOIN annees_scolaires a ON a.id = i.annee_scolaire_id
-- WHERE pe.matricule LIKE 'ID-TEST-%'
-- ORDER BY pe.nom;