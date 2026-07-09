-- ============================================================
-- SECTION 1 — AUTHENTIFICATION & RÔLES
-- Commun à toutes les équipes.
-- Chaque acteur (étudiant, prof, secrétaire, directeur, parent…)
-- possède un compte "users" + un profil dédié dans sa propre table.
-- ============================================================

-- Comptes d'accès unifiés pour tous les acteurs du système
CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,         -- bcrypt via CI4 Password helper
    is_active     BOOLEAN   DEFAULT TRUE,        -- désactivation sans suppression physique
    last_login    TIMESTAMP,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);
-- Rôles disponibles dans le système
-- Valeurs attendues : 'super_admin', 'directeur', 'secretariat',
-- 'comptable', 'professeur', 'etudiant', 'parent'
CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Liaison utilisateurs ↔ rôles (many-to-many)
-- Un utilisateur peut cumuler plusieurs rôles (ex : prof + parent)
CREATE TABLE user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Permissions granulaires associées aux rôles
-- Permet de contrôler des actions précises sans changer de rôle
-- ex : 'notes.write', 'finances.approve', 'edt.edit'
CREATE TABLE permissions (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(150) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE role_permissions (
    role_id       INT REFERENCES roles(id)       ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
