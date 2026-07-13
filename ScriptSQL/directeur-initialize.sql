-- ============================================================
-- SCRIPT DIRECTEUR - INITIALIZE.HTML
-- Tables concernées pour l'initialisation de l'école
-- Basé sur les tests effectués par Funaki Live ETU004169
-- Date : 11 Juillet 2026
-- ============================================================

BEGIN;

-- ============================================================
-- TABLES UTILISÉES PAR INITIALIZE.HTML
-- ============================================================

-- Table des établissements
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

-- Table des années scolaires
CREATE TABLE IF NOT EXISTS annees_scolaires (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(50) NOT NULL,
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    est_active       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des niveaux
CREATE TABLE IF NOT EXISTS niveaux (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(100) NOT NULL,
    ordre            INT NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des salles
CREATE TABLE IF NOT EXISTS salles (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(100) NOT NULL,
    capacite         INT,
    type             VARCHAR(50) DEFAULT 'cours',
    is_active        BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT NOW()
);

-- Table des matières
CREATE TABLE IF NOT EXISTS matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,
    code             VARCHAR(20),
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Table des classes
CREATE TABLE IF NOT EXISTS classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,
    capacite_max      INT DEFAULT 40,
    salle_id          INT REFERENCES salles(id) ON DELETE SET NULL,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLES UTILISATEURS ET ROLES
-- ============================================================

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    is_active     BOOLEAN   DEFAULT TRUE,
    last_login    TIMESTAMP,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

-- Table des rôles
CREATE TABLE IF NOT EXISTS roles (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Table de liaison users-roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- TABLES PROFILS
-- ============================================================

-- Table des profils directeurs
CREATE TABLE IF NOT EXISTS profils_directeurs (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    photo_url  VARCHAR(500),
    sexe       CHAR(1),
    id_contrat INT REFERENCES contrats_employes(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Table des profils professeurs
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

-- ============================================================
-- TABLES DE SUPPORT (CONTRATS)
-- ============================================================

-- Table des contrats employés
CREATE TABLE IF NOT EXISTS contrats_employes (
    id                SERIAL PRIMARY KEY,
    user_id           INT REFERENCES users(id) ON DELETE CASCADE,
    fonction          VARCHAR(50) NOT NULL,
    type_contrat_id   INT REFERENCES types_contrats_employes(id),
    sexe              CHAR(1),
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

-- Table des types de contrats employés
CREATE TABLE IF NOT EXISTS types_contrats_employes (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    duree_mois  INT,
    description TEXT,
    est_actif   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INDEX POUR OPTIMISATION INITIALIZE
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_classes_niveau_annee ON classes(niveau_id, annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_niveaux_etablissement ON niveaux(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_salles_etablissement ON salles(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_matieres_etablissement ON matieres(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_annees_etablissement ON annees_scolaires(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

COMMIT;
