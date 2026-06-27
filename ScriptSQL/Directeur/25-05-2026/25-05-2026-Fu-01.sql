-- ============================================================
--  EXTENSION SCHEMA (SANS DONNÉES DE SEED)
--  Contrats employés + vues + normalisation des profils
-- ============================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS types_contrats_employes (
	id          SERIAL PRIMARY KEY,
	code        VARCHAR(50) UNIQUE NOT NULL,
	libelle     VARCHAR(150) NOT NULL,
	duree_mois  INT,
	description TEXT,
	est_actif   BOOLEAN DEFAULT TRUE,
	created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS contrats_employes (
	id                SERIAL PRIMARY KEY,
	user_id           INT REFERENCES users(id) ON DELETE CASCADE,
	fonction          VARCHAR(50) NOT NULL,
	type_contrat_id   INT REFERENCES types_contrats_employes(id),
	sexe              CHAR(1) CHECK (sexe IN ('H', 'F')),
	photo_url         VARCHAR(500),
	reference_contrat VARCHAR(150) UNIQUE,
	date_debut        DATE NOT NULL,
	date_fin          DATE,
	salaire_mensuel   NUMERIC(12,2) DEFAULT 0,
	heures_hebdo      NUMERIC(5,1) DEFAULT 0,
	statut            VARCHAR(50) DEFAULT 'actif',
	document_url      VARCHAR(500),
	created_at        TIMESTAMP DEFAULT NOW(),
	updated_at        TIMESTAMP DEFAULT NOW()
);

ALTER TABLE profils_professeurs
	ADD COLUMN IF NOT EXISTS id_contrat INT,
	ADD COLUMN IF NOT EXISTS id_matiere INT;

ALTER TABLE profils_secretariat
	ADD COLUMN IF NOT EXISTS id_contrat INT,
	ADD COLUMN IF NOT EXISTS sexe CHAR(1);

ALTER TABLE profils_directeurs
	ADD COLUMN IF NOT EXISTS id_contrat INT,
	ADD COLUMN IF NOT EXISTS sexe CHAR(1);

ALTER TABLE profils_comptables
	ADD COLUMN IF NOT EXISTS id_contrat INT,
	ADD COLUMN IF NOT EXISTS sexe CHAR(1);

ALTER TABLE contrats_employes
	ADD COLUMN IF NOT EXISTS sexe CHAR(1),
	ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

DO $$
BEGIN
	IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_profils_professeurs_id_contrat') THEN
		ALTER TABLE profils_professeurs
			ADD CONSTRAINT fk_profils_professeurs_id_contrat
			FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_profils_professeurs_id_matiere') THEN
		ALTER TABLE profils_professeurs
			ADD CONSTRAINT fk_profils_professeurs_id_matiere
			FOREIGN KEY (id_matiere) REFERENCES matieres(id) ON DELETE SET NULL;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_profils_secretariat_id_contrat') THEN
		ALTER TABLE profils_secretariat
			ADD CONSTRAINT fk_profils_secretariat_id_contrat
			FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_profils_directeurs_id_contrat') THEN
		ALTER TABLE profils_directeurs
			ADD CONSTRAINT fk_profils_directeurs_id_contrat
			FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_profils_comptables_id_contrat') THEN
		ALTER TABLE profils_comptables
			ADD CONSTRAINT fk_profils_comptables_id_contrat
			FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;
	END IF;
END $$;

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
	COALESCE(ce.photo_url, pp.photo_url) AS photo_url,
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

-- Harmonisation des valeurs de sexe vers H/F
UPDATE profils_professeurs
SET sexe = CASE
	WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
	ELSE 'H'
END;

UPDATE profils_secretariat
SET sexe = CASE
	WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
	ELSE 'H'
END;

UPDATE profils_directeurs
SET sexe = CASE
	WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
	ELSE 'H'
END;

UPDATE profils_comptables
SET sexe = CASE
	WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
	ELSE 'H'
END;

UPDATE contrats_employes ce
SET sexe = CASE
	WHEN UPPER(COALESCE(ce.sexe, '')) = 'F' THEN 'F'
	WHEN UPPER(COALESCE(ce.sexe, '')) IN ('M', 'H') THEN 'H'
	ELSE COALESCE((
		SELECT CASE WHEN UPPER(pp.sexe) = 'F' THEN 'F' ELSE 'H' END
		FROM profils_professeurs pp
		WHERE pp.user_id = ce.user_id
		LIMIT 1
	), 'H')
END;

DO $$
BEGIN
	IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'contrats_employes_sexe_check') THEN
		ALTER TABLE contrats_employes DROP CONSTRAINT contrats_employes_sexe_check;
	END IF;

	IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_contrats_employes_sexe_hf') THEN
		ALTER TABLE contrats_employes DROP CONSTRAINT ck_contrats_employes_sexe_hf;
	END IF;

	ALTER TABLE contrats_employes
		ADD CONSTRAINT ck_contrats_employes_sexe_hf CHECK (sexe IN ('H', 'F'));
END $$;

-- Liaison automatique du contrat le plus récent si id_contrat est vide
UPDATE profils_professeurs p
SET id_contrat = (
		SELECT ce.id
		FROM contrats_employes ce
		WHERE ce.user_id = p.user_id
		ORDER BY ce.id DESC
		LIMIT 1
	),
	id_matiere = COALESCE(p.id_matiere, (
		SELECT m.id
		FROM matieres m
		WHERE m.nom = p.specialite
		LIMIT 1
	))
WHERE p.id_contrat IS NULL;

UPDATE profils_secretariat p
SET id_contrat = (
		SELECT ce.id
		FROM contrats_employes ce
		WHERE ce.user_id = p.user_id
		ORDER BY ce.id DESC
		LIMIT 1
	)
WHERE p.id_contrat IS NULL;

UPDATE profils_directeurs p
SET id_contrat = (
		SELECT ce.id
		FROM contrats_employes ce
		WHERE ce.user_id = p.user_id
		ORDER BY ce.id DESC
		LIMIT 1
	)
WHERE p.id_contrat IS NULL;

UPDATE profils_comptables p
SET id_contrat = (
		SELECT ce.id
		FROM contrats_employes ce
		WHERE ce.user_id = p.user_id
		ORDER BY ce.id DESC
		LIMIT 1
	)
WHERE p.id_contrat IS NULL;

INSERT INTO types_contrats_employes (code, libelle, duree_mois, description)
VALUES
	('permanent', 'Permanent', NULL, 'Contrat sans échéance fixe'),
	('vacataire', 'Vacataire', 12, 'Contrat à durée limitée pour heures ponctuelles'),
	('contractuel', 'Contractuel', 12, 'Contrat à durée déterminée renouvelable')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (nom, description) VALUES
	('professeur',  'Saisie notes, absences, emploi du temps'),
	('secretariat', 'Inscriptions, dossiers, finance opérationnelle')
ON CONFLICT (nom) DO NOTHING;

COMMIT;
