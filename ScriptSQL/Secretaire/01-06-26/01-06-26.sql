-- ============================================================
-- SECTION 1 — AUTHENTIFICATION & RoLES
-- Commun a toutes les equipes.
-- Chaque acteur (etudiant, prof, secretaire, directeur, parent…)
-- possede un compte "users" + un profil dedie dans sa propre table.
-- ============================================================

-- Comptes d'acces unifies pour tous les acteurs du systeme
CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,         -- bcrypt via CI4 Password helper
    is_active     BOOLEAN   DEFAULT TRUE,        -- desactivation sans suppression physique
    last_login    TIMESTAMP,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);
-- Roles disponibles dans le systeme
-- Valeurs attendues : 'super_admin', 'directeur', 'secretariat',
-- 'comptable', 'professeur', 'etudiant', 'parent'
CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Liaison utilisateurs ↔ roles (many-to-many)
-- Un utilisateur peut cumuler plusieurs roles (ex : prof + parent)
CREATE TABLE user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Permissions granulaires associees aux roles
-- Permet de controler des actions precises sans changer de role
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
