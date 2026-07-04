CREATE TABLE devoirs (
    id                SERIAL PRIMARY KEY,
    matiere_id        INT REFERENCES matieres(id),
    professeur_id     INT REFERENCES profils_professeurs(id),
    titre             VARCHAR(255) NOT NULL,
    affectation_id    INT REFERENCES affectations_enseignement(id),
    description       TEXT,
    date_limite       DATE,
    date_publication  DATE DEFAULT CURRENT_DATE,
    est_actif         BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE lecons (
    id               SERIAL PRIMARY KEY,
    affectation_id   INT REFERENCES affectations_enseignement(id),
    titre            VARCHAR(255) NOT NULL,
    contenu          TEXT,
    date_publication DATE DEFAULT CURRENT_DATE,
    document_url     VARCHAR(500),
    created_at       TIMESTAMP DEFAULT NOW()
);

Alter table note add COLUMN trimestre VARCHAR(250);