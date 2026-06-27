-- Table déterminant le professeur titulaire (professeur principal) d'une classe par année
-- UNIQUE sur (classe_id, annee_scolaire_id) : une classe n'a qu'un seul titulaire par an.
CREATE TABLE titulaires_classes (
    id                SERIAL PRIMARY KEY,
    professeur_id     BIGINT REFERENCES profils_professeurs(id) ON DELETE CASCADE,
    classe_id         BIGINT REFERENCES classes(id) ON DELETE CASCADE,
    annee_scolaire_id BIGINT REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    date_nomination   DATE DEFAULT CURRENT_DATE,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    
    -- Contrainte 1 : Un seul prof titulaire par classe par an
    CONSTRAINT uk_classe_annee_titulaire UNIQUE (classe_id, annee_scolaire_id)
);

-- Index de performance
CREATE INDEX idx_titulaires_prof          ON titulaires_classes(professeur_id);
CREATE INDEX idx_titulaires_classe_annee  ON titulaires_classes(classe_id, annee_scolaire_id);
