-- ============================================================
-- SCRIPT DIRECTEUR - DASHBOARD.HTML
-- Tables concernées pour la gestion du tableau de bord
-- Basé sur les tests effectués par Funaki Live ETU004169
-- Date : 11 Juillet 2026
-- ============================================================

BEGIN;

-- ============================================================
-- TABLES UTILISÉES PAR DASHBOARD.HTML
-- ============================================================

-- Table pour les transactions financières
CREATE TABLE IF NOT EXISTS transactions (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    annee_scolaire_id     INT REFERENCES annees_scolaires(id),
    type                  VARCHAR(50) NOT NULL,
    montant               NUMERIC(12,2) NOT NULL,
    date_transaction      DATE NOT NULL,
    description           TEXT,
    reference             VARCHAR(200),
    cree_par              INT REFERENCES users(id),
    created_at            TIMESTAMP DEFAULT NOW()
);

-- Table pour les dépenses
CREATE TABLE IF NOT EXISTS depenses (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    annee_scolaire_id     INT REFERENCES annees_scolaires(id),
    categorie_id          INT REFERENCES categories_depenses(id),
    fournisseur_id        INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    contrat_id            INT REFERENCES contrats_charges(id),
    echeance_contrat_id   INT REFERENCES echeances_contrats(id),
    prevision_id          INT REFERENCES previsions_depenses(id),
    intitule              VARCHAR(255) NOT NULL,
    type_charge           VARCHAR(20) NOT NULL,
    motif                 TEXT,
    montant               NUMERIC(12,2) NOT NULL,
    date_depense          DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    reference             VARCHAR(200),
    justificatif_url      VARCHAR(500),
    necessite_approbation BOOLEAN DEFAULT FALSE,
    statut_approbation    VARCHAR(50) DEFAULT 'approuvee',
    approuve_par          INT REFERENCES users(id),
    date_approbation      TIMESTAMP,
    saisi_par             INT REFERENCES users(id),
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

-- Table pour les paiements des écolages
CREATE TABLE IF NOT EXISTS paiements (
    id                    SERIAL PRIMARY KEY,
    echeance_id           INT REFERENCES echeances(id),
    inscription_id        INT REFERENCES inscriptions(id),
    montant               NUMERIC(12,2) NOT NULL,
    date_paiement         DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    reference_transaction VARCHAR(200),
    saisi_par             INT REFERENCES users(id),
    notes                 TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLES DE SUPPORT POUR DASHBOARD
-- ============================================================

-- Table des catégories de dépenses
CREATE TABLE IF NOT EXISTS categories_depenses (
    id          SERIAL PRIMARY KEY,
    parent_id   INT REFERENCES categories_depenses(id) ON DELETE SET NULL,
    nom         VARCHAR(150) NOT NULL,
    type_charge VARCHAR(20) DEFAULT 'variable',
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Table des fournisseurs
CREATE TABLE IF NOT EXISTS fournisseurs (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(255) NOT NULL,
    type             VARCHAR(100),
    contact_nom      VARCHAR(200),
    telephone        VARCHAR(50),
    email            VARCHAR(255),
    adresse          TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INDEX POUR OPTIMISATION DASHBOARD
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_depenses_annee ON depenses(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_depenses_date ON depenses(date_depense);
CREATE INDEX IF NOT EXISTS idx_paiements_inscription ON paiements(inscription_id);
CREATE INDEX IF NOT EXISTS idx_transactions_annee ON transactions(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date_transaction);

COMMIT;
