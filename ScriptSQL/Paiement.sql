-- ============================================================
-- REFONTE PAIEMENT MENSUEL
-- ============================================================

-- 1. Nouvelle table ecolage_mensuel
CREATE TABLE ecolage_mensuel (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id) ON DELETE CASCADE,
    annee_scolaire_id INT REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    montant           NUMERIC(12,2) NOT NULL,
    created_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (niveau_id, annee_scolaire_id)
);

-- 2. Modifier echeances
-- Ajouter les nouveaux champs
ALTER TABLE echeances
    ADD COLUMN inscription_id  INT REFERENCES inscriptions(id) ON DELETE CASCADE,
    ADD COLUMN mois            INT CHECK (mois BETWEEN 1 AND 12),
    ADD COLUMN annee           INT,
    ADD COLUMN montant_ecolage NUMERIC(12,2);

-- Mettre à jour la contrainte unique
-- (un seul enregistrement par mois/année/inscription)
ALTER TABLE echeances
    ADD CONSTRAINT uq_echeance_mois
    UNIQUE (inscription_id, mois, annee);

-- 3. Supprimer l'ancienne contrainte sur echeancier_id
-- (on garde la colonne pour ne pas casser les anciennes données)
-- mais on la rend nullable si elle ne l'est pas déjà
ALTER TABLE echeances
    ALTER COLUMN echeancier_id DROP NOT NULL;

ALTER TABLE echeances
    ALTER COLUMN numero_tranche DROP NOT NULL;

ALTER TABLE echeances
    ALTER COLUMN montant_attendu DROP NOT NULL;

ALTER TABLE echeances
    ALTER COLUMN date_limite DROP NOT NULL;

-- 4. Vérification
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'echeances'
ORDER BY ordinal_position;

SELECT * FROM ecolage_mensuel;