-- ============================================================
--  MIGRATION DIRECTEUR — HORAIRES EDT PAR NIVEAU
--  - Permet d'avoir des plages horaires personnalisées par niveau
--  - Gère les contraintes uniques par niveau et globales
-- ============================================================

BEGIN;

-- 1. Suppression dynamique de la contrainte unique globale sur (heure_debut, heure_fin) de la table horaire_edt
DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT tc.constraint_name 
    INTO constraint_name
    FROM information_schema.table_constraints tc 
    JOIN information_schema.key_column_usage kcu 
      ON tc.constraint_name = kcu.constraint_name 
      AND tc.table_schema = kcu.table_schema
    WHERE tc.table_name = 'horaire_edt' 
      AND tc.constraint_type = 'UNIQUE'
      AND kcu.column_name IN ('heure_debut', 'heure_fin')
    GROUP BY tc.constraint_name
    HAVING COUNT(DISTINCT kcu.column_name) = 2;

    IF constraint_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE horaire_edt DROP CONSTRAINT ' || quote_ident(constraint_name);
    END IF;
END $$;

-- 2. Ajout de la colonne niveau_id si elle n'existe pas déjà
ALTER TABLE horaire_edt ADD COLUMN IF NOT EXISTS niveau_id INT;

-- 3. Ajout de la contrainte de clé étrangère vers niveaux
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'horaire_edt'
          AND tc.constraint_name = 'fk_horaire_edt_niveau'
    ) THEN
        ALTER TABLE horaire_edt
            ADD CONSTRAINT fk_horaire_edt_niveau
            FOREIGN KEY (niveau_id) REFERENCES niveaux(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 4. Création d'index uniques conditionnels pour garantir l'unicité des heures de début et fin :
--    - Une plage horaire unique à une heure donnée au niveau global (niveau_id IS NULL)
--    - Une plage horaire unique à une heure donnée pour un niveau spécifique (niveau_id IS NOT NULL)
CREATE UNIQUE INDEX IF NOT EXISTS idx_horaire_edt_unique_global 
ON horaire_edt (heure_debut, heure_fin) 
WHERE niveau_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_horaire_edt_unique_level 
ON horaire_edt (niveau_id, heure_debut, heure_fin) 
WHERE niveau_id IS NOT NULL;

-- 5. Création d'un index sur niveau_id pour optimiser les performances des requêtes
CREATE INDEX IF NOT EXISTS idx_horaire_edt_niveau_id ON horaire_edt(niveau_id);

COMMIT;
