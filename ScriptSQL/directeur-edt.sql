-- ============================================================
-- SCRIPT DIRECTEUR - EDT.HTML (EMPLOIS DU TEMPS)
-- Tables concernées pour la gestion des emplois du temps
-- Basé sur les tests effectués par Funaki Live ETU004169
-- Date : 11 Juillet 2026
-- ============================================================

BEGIN;

-- ============================================================
-- TABLES UTILISÉES PAR EDT.HTML
-- ============================================================

-- Table des horaires EDT
CREATE TABLE IF NOT EXISTS horaire_edt (
    id           SERIAL PRIMARY KEY,
    libelle      VARCHAR(100) NOT NULL,
    heure_debut  TIME NOT NULL,
    heure_fin    TIME NOT NULL,
    ordre        INT NOT NULL,
    is_active    BOOLEAN DEFAULT TRUE,
    niveau_id    INT REFERENCES niveaux(id) ON DELETE CASCADE,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Table des emplois du temps
CREATE TABLE IF NOT EXISTS emploi_du_temps (
    id                  SERIAL PRIMARY KEY,
    affectation_id      INT REFERENCES affectations_enseignement(id),
    salle_id            INT REFERENCES salles(id),
    jour_semaine        INT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    heure_debut         TIME NOT NULL,
    heure_fin           TIME NOT NULL,
    date_debut_validite DATE,
    date_fin_validite   DATE,
    horaire_edt_id      INT REFERENCES horaire_edt(id) ON DELETE SET NULL,
    created_at          TIMESTAMP DEFAULT NOW()
);

-- Table des affectations d'enseignement
CREATE TABLE IF NOT EXISTS affectations_enseignement (
    id                SERIAL PRIMARY KEY,
    professeur_id     INT REFERENCES profils_professeurs(id),
    matiere_id        INT REFERENCES matieres(id),
    classe_id         INT REFERENCES classes(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    heures_hebdo      NUMERIC(4,1),
    created_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (matiere_id, classe_id, annee_scolaire_id)
);

-- ============================================================
-- TABLES DE SUPPORT POUR EDT
-- ============================================================

-- Table des niveaux (référence pour les horaires)
CREATE TABLE IF NOT EXISTS niveaux (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(100) NOT NULL,
    ordre            INT NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des salles (référence pour les emplois du temps)
CREATE TABLE IF NOT EXISTS salles (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(100) NOT NULL,
    capacite         INT,
    type             VARCHAR(50) DEFAULT 'cours',
    is_active        BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT NOW()
);

-- Table des classes (référence pour les affectations)
CREATE TABLE IF NOT EXISTS classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,
    capacite_max      INT DEFAULT 40,
    salle_id          INT REFERENCES salles(id) ON DELETE SET NULL,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- Table des matières (référence pour les affectations)
CREATE TABLE IF NOT EXISTS matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,
    code             VARCHAR(20),
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des années scolaires (référence pour les affectations)
CREATE TABLE IF NOT EXISTS annees_scolaires (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(50) NOT NULL,
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    est_active       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des profils professeurs (référence pour les affectations)
CREATE TABLE IF NOT EXISTS profils_professeurs (
    id                 SERIAL PRIMARY KEY,
    user_id            INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    matricule          VARCHAR(100) UNIQUE NOT NULL,
    nom                VARCHAR(150) NOT NULL,
    prenom             VARCHAR(150) NOT NULL,
    date_naissance     DATE,
    sexe               CHAR(1) CHECK (sexe IN ('H', 'F')),
    photo_url          VARCHAR(500),
    telephone          VARCHAR(50),
    adresse            TEXT,
    specialite         VARCHAR(200),
    type_contrat       VARCHAR(50),
    date_debut_contrat DATE,
    date_fin_contrat   DATE,
    is_archived        BOOLEAN DEFAULT FALSE,
    id_contrat         INT REFERENCES contrats_employes(id) ON DELETE SET NULL,
    id_matiere         INT,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP DEFAULT NOW()
);

-- Table des établissements (référence pour les niveaux, salles, matières)
CREATE TABLE IF NOT EXISTS etablissements (
    id           SERIAL PRIMARY KEY,
    nom          VARCHAR(255) NOT NULL,
    adresse      TEXT,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    logo_url     VARCHAR(500),
    directeur_id INT,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INDEX POUR OPTIMISATION EDT
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_edt_affectation ON emploi_du_temps(affectation_id);
CREATE INDEX IF NOT EXISTS idx_edt_salle ON emploi_du_temps(salle_id);
CREATE INDEX IF NOT EXISTS idx_edt_validite ON emploi_du_temps(date_debut_validite, date_fin_validite);
CREATE INDEX IF NOT EXISTS idx_horaire_niveau ON horaire_edt(niveau_id);
CREATE INDEX IF NOT EXISTS idx_affectation_prof ON affectations_enseignement(professeur_id);
CREATE INDEX IF NOT EXISTS idx_affectation_classe_annee ON affectations_enseignement(classe_id, annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_affectation_matiere ON affectations_enseignement(matiere_id);

COMMIT;
