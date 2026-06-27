-- ============================================================
--  MIGRATION PHOTO_URL VERS LES PROFILS
--  - ajoute photo_url aux profils manquants
--  - retire photo_url de contrats_employes
--  - met à jour vue_employes_detail pour lire la photo depuis les profils
-- ============================================================

BEGIN;
drop view if exists vue_employes_detail;

ALTER TABLE profils_professeurs
	ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

ALTER TABLE profils_secretariat
	ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

ALTER TABLE profils_directeurs
	ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

ALTER TABLE profils_comptables
	ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

ALTER TABLE contrats_employes
	DROP COLUMN IF EXISTS photo_url;

CREATE OR REPLACE VIEW vue_employes_detail AS
SELECT
	u.id AS user_id,
	u.email,
	u.is_active,
	r.nom AS role_nom,
	COALESCE(pp.nom, pd.nom, ps.nom, pc.nom) AS nom,
	COALESCE(pp.prenom, pd.prenom, ps.prenom, pc.prenom) AS prenom,
	ce.id AS contrat_id,
	ce.reference_contrat,
	ce.fonction,
	COALESCE(ce.sexe, pp.sexe, ps.sexe, pd.sexe, pc.sexe, 'H') AS sexe,
	COALESCE(pp.photo_url, pd.photo_url, ps.photo_url, pc.photo_url) AS photo_url,
	tce.code AS type_contrat_code,
	tce.libelle AS type_contrat_libelle,
	ce.date_debut,
	ce.date_fin,
	ce.document_url,
	ce.salaire_mensuel,
	ce.heures_hebdo,
	pp.id_matiere,
	m.nom AS matiere_nom,
	CASE
		WHEN ce.date_fin IS NULL THEN NULL
		ELSE GREATEST(ce.date_fin - CURRENT_DATE, 0)
	END AS jours_restants
FROM users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id
LEFT JOIN profils_professeurs pp ON pp.user_id = u.id
LEFT JOIN profils_directeurs pd ON pd.user_id = u.id
LEFT JOIN profils_secretariat ps ON ps.user_id = u.id
LEFT JOIN profils_comptables pc ON pc.user_id = u.id
LEFT JOIN contrats_employes ce ON ce.id = COALESCE(pp.id_contrat, pd.id_contrat, ps.id_contrat, pc.id_contrat)
LEFT JOIN types_contrats_employes tce ON tce.id = ce.type_contrat_id
LEFT JOIN matieres m ON m.id = pp.id_matiere
WHERE r.nom NOT IN ('etudiant', 'parent');

COMMIT;
