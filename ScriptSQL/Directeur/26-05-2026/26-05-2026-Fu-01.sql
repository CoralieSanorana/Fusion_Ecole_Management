-- ============================================================
--  MIGRATION DIRECTEUR — HORAIRES EDT + GRILLE INTERACTIVE
--  - crée la table horaire_edt si elle n'existe pas
--  - rattache emploi_du_temps à un horaire de référence
--  - seed les plages horaires standards de l'école
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS horaire_edt (
    id           SERIAL PRIMARY KEY,
    libelle      VARCHAR(100) NOT NULL,
    heure_debut  TIME NOT NULL,
    heure_fin    TIME NOT NULL,
    ordre        INT NOT NULL,
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (heure_debut, heure_fin)
);

ALTER TABLE IF EXISTS emploi_du_temps
    ADD COLUMN IF NOT EXISTS horaire_edt_id INT;

ALTER TABLE IF EXISTS classes
    ADD COLUMN IF NOT EXISTS salle_id INT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'emploi_du_temps'
          AND tc.constraint_name = 'fk_emploi_du_temps_horaire_edt'
    ) THEN
        ALTER TABLE emploi_du_temps
            ADD CONSTRAINT fk_emploi_du_temps_horaire_edt
            FOREIGN KEY (horaire_edt_id) REFERENCES horaire_edt(id) ON DELETE SET NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'classes'
          AND tc.constraint_name = 'fk_classes_salle'
    ) THEN
        ALTER TABLE classes
            ADD CONSTRAINT fk_classes_salle
            FOREIGN KEY (salle_id) REFERENCES salles(id) ON DELETE SET NULL;
    END IF;
END $$;

INSERT INTO horaire_edt (libelle, heure_debut, heure_fin, ordre)
VALUES
    ('07h00 - 08h00', '07:00', '08:00', 1),
    ('08h00 - 09h00', '08:00', '09:00', 2),
    ('09h00 - 10h00', '09:00', '10:00', 3),
    ('10h00 - 11h00', '10:00', '11:00', 4),
    ('11h00 - 12h00', '11:00', '12:00', 5),
    ('13h00 - 14h00', '13:00', '14:00', 6),
    ('14h00 - 15h00', '14:00', '15:00', 7),
    ('15h00 - 16h00', '15:00', '16:00', 8),
    ('16h00 - 17h00', '16:00', '17:00', 9)
ON CONFLICT (heure_debut, heure_fin) DO NOTHING;

UPDATE emploi_du_temps e
SET horaire_edt_id = h.id
FROM horaire_edt h
WHERE e.horaire_edt_id IS NULL
  AND e.heure_debut = h.heure_debut
  AND e.heure_fin = h.heure_fin;

CREATE INDEX IF NOT EXISTS idx_horaire_edt_ordre ON horaire_edt(ordre);
CREATE INDEX IF NOT EXISTS idx_edt_horaire ON emploi_du_temps(horaire_edt_id);
CREATE INDEX IF NOT EXISTS idx_classes_salle ON classes(salle_id);

COMMIT;