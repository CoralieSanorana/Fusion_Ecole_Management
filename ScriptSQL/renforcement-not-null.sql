-- ============================================================
-- SCRIPT DE RENFORCEMENT DES CONTRAINTES NOT NULL
-- Ecole — Base de données
--
-- Objectif : n'ajouter QUE des contraintes NOT NULL sur les colonnes
-- qui n'ont structurellement aucun sens à NULL (relations obligatoires).
-- Aucune table, colonne, type ou FK n'est modifié ici.
--
-- IMPORTANT : ALTER ... SET NOT NULL échoue si des lignes existantes
-- contiennent déjà NULL dans la colonne concernée. Exécute D'ABORD
-- la PARTIE 1 (diagnostic) pour vérifier que tu peux appliquer la
-- PARTIE 2 sans erreur. Si des NULL existent, corrige/supprime ces
-- lignes avant de lancer la PARTIE 2.
-- ============================================================


-- ============================================================
-- PARTIE 1 — DIAGNOSTIC (à exécuter et lire AVANT la partie 2)
-- Liste, pour chaque colonne ciblée, le nombre de lignes NULL.
-- Si un total > 0 apparaît, corrige les données avant de continuer.
-- ============================================================

SELECT 'classes.niveau_id'              AS colonne, COUNT(*) FROM classes              WHERE niveau_id IS NULL
UNION ALL
SELECT 'classes.annee_scolaire_id',            COUNT(*) FROM classes              WHERE annee_scolaire_id IS NULL
UNION ALL
SELECT 'classes.salle_id',                     COUNT(*) FROM classes              WHERE salle_id IS NULL
UNION ALL
SELECT 'inscriptions.etudiant_id',             COUNT(*) FROM inscriptions        WHERE etudiant_id IS NULL
UNION ALL
SELECT 'inscriptions.classe_id',               COUNT(*) FROM inscriptions        WHERE classe_id IS NULL
UNION ALL
SELECT 'inscriptions.annee_scolaire_id',       COUNT(*) FROM inscriptions        WHERE annee_scolaire_id IS NULL
UNION ALL
SELECT 'notes.etudiant_id',                    COUNT(*) FROM notes               WHERE etudiant_id IS NULL
UNION ALL
SELECT 'notes.affectation_id',                 COUNT(*) FROM notes               WHERE affectation_id IS NULL
UNION ALL
SELECT 'notes.periode_id',                     COUNT(*) FROM notes               WHERE periode_id IS NULL
UNION ALL
SELECT 'moyennes.etudiant_id',                 COUNT(*) FROM moyennes            WHERE etudiant_id IS NULL
UNION ALL
SELECT 'moyennes.inscription_id',              COUNT(*) FROM moyennes            WHERE inscription_id IS NULL
UNION ALL
SELECT 'moyennes.periode_id',                  COUNT(*) FROM moyennes            WHERE periode_id IS NULL
UNION ALL
SELECT 'moyennes.matiere_id',                  COUNT(*) FROM moyennes            WHERE matiere_id IS NULL
UNION ALL
SELECT 'absences.seance_id',                   COUNT(*) FROM absences            WHERE seance_id IS NULL
UNION ALL
SELECT 'absences.etudiant_id',                 COUNT(*) FROM absences            WHERE etudiant_id IS NULL
UNION ALL
SELECT 'affectations_enseignement.professeur_id', COUNT(*) FROM affectations_enseignement WHERE professeur_id IS NULL
UNION ALL
SELECT 'affectations_enseignement.matiere_id',    COUNT(*) FROM affectations_enseignement WHERE matiere_id IS NULL
UNION ALL
SELECT 'affectations_enseignement.classe_id',     COUNT(*) FROM affectations_enseignement WHERE classe_id IS NULL
UNION ALL
SELECT 'affectations_enseignement.annee_scolaire_id', COUNT(*) FROM affectations_enseignement WHERE annee_scolaire_id IS NULL
UNION ALL
SELECT 'emploi_du_temps.affectation_id',       COUNT(*) FROM emploi_du_temps     WHERE affectation_id IS NULL
UNION ALL
SELECT 'paiements.echeance_id',                COUNT(*) FROM paiements           WHERE echeance_id IS NULL
UNION ALL
SELECT 'paiements.inscription_id',             COUNT(*) FROM paiements           WHERE inscription_id IS NULL
UNION ALL
SELECT 'contrats_employes.user_id',            COUNT(*) FROM contrats_employes   WHERE user_id IS NULL
UNION ALL
SELECT 'depenses.etablissement_id',            COUNT(*) FROM depenses            WHERE etablissement_id IS NULL
ORDER BY 1;


