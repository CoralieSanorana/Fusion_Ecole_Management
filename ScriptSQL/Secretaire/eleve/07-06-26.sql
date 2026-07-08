CREATE TABLE IF NOT EXISTS historique_reinscriptions (
    id                          SERIAL PRIMARY KEY,
    inscription_id_old          INT REFERENCES inscriptions(id) ON DELETE SET NULL,
    inscription_id_new          INT REFERENCES inscriptions(id) ON DELETE CASCADE,
    etudiant_id                 INT REFERENCES profils_etudiants(id),
    ancienne_annee_scolaire_id  INT,
    nouvelle_annee_scolaire_id  INT,
    ancienne_classe_id          INT,
    nouvelle_classe_id          INT,
    ancien_statut               VARCHAR(50),
    ancien_rang_final           INT,
    ancien_resultat             VARCHAR(50),
    ancienne_moyenne_generale   NUMERIC(5,2),
    absences_annee_precedente  INT,
    change_par                  INT REFERENCES users(id),
    created_at                  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hist_reinscr_etudiant ON historique_reinscriptions(etudiant_id);

CREATE OR REPLACE FUNCTION fn_archiver_reinscription()
RETURNS TRIGGER AS $$
DECLARE
    v_ancienne_inscription RECORD;
BEGIN
    IF NEW.type_inscription = 'reinscription' THEN
        SELECT i.*
        INTO v_ancienne_inscription
        FROM inscriptions i
        JOIN annees_scolaires a ON a.id = i.annee_scolaire_id
        WHERE i.etudiant_id = NEW.etudiant_id
          AND i.id <> NEW.id
        ORDER BY a.date_fin DESC, i.date_inscription DESC
        LIMIT 1;

        IF v_ancienne_inscription.id IS NOT NULL THEN
            INSERT INTO historique_reinscriptions (
                inscription_id_old,
                inscription_id_new,
                etudiant_id,
                ancienne_annee_scolaire_id,
                nouvelle_annee_scolaire_id,
                ancienne_classe_id,
                nouvelle_classe_id,
                ancien_statut,
                ancien_rang_final,
                ancien_resultat,
                ancienne_moyenne_generale,
                absences_annee_precedente,
                change_par,
                created_at
            ) VALUES (
                v_ancienne_inscription.id,
                NEW.id,
                NEW.etudiant_id,
                v_ancienne_inscription.annee_scolaire_id,
                NEW.annee_scolaire_id,
                v_ancienne_inscription.classe_id,
                NEW.classe_id,
                v_ancienne_inscription.statut,
                v_ancienne_inscription.rang_final,
                CASE WHEN v_ancienne_inscription.est_admis THEN 'admis' ELSE 'non_admis' END,
                NULL,
                NULL,
                NULL,
                NOW()
            );
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_archiver_reinscription
AFTER INSERT ON inscriptions
FOR EACH ROW
EXECUTE FUNCTION fn_archiver_reinscription();
