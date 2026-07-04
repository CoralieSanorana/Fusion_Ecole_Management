BEGIN;

CREATE TABLE IF NOT EXISTS types_depenses (
	id          SERIAL PRIMARY KEY,
	libelle     VARCHAR(120) NOT NULL UNIQUE,
	description TEXT,
	created_at  TIMESTAMP DEFAULT NOW(),
	updated_at  TIMESTAMP DEFAULT NOW()
);

ALTER TABLE depenses
	ADD COLUMN IF NOT EXISTS type_depense_id INT,
	ADD COLUMN IF NOT EXISTS description TEXT,
	ADD COLUMN IF NOT EXISTS prix_unitaire NUMERIC(15,2),
	ADD COLUMN IF NOT EXISTS quantite NUMERIC(15,2),
	ADD COLUMN IF NOT EXISTS total NUMERIC(15,2),
	ADD COLUMN IF NOT EXISTS saisi_par INT;

ALTER TABLE depenses
	ALTER COLUMN prix_unitaire SET DEFAULT 0,
	ALTER COLUMN quantite SET DEFAULT 1,
	ALTER COLUMN total SET DEFAULT 0;

DO $$
BEGIN
	IF NOT EXISTS (
		SELECT 1
		FROM pg_constraint
		WHERE conname = 'fk_depenses_type_depense'
	) THEN
		ALTER TABLE depenses
			ADD CONSTRAINT fk_depenses_type_depense
			FOREIGN KEY (type_depense_id) REFERENCES types_depenses(id) ON DELETE RESTRICT;
	END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_types_depenses_libelle ON types_depenses(libelle);
CREATE INDEX IF NOT EXISTS idx_depenses_annee_date ON depenses(annee_scolaire_id, date_depense);
CREATE INDEX IF NOT EXISTS idx_depenses_type ON depenses(type_depense_id);
CREATE INDEX IF NOT EXISTS idx_depenses_saisi_par ON depenses(saisi_par);

INSERT INTO types_depenses (libelle, description) VALUES
	('Dépense générale', 'Type de secours pour les dépenses déjà présentes en base'),
	('Salaires professeurs', 'Rémunération mensuelle des professeurs'),
	('Chaises', 'Achat ou remplacement des chaises'),
	('Craies', 'Consommables de classe'),
	('Rénovation', 'Travaux de rénovation et d''entretien'),
	('Création de nouvelles salles', 'Matériaux et main d''œuvre pour nouvelles salles'),
	('Matériel pédagogique', 'Supports, fournitures et matériel éducatif')
ON CONFLICT (libelle) DO NOTHING;

UPDATE depenses d
SET
	description = COALESCE(NULLIF(d.description, ''), d.motif, d.intitule),
	prix_unitaire = COALESCE(d.prix_unitaire, d.montant, 0),
	quantite = COALESCE(d.quantite, 1),
	total = COALESCE(d.total, d.montant, COALESCE(d.prix_unitaire, d.montant, 0) * COALESCE(d.quantite, 1)),
	type_depense_id = COALESCE(
		d.type_depense_id,
		(
			SELECT td.id
			FROM types_depenses td
			WHERE td.libelle = 'Dépense générale'
			LIMIT 1
		)
	)
WHERE d.type_depense_id IS NULL
	OR d.description IS NULL
	OR d.prix_unitaire IS NULL
	OR d.quantite IS NULL
	OR d.total IS NULL;

ALTER TABLE depenses
	ALTER COLUMN type_depense_id SET NOT NULL,
	ALTER COLUMN description SET NOT NULL,
	ALTER COLUMN prix_unitaire SET NOT NULL,
	ALTER COLUMN quantite SET NOT NULL,
	ALTER COLUMN total SET NOT NULL;

COMMIT;
