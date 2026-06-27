-- ============================================================
--  SEED DONNÉES TEST - MATIÈRES ET SALLES
--  À exécuter après le script de base
-- ============================================================

BEGIN;

WITH fallback_etab AS (
	INSERT INTO etablissements (nom, adresse, telephone, email)
	SELECT 'Ecole de gestion', 'Antananarivo', NULL, NULL
	WHERE NOT EXISTS (
		SELECT 1
		FROM etablissements
	)
	RETURNING id
)
SELECT 1;

WITH etab AS (
	SELECT id AS etablissement_id
	FROM etablissements
	ORDER BY id
	LIMIT 1
)
INSERT INTO matieres (etablissement_id, nom, code)
SELECT etab.etablissement_id, v.nom, v.code
FROM etab
CROSS JOIN (
	VALUES
		('Mathématiques', 'MATH'),
		('Français', 'FRAN'),
		('Physique-Chimie', 'PC'),
		('Histoire-Géographie', 'HIST'),
		('SVT', 'SVT'),
		('Anglais', 'ANG')
) AS v(nom, code)
ON CONFLICT DO NOTHING;

WITH etab AS (
	SELECT id AS etablissement_id
	FROM etablissements
	ORDER BY id
	LIMIT 1
)
INSERT INTO salles (etablissement_id, nom, capacite, type, is_active)
SELECT etab.etablissement_id, v.nom, v.capacite, v.type, TRUE
FROM etab
CROSS JOIN (
	VALUES
		('Salle 1', 40, 'cours'),
		('Salle 2', 40, 'cours'),
		('Salle 3', 35, 'cours'),
		('Labo Sciences', 30, 'laboratoire'),
		('Salle Informatique', 25, 'laboratoire'),
		('Salle Polyvalente', 100, 'amphi')
) AS v(nom, capacite, type)
ON CONFLICT DO NOTHING;

COMMIT;