-- ============================================================
-- PARTIE 2 — APPLICATION DES CONTRAINTES NOT NULL
-- Ne touche que les colonnes déjà existantes. Aucun ADD COLUMN,
-- aucun DROP, aucun changement de type.
-- ============================================================

BEGIN;

-- ---- classes : une classe doit être rattachée à un niveau et une année ----
ALTER TABLE classes ALTER COLUMN niveau_id         SET NOT NULL;
ALTER TABLE classes ALTER COLUMN annee_scolaire_id SET NOT NULL;

-- salle_id : décommente la ligne suivante SEULEMENT si une classe doit
-- obligatoirement avoir une salle dès sa création (pas de workflow
-- "brouillon" qui la complète plus tard). C'est ton cas de départ.
ALTER TABLE classes ALTER COLUMN salle_id SET NOT NULL;

-- ---- inscriptions : une inscription sans élève/classe/année n'a pas de sens ----
ALTER TABLE inscriptions ALTER COLUMN etudiant_id       SET NOT NULL;
ALTER TABLE inscriptions ALTER COLUMN classe_id         SET NOT NULL;
ALTER TABLE inscriptions ALTER COLUMN annee_scolaire_id SET NOT NULL;

-- ---- notes : une note orpheline est inexploitable pour les bulletins ----
ALTER TABLE notes ALTER COLUMN etudiant_id    SET NOT NULL;
ALTER TABLE notes ALTER COLUMN affectation_id SET NOT NULL;
ALTER TABLE notes ALTER COLUMN periode_id     SET NOT NULL;

-- ---- moyennes : idem, calcul impossible sans ces 4 relations ----
ALTER TABLE moyennes ALTER COLUMN etudiant_id    SET NOT NULL;
ALTER TABLE moyennes ALTER COLUMN inscription_id SET NOT NULL;
ALTER TABLE moyennes ALTER COLUMN periode_id     SET NOT NULL;
ALTER TABLE moyennes ALTER COLUMN matiere_id     SET NOT NULL;

-- ---- absences : une absence doit être rattachée à une séance et un élève ----
ALTER TABLE absences ALTER COLUMN seance_id   SET NOT NULL;
ALTER TABLE absences ALTER COLUMN etudiant_id SET NOT NULL;

-- ---- affectations_enseignement : une affectation vide n'a aucun sens ----
ALTER TABLE affectations_enseignement ALTER COLUMN professeur_id     SET NOT NULL;
ALTER TABLE affectations_enseignement ALTER COLUMN matiere_id        SET NOT NULL;
ALTER TABLE affectations_enseignement ALTER COLUMN classe_id         SET NOT NULL;
ALTER TABLE affectations_enseignement ALTER COLUMN annee_scolaire_id SET NOT NULL;

-- ---- emploi_du_temps : un créneau doit être rattaché à une affectation ----
ALTER TABLE emploi_du_temps ALTER COLUMN affectation_id SET NOT NULL;

-- ---- paiements : un paiement doit pouvoir être réconcilié ----
ALTER TABLE paiements ALTER COLUMN echeance_id    SET NOT NULL;
ALTER TABLE paiements ALTER COLUMN inscription_id SET NOT NULL;

-- ---- contrats_employes : un contrat sans employé lié n'a pas de sens ----
ALTER TABLE contrats_employes ALTER COLUMN user_id SET NOT NULL;

-- ---- depenses : une dépense doit être rattachée à un établissement ----
ALTER TABLE depenses ALTER COLUMN etablissement_id SET NOT NULL;

-- NOTE : depenses.categorie_id et depenses.type_depense_id ne sont PAS
-- passés en NOT NULL ici volontairement — ton schéma a deux systèmes de
-- classification en parallèle (categories_depenses vs types_depenses,
-- voir section 3.4 de l'audit) et forcer l'un des deux sans trancher
-- lequel est la référence risquerait de bloquer des insertions valides.
-- Une fois ce choix fait côté produit, ajoute la ligne correspondante ici.

COMMIT;
