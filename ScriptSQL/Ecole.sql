BEGIN;

-- ============================================================
-- 1. CRÉATION DES TABLES
-- ============================================================

-- Tables sans dépendances
CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    is_active     BOOLEAN   DEFAULT TRUE,
    last_login    TIMESTAMP,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS roles (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS permissions (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(150) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS notification_types (
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(100) UNIQUE NOT NULL,
    libelle          VARCHAR(255),
    template_message TEXT
);

CREATE TABLE IF NOT EXISTS types_fichiers (
    id          SERIAL PRIMARY KEY,
    libelle     VARCHAR(100) UNIQUE NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS types_contrats_employes (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    duree_mois  INT,
    description TEXT,
    est_actif   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
);

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

CREATE TABLE IF NOT EXISTS annees_scolaires (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(50) NOT NULL,
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    est_active       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS niveaux (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(100) NOT NULL,
    ordre            INT NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,
    code             VARCHAR(20),
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS salles (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(100) NOT NULL,
    capacite         INT,
    type             VARCHAR(50) DEFAULT 'cours',
    is_active        BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT NOW()
);

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

CREATE TABLE IF NOT EXISTS profils_secretariat (
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

CREATE TABLE IF NOT EXISTS profils_comptables (
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

CREATE TABLE IF NOT EXISTS profils_etudiants (
    id             SERIAL PRIMARY KEY,
    user_id        INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    matricule      VARCHAR(100) UNIQUE NOT NULL,
    nom            VARCHAR(150) NOT NULL,
    prenom         VARCHAR(150) NOT NULL,
    date_naissance DATE,
    lieu_naissance VARCHAR(200),
    sexe           CHAR(1) CHECK (sexe IN ('M', 'F')),
    photo_url      VARCHAR(500),
    adresse        TEXT,
    commune        VARCHAR(150),
    region         VARCHAR(150),
    nationalite    VARCHAR(100) DEFAULT 'Malgache',
    cin            VARCHAR(50),
    telephone      VARCHAR(50),
    is_archived    BOOLEAN DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS profils_parents (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE SET NULL,
    nom          VARCHAR(150) NOT NULL,
    prenom       VARCHAR(150) NOT NULL,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    profession   VARCHAR(200),
    lien_parente VARCHAR(100),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,
    capacite_max      INT DEFAULT 40,
    salle_id          INT REFERENCES salles(id) ON DELETE SET NULL,
    created_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS coefficients (
    id         SERIAL PRIMARY KEY,
    matiere_id INT REFERENCES matieres(id) ON DELETE CASCADE,
    niveau_id  INT REFERENCES niveaux(id)  ON DELETE CASCADE,
    valeur     NUMERIC(4,2) NOT NULL,
    UNIQUE (matiere_id, niveau_id)
);

CREATE TABLE IF NOT EXISTS periodes (
    id                     SERIAL PRIMARY KEY,
    annee_scolaire_id      INT REFERENCES annees_scolaires(id),
    libelle                VARCHAR(100) NOT NULL,
    type                   VARCHAR(20) DEFAULT 'trimestre',
    ordre                  INT NOT NULL,
    date_debut             DATE,
    date_fin               DATE,
    date_publication_notes DATE,
    est_cloturee           BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS inscriptions (
    id                SERIAL PRIMARY KEY,
    etudiant_id       INT REFERENCES profils_etudiants(id),
    classe_id         INT REFERENCES classes(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    type_inscription  VARCHAR(50) DEFAULT 'reinscription',
    date_inscription  DATE        DEFAULT CURRENT_DATE,
    statut            VARCHAR(50) DEFAULT 'active',
    rang_final        INT,
    est_admis         BOOLEAN,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, annee_scolaire_id)
);

CREATE TABLE IF NOT EXISTS etudiants_parents (
    etudiant_id           INT REFERENCES profils_etudiants(id) ON DELETE CASCADE,
    parent_id             INT REFERENCES profils_parents(id) ON DELETE CASCADE,
    est_contact_principal BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (etudiant_id, parent_id)
);

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

CREATE TABLE IF NOT EXISTS modifications_edt (
    id                   SERIAL PRIMARY KEY,
    emploi_du_temps_id   INT REFERENCES emploi_du_temps(id),
    date_concernee       DATE NOT NULL,
    portee               VARCHAR(20) DEFAULT 'ponctuel',
    type_modification    VARCHAR(50) NOT NULL,
    motif                VARCHAR(500),
    nouvelle_salle_id    INT REFERENCES salles(id),
    nouvelle_heure_debut TIME,
    nouvelle_heure_fin   TIME,
    remplacant_id        INT REFERENCES profils_professeurs(id),
    cree_par             INT REFERENCES users(id),
    created_at           TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS seances (
    id                 SERIAL PRIMARY KEY,
    emploi_du_temps_id INT REFERENCES emploi_du_temps(id),
    date_seance        DATE NOT NULL,
    heure_debut        TIME,
    heure_fin          TIME,
    a_eu_lieu          BOOLEAN DEFAULT TRUE,
    created_at         TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS absences (
    id               SERIAL PRIMARY KEY,
    seance_id        INT REFERENCES seances(id),
    etudiant_id      INT REFERENCES profils_etudiants(id),
    type             VARCHAR(50) DEFAULT 'non_justifiee',
    motif            TEXT,
    justificatif_url VARCHAR(500),
    saisi_par        INT REFERENCES users(id),
    valide_par       INT REFERENCES users(id),
    date_validation  TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    UNIQUE (seance_id, etudiant_id)
);

CREATE TABLE IF NOT EXISTS notes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    affectation_id  INT REFERENCES affectations_enseignement(id),
    periode_id      INT REFERENCES periodes(id),
    type_evaluation VARCHAR(100),
    valeur          NUMERIC(5,2) NOT NULL CHECK (valeur >= 0),
    sur             NUMERIC(5,2) DEFAULT 20.00,
    commentaire     TEXT,
    saisi_par       INT REFERENCES users(id),
    date_saisie     TIMESTAMP DEFAULT NOW(),
    est_valide      BOOLEAN DEFAULT TRUE,
    ancienne_valeur NUMERIC(5,2),
    corrige_par     INT REFERENCES users(id),
    date_correction TIMESTAMP,
    motif_correction TEXT,
    trimestre       VARCHAR(250),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS moyennes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    inscription_id  INT REFERENCES inscriptions(id),
    periode_id      INT REFERENCES periodes(id),
    matiere_id      INT REFERENCES matieres(id),
    valeur          NUMERIC(5,2),
    rang            INT,
    effectif_classe INT,
    calculated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, inscription_id, periode_id, matiere_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS categories_depenses (
    id          SERIAL PRIMARY KEY,
    parent_id   INT REFERENCES categories_depenses(id) ON DELETE SET NULL,
    nom         VARCHAR(150) NOT NULL,
    type_charge VARCHAR(20) DEFAULT 'variable',
    created_at  TIMESTAMP DEFAULT NOW()
);

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

CREATE TABLE IF NOT EXISTS contrats_charges (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    fournisseur_id   INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    categorie_id     INT REFERENCES categories_depenses(id),
    intitule         VARCHAR(255) NOT NULL,
    description      TEXT,
    type_recurrence  VARCHAR(50) NOT NULL,
    montant_prevu    NUMERIC(12,2) NOT NULL,
    jour_echeance    INT,
    date_debut       DATE NOT NULL,
    date_fin         DATE,
    statut           VARCHAR(50) DEFAULT 'actif',
    numero_contrat   VARCHAR(150),
    document_url     VARCHAR(500),
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS echeances_contrats (
    id               SERIAL PRIMARY KEY,
    contrat_id       INT REFERENCES contrats_charges(id) ON DELETE CASCADE,
    periode_concernee VARCHAR(50) NOT NULL,
    date_echeance    DATE NOT NULL,
    montant_prevu    NUMERIC(12,2) NOT NULL,
    statut           VARCHAR(50) DEFAULT 'en_attente',
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS previsions_depenses (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id     INT REFERENCES categories_depenses(id),
    fournisseur_id   INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    intitule         VARCHAR(255) NOT NULL,
    description      TEXT,
    montant_estime   NUMERIC(12,2) NOT NULL,
    date_prevue      DATE NOT NULL,
    type_charge      VARCHAR(20) NOT NULL,
    statut           VARCHAR(50) DEFAULT 'planifiee',
    approuve_par     INT REFERENCES users(id),
    date_approbation TIMESTAMP,
    depense_id       INT,
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS grilles_tarifaires (
    id                SERIAL PRIMARY KEY,
    etablissement_id  INT REFERENCES etablissements(id),
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    montant_total     NUMERIC(12,2) NOT NULL,
    description       TEXT,
    created_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (niveau_id, annee_scolaire_id)
);

CREATE TABLE IF NOT EXISTS echeanciers (
    id             SERIAL PRIMARY KEY,
    inscription_id INT REFERENCES inscriptions(id),
    grille_id      INT REFERENCES grilles_tarifaires(id),
    type           VARCHAR(50),
    montant_total  NUMERIC(12,2),
    created_at     TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS echeances (
    id               SERIAL PRIMARY KEY,
    echeancier_id    INT REFERENCES echeanciers(id) ON DELETE CASCADE,
    numero_tranche   INT NOT NULL,
    montant_attendu  NUMERIC(12,2) NOT NULL,
    date_limite      DATE NOT NULL,
    est_soldee       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);

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

CREATE TABLE IF NOT EXISTS budgets (
    id                SERIAL PRIMARY KEY,
    etablissement_id  INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id      INT REFERENCES categories_depenses(id),
    montant_prevu     NUMERIC(12,2) NOT NULL,
    created_by        INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (annee_scolaire_id, categorie_id)
);

CREATE TABLE IF NOT EXISTS evenements (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    titre                 VARCHAR(255) NOT NULL,
    description           TEXT,
    type                  VARCHAR(100),
    est_recurrente        BOOLEAN DEFAULT FALSE,
    type_recurrence       VARCHAR(20),
    jour_recurrence       INT CHECK (jour_recurrence BETWEEN 1 AND 31),
    mois_recurrence       INT CHECK (mois_recurrence BETWEEN 1 AND 12),
    duree_jours           INT DEFAULT 1,
    heure_debut_defaut    TIME,
    heure_fin_defaut      TIME,
    annule_cours          BOOLEAN DEFAULT FALSE,
    concerne_toute_ecole  BOOLEAN DEFAULT TRUE,
    concerne_matiere_id   INT REFERENCES matieres(id),
    cree_par              INT REFERENCES users(id),
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS evenements_instances (
    id                SERIAL PRIMARY KEY,
    evenement_id      INT REFERENCES evenements(id) ON DELETE CASCADE,
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    classe_id         INT REFERENCES classes(id),
    date_debut        DATE NOT NULL,
    date_fin          DATE,
    heure_debut       TIME,
    heure_fin         TIME,
    salle_id          INT REFERENCES salles(id),
    lieu_externe      VARCHAR(255),
    statut            VARCHAR(50) DEFAULT 'planifie',
    notes             TEXT,
    cree_par          INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notifications (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE CASCADE,
    type_id      INT REFERENCES notification_types(id),
    titre        VARCHAR(255) NOT NULL,
    message      TEXT NOT NULL,
    lien_action  VARCHAR(500),
    est_lu       BOOLEAN   DEFAULT FALSE,
    date_lecture TIMESTAMP,
    entite_type  VARCHAR(100),
    entite_id    INT,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS actualites (
    id               SERIAL PRIMARY KEY,
    titre            VARCHAR(255) NOT NULL,
    contenu          TEXT NOT NULL,
    categorie        VARCHAR(100),
    auteur_id        INT REFERENCES users(id) ON DELETE SET NULL,
    auteur_nom       VARCHAR(255),
    image_url        VARCHAR(500),
    icone_classe     VARCHAR(100),
    date_publication TIMESTAMP DEFAULT NOW(),
    est_active       BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS documents (
    id                SERIAL PRIMARY KEY,
    etudiant_id       INT REFERENCES profils_etudiants(id),
    type_document     VARCHAR(100) NOT NULL,
    titre             VARCHAR(255),
    fichier_url       VARCHAR(500),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    periode_id        INT REFERENCES periodes(id),
    genere_par        INT REFERENCES users(id),
    genere_le         TIMESTAMP DEFAULT NOW(),
    est_valide        BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS demandes_modification_dossier (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    champ_modifie   VARCHAR(150) NOT NULL,
    ancienne_valeur TEXT,
    nouvelle_valeur TEXT,
    motif           TEXT,
    statut          VARCHAR(50) DEFAULT 'en_attente',
    soumis_par      INT REFERENCES users(id),
    traite_par      INT REFERENCES users(id),
    date_traitement TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_log (
    id                SERIAL PRIMARY KEY,
    user_id           INT REFERENCES users(id) ON DELETE SET NULL,
    action            VARCHAR(200) NOT NULL,
    table_concernee   VARCHAR(100),
    entite_id         INT,
    anciennes_valeurs JSONB,
    nouvelles_valeurs JSONB,
    ip_address        VARCHAR(45),
    user_agent        VARCHAR(500),
    created_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS supports_cours (
    id                SERIAL PRIMARY KEY,
    affectation_id    INT REFERENCES affectations_enseignement(id) ON DELETE CASCADE,
    type_fichier_id   INT REFERENCES types_fichiers(id) ON DELETE SET NULL,
    titre             VARCHAR(255) NOT NULL,
    description       TEXT,
    fichier_url       VARCHAR(500),
    type_contenu      VARCHAR(50) DEFAULT 'lecon',
    date_limite       TIMESTAMP,
    accepte_retard    BOOLEAN DEFAULT FALSE,
    is_archived       BOOLEAN DEFAULT FALSE,
    cree_par          INT REFERENCES users(id) ON DELETE SET NULL,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS devoirs (
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

CREATE TABLE IF NOT EXISTS lecons (
    id               SERIAL PRIMARY KEY,
    affectation_id   INT REFERENCES affectations_enseignement(id),
    titre            VARCHAR(255) NOT NULL,
    contenu          TEXT,
    date_publication DATE DEFAULT CURRENT_DATE,
    document_url     VARCHAR(500),
    created_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS titulaires_classes (
    id                SERIAL PRIMARY KEY,
    professeur_id     BIGINT REFERENCES profils_professeurs(id) ON DELETE CASCADE,
    classe_id         BIGINT REFERENCES classes(id) ON DELETE CASCADE,
    annee_scolaire_id BIGINT REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    date_nomination   DATE DEFAULT CURRENT_DATE,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uk_classe_annee_titulaire UNIQUE (classe_id, annee_scolaire_id)
);

-- ============================================================
-- 2. TABLE TYPES_DEPENSES (ajoutée par le script 04-07-2026)
-- ============================================================
CREATE TABLE IF NOT EXISTS types_depenses (
    id          SERIAL PRIMARY KEY,
    libelle     VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 3. AJOUT DE COLONNES À LA TABLE DEPENSES
-- ============================================================
ALTER TABLE depenses
    ADD COLUMN IF NOT EXISTS type_depense_id INT,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS prix_unitaire NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS quantite NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS total NUMERIC(15,2),
    ADD COLUMN IF NOT EXISTS saisi_par INT;

ALTER TABLE depenses
    ALTER COLUMN prix_unitaire SET DEFAULT 0,
    ALTER COLUMN quantite SET DEFAULT 1,
    ALTER COLUMN total SET DEFAULT 0;

-- ============================================================
-- 4. CONTRAINTES DE CLÉ ÉTRANGÈRE (FK)
-- ============================================================

-- Etablissements -> directeur (profils_directeurs)
ALTER TABLE etablissements
    ADD CONSTRAINT fk_etablissements_directeur
    FOREIGN KEY (directeur_id) REFERENCES profils_directeurs(id) ON DELETE SET NULL;

-- Previsions_depenses -> depenses
ALTER TABLE previsions_depenses
    ADD CONSTRAINT fk_prevision_depense
    FOREIGN KEY (depense_id) REFERENCES depenses(id) ON DELETE SET NULL;

-- Profils_professeurs -> matieres
ALTER TABLE profils_professeurs
    ADD CONSTRAINT fk_profils_professeurs_id_matiere
    FOREIGN KEY (id_matiere) REFERENCES matieres(id) ON DELETE SET NULL;

-- Profils_professeurs, secretariat, directeurs, comptables -> contrats_employes
ALTER TABLE profils_professeurs
    ADD CONSTRAINT fk_profils_professeurs_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_secretariat
    ADD CONSTRAINT fk_profils_secretariat_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_directeurs
    ADD CONSTRAINT fk_profils_directeurs_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_comptables
    ADD CONSTRAINT fk_profils_comptables_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

-- Depenses -> types_depenses (FK ajoutée par 04-07-2026)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_depenses_type_depense'
    ) THEN
        ALTER TABLE depenses
            ADD CONSTRAINT fk_depenses_type_depense
            FOREIGN KEY (type_depense_id) REFERENCES types_depenses(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- ============================================================
-- 5. INDEX
-- ============================================================

-- Index généraux
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_etudiants_matricule ON profils_etudiants(matricule);
CREATE INDEX IF NOT EXISTS idx_professeurs_matricule ON profils_professeurs(matricule);
CREATE INDEX IF NOT EXISTS idx_classes_niveau_annee ON classes(niveau_id, annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_affectation_prof ON affectations_enseignement(professeur_id);
CREATE INDEX IF NOT EXISTS idx_affectation_classe_annee ON affectations_enseignement(classe_id, annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_inscriptions_etudiant ON inscriptions(etudiant_id);
CREATE INDEX IF NOT EXISTS idx_inscriptions_classe ON inscriptions(classe_id);
CREATE INDEX IF NOT EXISTS idx_inscriptions_annee ON inscriptions(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_edt_affectation ON emploi_du_temps(affectation_id);
CREATE INDEX IF NOT EXISTS idx_edt_validite ON emploi_du_temps(date_debut_validite, date_fin_validite);
CREATE INDEX IF NOT EXISTS idx_modif_edt_date ON modifications_edt(emploi_du_temps_id, date_concernee);
CREATE INDEX IF NOT EXISTS idx_seances_date ON seances(date_seance);
CREATE INDEX IF NOT EXISTS idx_notes_etudiant ON notes(etudiant_id);
CREATE INDEX IF NOT EXISTS idx_notes_affectation ON notes(affectation_id);
CREATE INDEX IF NOT EXISTS idx_notes_periode ON notes(periode_id);
CREATE INDEX IF NOT EXISTS idx_moyennes_etudiant ON moyennes(etudiant_id, inscription_id);
CREATE INDEX IF NOT EXISTS idx_absences_etudiant ON absences(etudiant_id);
CREATE INDEX IF NOT EXISTS idx_absences_seance ON absences(seance_id);
CREATE INDEX IF NOT EXISTS idx_paiements_inscription ON paiements(inscription_id);
CREATE INDEX IF NOT EXISTS idx_echeances_echeancier ON echeances(echeancier_id);
CREATE INDEX IF NOT EXISTS idx_echeances_date_limite ON echeances(date_limite);
CREATE INDEX IF NOT EXISTS idx_depenses_annee ON depenses(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_depenses_categorie ON depenses(categorie_id);
CREATE INDEX IF NOT EXISTS idx_depenses_date ON depenses(date_depense);
CREATE INDEX IF NOT EXISTS idx_depenses_statut ON depenses(statut_approbation);
CREATE INDEX IF NOT EXISTS idx_echeances_contrats_contrat ON echeances_contrats(contrat_id);
CREATE INDEX IF NOT EXISTS idx_echeances_contrats_statut ON echeances_contrats(statut, date_echeance);
CREATE INDEX IF NOT EXISTS idx_previsions_annee ON previsions_depenses(annee_scolaire_id, statut);
CREATE INDEX IF NOT EXISTS idx_budgets_annee ON budgets(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_evenements_recurrence ON evenements(est_recurrente, type_recurrence);
CREATE INDEX IF NOT EXISTS idx_evt_instances_annee ON evenements_instances(annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_evt_instances_date ON evenements_instances(date_debut);
CREATE INDEX IF NOT EXISTS idx_evt_instances_statut ON evenements_instances(statut);
CREATE INDEX IF NOT EXISTS idx_notif_user_lu ON notifications(user_id, est_lu);
CREATE INDEX IF NOT EXISTS idx_notif_created ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_actualites_active_date ON actualites(est_active, date_publication DESC);
CREATE INDEX IF NOT EXISTS idx_actualites_categorie ON actualites(categorie);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_table_entite ON audit_log(table_concernee, entite_id);
CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_supports_affectation ON supports_cours(affectation_id);
CREATE INDEX IF NOT EXISTS idx_supports_type_contenu ON supports_cours(type_contenu);
CREATE INDEX IF NOT EXISTS idx_supports_date_limite ON supports_cours(date_limite) WHERE date_limite IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_titulaires_prof ON titulaires_classes(professeur_id);
CREATE INDEX IF NOT EXISTS idx_titulaires_classe_annee ON titulaires_classes(classe_id, annee_scolaire_id);
CREATE INDEX IF NOT EXISTS idx_horaire_edt_ordre ON horaire_edt(ordre);
CREATE INDEX IF NOT EXISTS idx_edt_horaire ON emploi_du_temps(horaire_edt_id);
CREATE INDEX IF NOT EXISTS idx_classes_salle ON classes(salle_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_horaire_edt_unique_global ON horaire_edt (heure_debut, heure_fin) WHERE niveau_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_horaire_edt_unique_level ON horaire_edt (niveau_id, heure_debut, heure_fin) WHERE niveau_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_horaire_edt_niveau_id ON horaire_edt(niveau_id);

-- Index spécifiques ajoutés par 04-07-2026
CREATE INDEX IF NOT EXISTS idx_types_depenses_libelle ON types_depenses(libelle);
CREATE INDEX IF NOT EXISTS idx_depenses_annee_date ON depenses(annee_scolaire_id, date_depense);
CREATE INDEX IF NOT EXISTS idx_depenses_type ON depenses(type_depense_id);
CREATE INDEX IF NOT EXISTS idx_depenses_saisi_par ON depenses(saisi_par);

-- ============================================================
-- 6. VUES
-- ============================================================

DROP VIEW IF EXISTS vue_employes_detail;

CREATE VIEW vue_employes_detail AS
SELECT
    u.id AS user_id,
    u.email,
    u.is_active,
    r.nom AS role_nom,
    COALESCE(pp.nom, pd.nom, ps.nom, pc.nom) AS nom,
    COALESCE(pp.prenom, pd.prenom, ps.prenom, pc.prenom) AS prenom,
    ce.id AS contrat_id,
    ce.reference_contrat,
    ce.fonction,
    COALESCE(ce.sexe, pp.sexe, ps.sexe, pd.sexe, pc.sexe, 'H') AS sexe,
    COALESCE(pp.photo_url, pd.photo_url, ps.photo_url, pc.photo_url) AS photo_url,
    tce.code AS type_contrat_code,
    tce.libelle AS type_contrat_libelle,
    ce.date_debut,
    ce.date_fin,
    ce.document_url,
    ce.salaire_mensuel,
    ce.heures_hebdo,
    pp.id_matiere,
    m.nom AS matiere_nom,
    CASE
        WHEN ce.date_fin IS NULL THEN NULL
        ELSE GREATEST(ce.date_fin - CURRENT_DATE, 0)
    END AS jours_restants
FROM users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id
LEFT JOIN profils_professeurs pp ON pp.user_id = u.id
LEFT JOIN profils_directeurs pd ON pd.user_id = u.id
LEFT JOIN profils_secretariat ps ON ps.user_id = u.id
LEFT JOIN profils_comptables pc ON pc.user_id = u.id
LEFT JOIN contrats_employes ce ON ce.id = COALESCE(pp.id_contrat, pd.id_contrat, ps.id_contrat, pc.id_contrat)
LEFT JOIN types_contrats_employes tce ON tce.id = ce.type_contrat_id
LEFT JOIN matieres m ON m.id = pp.id_matiere
WHERE r.nom NOT IN ('etudiant', 'parent');

-- ============================================================
-- 7. SUPPRESSION DE CONTRAINTES UNIQUES EXISTANTES SUR horaire_edt
--    (pour éviter les conflits avec les nouvelles contraintes)
-- ============================================================
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

COMMIT;