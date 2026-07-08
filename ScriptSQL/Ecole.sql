BEGIN;

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

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS permissions (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(150) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
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

CREATE TABLE IF NOT EXISTS classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,
    capacite_max      INT DEFAULT 40,
    salle_id          INT,
    created_at        TIMESTAMP DEFAULT NOW()
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

CREATE TABLE IF NOT EXISTS matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,
    code             VARCHAR(20),
    created_at       TIMESTAMP DEFAULT NOW()
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
    id_contrat         INT,
    id_matiere         INT,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS profils_directeurs (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    photo_url  VARCHAR(500),
    sexe       CHAR(1),
    id_contrat INT,
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
    id_contrat INT,
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
    id_contrat INT,
    created_at TIMESTAMP DEFAULT NOW()
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

CREATE TABLE IF NOT EXISTS etudiants_parents (
    etudiant_id           INT REFERENCES profils_etudiants(id) ON DELETE CASCADE,
    parent_id             INT REFERENCES profils_parents(id) ON DELETE CASCADE,
    est_contact_principal BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (etudiant_id, parent_id)
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

CREATE TABLE IF NOT EXISTS emploi_du_temps (
    id                  SERIAL PRIMARY KEY,
    affectation_id      INT REFERENCES affectations_enseignement(id),
    salle_id            INT REFERENCES salles(id),
    jour_semaine        INT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    heure_debut         TIME NOT NULL,
    heure_fin           TIME NOT NULL,
    date_debut_validite DATE,
    date_fin_validite   DATE,
    horaire_edt_id      INT,
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

CREATE TABLE IF NOT EXISTS notification_types (
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(100) UNIQUE NOT NULL,
    libelle          VARCHAR(255),
    template_message TEXT
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

CREATE TABLE IF NOT EXISTS types_fichiers (
    id          SERIAL PRIMARY KEY,
    libelle     VARCHAR(100) UNIQUE NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
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

CREATE TABLE IF NOT EXISTS types_contrats_employes (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    duree_mois  INT,
    description TEXT,
    est_actif   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW()
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

CREATE TABLE IF NOT EXISTS horaire_edt (
    id           SERIAL PRIMARY KEY,
    libelle      VARCHAR(100) NOT NULL,
    heure_debut  TIME NOT NULL,
    heure_fin    TIME NOT NULL,
    ordre        INT NOT NULL,
    is_active    BOOLEAN DEFAULT TRUE,
    niveau_id    INT,
    created_at   TIMESTAMP DEFAULT NOW()
);

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

INSERT INTO roles (nom, description) VALUES
    ('super_admin', 'Accès total, gestion technique du système'),
    ('directeur', 'Pilotage pédagogique et financier, validation'),
    ('secretariat', 'Inscriptions, dossiers, finance opérationnelle'),
    ('comptable', 'Finances, paiements, rapports financiers'),
    ('professeur', 'Saisie notes, absences, emploi du temps'),
    ('etudiant', 'Consultation notes, dossier, emploi du temps'),
    ('parent', 'Consultation dossier enfant, notifications');

INSERT INTO notification_types (code, libelle, template_message) VALUES
    ('notes_publiees', 'Notes disponibles', 'Vos notes du {periode} sont maintenant disponibles.'),
    ('baisse_notes_alerte', 'Alerte baisse de notes', 'Votre moyenne en {matiere} a baissé significativement.'),
    ('absence_frequente', 'Absences fréquentes', 'Votre taux d''absence dépasse {seuil}%. Veuillez régulariser.'),
    ('echeance_approchante', 'Échéance de paiement proche', 'Un paiement de {montant} Ar est attendu avant le {date}.'),
    ('edt_modifie', 'Emploi du temps modifié', 'Le cours de {matiere} du {date} a été modifié : {motif}.'),
    ('evenement_confirme', 'Nouvel événement au calendrier', 'L''événement "{titre}" est prévu le {date}.'),
    ('document_disponible', 'Document prêt', 'Votre {type_document} est disponible au téléchargement.'),
    ('depense_a_approuver', 'Dépense en attente d''approbation', 'Une dépense urgente de {montant} Ar attend votre validation.'),
    ('budget_depasse', 'Dépassement budgétaire', 'Le budget "{categorie}" est dépassé de {ecart} Ar.');

INSERT INTO categories_depenses (parent_id, nom, type_charge) VALUES
    (NULL, 'Ressources Humaines', 'fixe'),
    (NULL, 'Infrastructure', 'fixe'),
    (NULL, 'Pédagogie', 'variable'),
    (NULL, 'Administratif', 'variable'),
    (NULL, 'Événements', 'variable');

INSERT INTO types_fichiers (libelle) VALUES
    ('Document PDF'),
    ('Document Word (Docx)'),
    ('Feuille de calcul Excel'),
    ('Présentation (PPTX)'),
    ('Lien Externe (Vidéo/Site Web)'),
    ('Archive compressée (ZIP/RAR)');

INSERT INTO types_contrats_employes (code, libelle, duree_mois, description) VALUES
    ('permanent', 'Permanent', NULL, 'Contrat sans échéance fixe'),
    ('vacataire', 'Vacataire', 12, 'Contrat à durée limitée pour heures ponctuelles'),
    ('contractuel', 'Contractuel', 12, 'Contrat à durée déterminée renouvelable');

ALTER TABLE etablissements
    ADD CONSTRAINT fk_etablissements_directeur
    FOREIGN KEY (directeur_id) REFERENCES profils_directeurs(id) ON DELETE SET NULL;

ALTER TABLE previsions_depenses
    ADD CONSTRAINT fk_prevision_depense
    FOREIGN KEY (depense_id) REFERENCES depenses(id) ON DELETE SET NULL;

ALTER TABLE emploi_du_temps
    ADD CONSTRAINT fk_emploi_du_temps_horaire_edt
    FOREIGN KEY (horaire_edt_id) REFERENCES horaire_edt(id) ON DELETE SET NULL;

ALTER TABLE classes
    ADD CONSTRAINT fk_classes_salle
    FOREIGN KEY (salle_id) REFERENCES salles(id) ON DELETE SET NULL;

ALTER TABLE profils_professeurs
    ADD CONSTRAINT fk_profils_professeurs_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_professeurs
    ADD CONSTRAINT fk_profils_professeurs_id_matiere
    FOREIGN KEY (id_matiere) REFERENCES matieres(id) ON DELETE SET NULL;

ALTER TABLE profils_secretariat
    ADD CONSTRAINT fk_profils_secretariat_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_directeurs
    ADD CONSTRAINT fk_profils_directeurs_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE profils_comptables
    ADD CONSTRAINT fk_profils_comptables_id_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrats_employes(id) ON DELETE SET NULL;

ALTER TABLE horaire_edt
    ADD CONSTRAINT fk_horaire_edt_niveau
    FOREIGN KEY (niveau_id) REFERENCES niveaux(id) ON DELETE CASCADE;

ALTER TABLE profils_professeurs DROP CONSTRAINT IF EXISTS profils_professeurs_sexe_check;
ALTER TABLE profils_professeurs ADD CONSTRAINT profils_professeurs_sexe_check CHECK (sexe IN ('H', 'F'));

ALTER TABLE contrats_employes
    ADD CONSTRAINT ck_contrats_employes_sexe_hf CHECK (sexe IN ('H', 'F'));

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

UPDATE profils_professeurs
SET sexe = CASE
    WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
    ELSE 'H'
END;

UPDATE profils_secretariat
SET sexe = CASE
    WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
    ELSE 'H'
END;

UPDATE profils_directeurs
SET sexe = CASE
    WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
    ELSE 'H'
END;

UPDATE profils_comptables
SET sexe = CASE
    WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F'
    ELSE 'H'
END;

UPDATE contrats_employes ce
SET sexe = CASE
    WHEN UPPER(COALESCE(ce.sexe, '')) = 'F' THEN 'F'
    WHEN UPPER(COALESCE(ce.sexe, '')) IN ('M', 'H') THEN 'H'
    ELSE COALESCE((
        SELECT CASE WHEN UPPER(pp.sexe) = 'F' THEN 'F' ELSE 'H' END
        FROM profils_professeurs pp
        WHERE pp.user_id = ce.user_id
        LIMIT 1
    ), 'H')
END;

UPDATE profils_professeurs p
SET id_contrat = (
    SELECT ce.id
    FROM contrats_employes ce
    WHERE ce.user_id = p.user_id
    ORDER BY ce.id DESC
    LIMIT 1
),
id_matiere = COALESCE(p.id_matiere, (
    SELECT m.id
    FROM matieres m
    WHERE m.nom = p.specialite
    LIMIT 1
))
WHERE p.id_contrat IS NULL;

UPDATE profils_secretariat p
SET id_contrat = (
    SELECT ce.id
    FROM contrats_employes ce
    WHERE ce.user_id = p.user_id
    ORDER BY ce.id DESC
    LIMIT 1
)
WHERE p.id_contrat IS NULL;

UPDATE profils_directeurs p
SET id_contrat = (
    SELECT ce.id
    FROM contrats_employes ce
    WHERE ce.user_id = p.user_id
    ORDER BY ce.id DESC
    LIMIT 1
)
WHERE p.id_contrat IS NULL;

UPDATE profils_comptables p
SET id_contrat = (
    SELECT ce.id
    FROM contrats_employes ce
    WHERE ce.user_id = p.user_id
    ORDER BY ce.id DESC
    LIMIT 1
)
WHERE p.id_contrat IS NULL;

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

WITH fallback_etab AS (
    INSERT INTO etablissements (nom, adresse, telephone, email)
    SELECT 'Lycée Technique de Tananarive', 'Antananarivo, Madagascar', '+261 34 00 000 01', 'contact@lycee-tana.mg'
    WHERE NOT EXISTS (SELECT 1 FROM etablissements)
    RETURNING id
)
SELECT 1;

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
    ('16h00 - 17h00', '16:00', '17:00', 9);

INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active) VALUES
    (1, '2025-2026', '2025-09-01', '2026-07-31', TRUE);

INSERT INTO niveaux (etablissement_id, libelle, ordre) VALUES
    (1, 'Seconde', 1),
    (1, 'Première', 2),
    (1, 'Terminale', 3);

INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max) VALUES
    (1, 1, 'Seconde A', 40),
    (1, 1, 'Seconde B', 40),
    (2, 1, 'Première S', 35),
    (2, 1, 'Première ES', 35),
    (3, 1, 'Terminale A', 30),
    (3, 1, 'Terminale C', 30);

INSERT INTO salles (etablissement_id, nom, capacite, type) VALUES
    (1, 'Salle 101', 40, 'cours'),
    (1, 'Salle 102', 40, 'cours'),
    (1, 'Salle 201', 35, 'cours'),
    (1, 'Salle 202', 35, 'cours'),
    (1, 'Labo Physique', 30, 'laboratoire'),
    (1, 'Labo Chimie', 30, 'laboratoire'),
    (1, 'Salle Informatique', 25, 'laboratoire');

INSERT INTO matieres (etablissement_id, nom, code) VALUES
    (1, 'Mathématiques', 'MATH'),
    (1, 'Physique-Chimie', 'PC'),
    (1, 'Sciences de la Vie et de la Terre', 'SVT'),
    (1, 'Français', 'FRAN'),
    (1, 'Anglais', 'ANGL'),
    (1, 'Histoire-Géographie', 'HIST'),
    (1, 'Philosophie', 'PHIL'),
    (1, 'Éducation Physique et Sportive', 'EPS');

INSERT INTO coefficients (matiere_id, niveau_id, valeur) VALUES
    (1, 1, 4.00), (2, 1, 4.00), (3, 1, 3.00), (4, 1, 3.00), (5, 1, 3.00), (6, 1, 2.00), (8, 1, 2.00),
    (1, 2, 5.00), (2, 2, 5.00), (3, 2, 4.00), (4, 2, 3.00), (5, 2, 3.00), (6, 2, 2.00), (8, 2, 2.00),
    (1, 3, 6.00), (2, 3, 6.00), (3, 3, 5.00), (4, 3, 3.00), (5, 3, 3.00), (7, 3, 4.00), (8, 3, 2.00);

INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes) VALUES
    (1, '1er Trimestre', 'trimestre', 1, '2025-09-01', '2025-11-30', '2025-12-15'),
    (1, '2ème Trimestre', 'trimestre', 2, '2025-12-01', '2026-03-31', '2026-04-15'),
    (1, '3ème Trimestre', 'trimestre', 3, '2026-04-01', '2026-07-31', '2026-08-15');

INSERT INTO users (email, password, is_active) VALUES
    ('prof.rakoto@ecole.mg', '$2y$10$hashed_password_1', TRUE),
    ('prof.rasoa@ecole.mg', '$2y$10$hashed_password_2', TRUE),
    ('prof.andriamanitra@ecole.mg', '$2y$10$hashed_password_3', TRUE),
    ('prof.nirina@ecole.mg', '$2y$10$hashed_password_4', TRUE),
    ('etudiant1@ecole.mg', '$2y$10$hashed_password_5', TRUE),
    ('etudiant2@ecole.mg', '$2y$10$hashed_password_6', TRUE),
    ('etudiant3@ecole.mg', '$2y$10$hashed_password_7', TRUE),
    ('etudiant4@ecole.mg', '$2y$10$hashed_password_8', TRUE),
    ('etudiant5@ecole.mg', '$2y$10$hashed_password_9', TRUE),
    ('etudiant6@ecole.mg', '$2y$10$hashed_password_10', TRUE),
    ('etudiant7@ecole.mg', '$2y$10$hashed_password_11', TRUE),
    ('etudiant8@ecole.mg', '$2y$10$hashed_password_12', TRUE),
    ('jean.rakoto@ecole.mg','etudiant123',true),
    ('marie.rasoa@ecole.mg','etudiant123',true),
    ('paul.andry@ecole.mg','etudiant123',true),
    ('soa.rabe@ecole.mg','etudiant123',true),
    ('hery.rajoana@ecole.mg','etudiant123',true),
    ('luc.rakotoniaina@ecole.mg','etudiant123',true),
    ('aina.randria@ecole.mg','etudiant123',true),
    ('fanja.ravelo@ecole.mg','etudiant123',true),
    ('toky.rabary@ecole.mg','etudiant123',true),
    ('nantenaina.rasoanaivo@ecole.mg','etudiant123',true),
    ('miora.rakotondrazaka@ecole.mg','etudiant123',true),
    ('fitia.ramamonjy@ecole.mg','etudiant123',true),
    ('hasina.ravelomanana@ecole.mg','etudiant123',true),
    ('anto.rakotobe@ecole.mg','etudiant123',true),
    ('tahina.ramanitra@ecole.mg','etudiant123',true),
    ('kiady.rasoanirina@ecole.mg','etudiant123',true),
    ('feno.randrianarisoa@ecole.mg','etudiant123',true),
    ('lalaina.rabeson@ecole.mg','etudiant123',true),
    ('sitraka.ramialison@ecole.mg','etudiant123',true),
    ('mihaja.rakotovao@ecole.mg','etudiant123',true),
    ('onja.rakotonirina@ecole.mg','etudiant123',true),
    ('rinah.randriamampionona@ecole.mg','etudiant123',true),
    ('tiana.raveloson@ecole.mg','etudiant123',true),
    ('andy.rabefitia@ecole.mg','etudiant123',true),
    ('sarobidy.rakotomanga@ecole.mg','etudiant123',true),
    ('prof.test.decrochage@ecole.mg', 'prof123', true),
    ('directeur@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('rakoto@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('prof@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('etudiant@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 5), (2, 5), (3, 5), (4, 5),
    (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (11, 6), (12, 6),
    ((SELECT id FROM users WHERE email='directeur@ecole.mg'), 2),
    ((SELECT id FROM users WHERE email='rakoto@ecole.mg'), 3),
    ((SELECT id FROM users WHERE email='prof@ecole.mg'), 5),
    ((SELECT id FROM users WHERE email='etudiant@ecole.mg'), 6);

INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat) VALUES
    (1, 'PROF001', 'Rakoto', 'Jean', '1980-05-15', 'H', '+261 34 00 001 01', 'Antananarivo', 'Mathématiques', 'permanent', '2015-09-01'),
    (2, 'PROF002', 'Rasoa', 'Marie', '1985-08-20', 'F', '+261 34 00 002 02', 'Antananarivo', 'Physique-Chimie', 'permanent', '2018-09-01'),
    (3, 'PROF003', 'Andriamanitra', 'Paul', '1978-03-10', 'H', '+261 34 00 003 03', 'Antananarivo', 'SVT', 'contractuel', '2020-09-01'),
    (4, 'PROF004', 'Nirina', 'Lucie', '1990-12-25', 'F', '+261 34 00 004 04', 'Antananarivo', 'Français', 'vacataire', '2023-09-01'),
    ((SELECT id FROM users WHERE email='prof.test.decrochage@ecole.mg'), 'PROF_TEST_DECR', 'RAZAFY', 'Michel', NULL, 'H', NULL, NULL, 'Polyvalent', 'permanent', NULL);

INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone) VALUES
    (5, 'ETU001', 'Rasoarimanana', 'Mirana', '2008-02-14', 'Antananarivo', 'F', 'Lot IV 123', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 010 01'),
    (6, 'ETU002', 'Randrianarivony', 'Tiana', '2008-06-22', 'Fianarantsoa', 'M', 'Lot V 456', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 020 02'),
    (7, 'ETU003', 'Rakotobe', 'Niry', '2008-09-30', 'Toamasina', 'F', 'Lot VI 789', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 030 03'),
    (8, 'ETU004', 'Andrianasolo', 'Fidy', '2008-11-11', 'Mahajanga', 'M', 'Lot VII 012', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 040 04'),
    (9, 'ETU005', 'Rasolofomanana', 'Miora', '2007-04-05', 'Antsirabe', 'F', 'Lot VIII 345', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 050 05'),
    (10, 'ETU006', 'Randriamanantena', 'Rado', '2007-07-18', 'Toliara', 'M', 'Lot IX 678', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 060 06'),
    (11, 'ETU007', 'Rakotonirina', 'Lala', '2007-10-25', 'Diego Suarez', 'F', 'Lot X 901', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 070 07'),
    (12, 'ETU008', 'Andriamalala', 'Toky', '2007-01-08', 'Antananarivo', 'M', 'Lot XI 234', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 080 08'),
    ((SELECT id FROM users WHERE email='jean.rakoto@ecole.mg'), 'ETU20240001', 'RAKOTO', 'Jean', '2006-03-15', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234501'),
    ((SELECT id FROM users WHERE email='marie.rasoa@ecole.mg'), 'ETU20240002', 'RASOA', 'Marie', '2006-07-22', 'Fianarantsoa', 'F', NULL, NULL, 'Haute Matsiatra', 'Malgache', '0341234502'),
    ((SELECT id FROM users WHERE email='paul.andry@ecole.mg'), 'ETU20240003', 'ANDRY', 'Paul', '2005-11-08', 'Antsirabe', 'M', NULL, NULL, 'Vakinankaratra', 'Malgache', '0341234503'),
    ((SELECT id FROM users WHERE email='soa.rabe@ecole.mg'), 'ETU20240004', 'RABE', 'Soa', '2006-01-30', 'Antsirabe', 'F', NULL, NULL, 'Vakinankaratra', 'Malgache', '0341234504'),
    ((SELECT id FROM users WHERE email='hery.rajoana@ecole.mg'), 'ETU20240005', 'RAJOANA', 'Hery', '2005-09-12', 'Mahajanga', 'M', NULL, NULL, 'Boeny', 'Malgache', '0341234505'),
    ((SELECT id FROM users WHERE email='luc.rakotoniaina@ecole.mg'), 'ETU20240006', 'RAKOTONIAINA', 'Luc', '2006-02-10', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234506'),
    ((SELECT id FROM users WHERE email='aina.randria@ecole.mg'), 'ETU20240007', 'RANDRIA', 'Aina', '2006-05-11', 'Toamasina', 'F', NULL, NULL, 'Atsinanana', 'Malgache', '0341234507'),
    ((SELECT id FROM users WHERE email='fanja.ravelo@ecole.mg'), 'ETU20240008', 'RAVELO', 'Fanja', '2006-08-12', 'Morondava', 'F', NULL, NULL, 'Menabe', 'Malgache', '0341234508'),
    ((SELECT id FROM users WHERE email='toky.rabary@ecole.mg'), 'ETU20240009', 'RABARY', 'Toky', '2005-10-21', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234509'),
    ((SELECT id FROM users WHERE email='nantenaina.rasoanaivo@ecole.mg'), 'ETU20240010', 'RASOANAIVO', 'Nantenaina', '2005-12-18', 'Antsirabe', 'M', NULL, NULL, 'Vakinankaratra', 'Malgache', '0341234510'),
    ((SELECT id FROM users WHERE email='miora.rakotondrazaka@ecole.mg'), 'ETU20240011', 'RAKOTONDRAZAKA', 'Miora', '2006-03-08', 'Antananarivo', 'F', NULL, NULL, 'Analamanga', 'Malgache', '0341234511'),
    ((SELECT id FROM users WHERE email='fitia.ramamonjy@ecole.mg'), 'ETU20240012', 'RAMAMONJY', 'Fitia', '2006-06-14', 'Fianarantsoa', 'F', NULL, NULL, 'Haute Matsiatra', 'Malgache', '0341234512'),
    ((SELECT id FROM users WHERE email='hasina.ravelomanana@ecole.mg'), 'ETU20240013', 'RAVELOMANANA', 'Hasina', '2005-07-17', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234513'),
    ((SELECT id FROM users WHERE email='anto.rakotobe@ecole.mg'), 'ETU20240014', 'RAKOTOBE', 'Anto', '2006-09-22', 'Mahajanga', 'M', NULL, NULL, 'Boeny', 'Malgache', '0341234514'),
    ((SELECT id FROM users WHERE email='tahina.ramanitra@ecole.mg'), 'ETU20240015', 'RAMANITRA', 'Tahina', '2005-04-03', 'Toamasina', 'F', NULL, NULL, 'Atsinanana', 'Malgache', '0341234515'),
    ((SELECT id FROM users WHERE email='kiady.rasoanirina@ecole.mg'), 'ETU20240016', 'RASOANIRINA', 'Kiady', '2006-11-30', 'Antananarivo', 'F', NULL, NULL, 'Analamanga', 'Malgache', '0341234516'),
    ((SELECT id FROM users WHERE email='feno.randrianarisoa@ecole.mg'), 'ETU20240017', 'RANDRIANARISOA', 'Feno', '2006-01-19', 'Antsirabe', 'M', NULL, NULL, 'Vakinankaratra', 'Malgache', '0341234517'),
    ((SELECT id FROM users WHERE email='lalaina.rabeson@ecole.mg'), 'ETU20240018', 'RABESON', 'Lalaina', '2005-05-05', 'Antananarivo', 'F', NULL, NULL, 'Analamanga', 'Malgache', '0341234518'),
    ((SELECT id FROM users WHERE email='sitraka.ramialison@ecole.mg'), 'ETU20240019', 'RAMIALISON', 'Sitraka', '2005-08-28', 'Fianarantsoa', 'M', NULL, NULL, 'Haute Matsiatra', 'Malgache', '0341234519'),
    ((SELECT id FROM users WHERE email='mihaja.rakotovao@ecole.mg'), 'ETU20240020', 'RAKOTOVAO', 'Mihaja', '2006-02-27', 'Mahajanga', 'M', NULL, NULL, 'Boeny', 'Malgache', '0341234520'),
    ((SELECT id FROM users WHERE email='onja.rakotonirina@ecole.mg'), 'ETU20240021', 'RAKOTONIRINA', 'Onja', '2006-07-01', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234521'),
    ((SELECT id FROM users WHERE email='rinah.randriamampionona@ecole.mg'), 'ETU20240022', 'RANDRIAMAMPIONONA', 'Rinah', '2005-09-13', 'Toamasina', 'F', NULL, NULL, 'Atsinanana', 'Malgache', '0341234522'),
    ((SELECT id FROM users WHERE email='tiana.raveloson@ecole.mg'), 'ETU20240023', 'RAVELOSON', 'Tiana', '2006-04-18', 'Antananarivo', 'M', NULL, NULL, 'Analamanga', 'Malgache', '0341234523'),
    ((SELECT id FROM users WHERE email='andy.rabefitia@ecole.mg'), 'ETU20240024', 'RABEFITIA', 'Andy', '2005-12-09', 'Antsirabe', 'M', NULL, NULL, 'Vakinankaratra', 'Malgache', '0341234524'),
    ((SELECT id FROM users WHERE email='sarobidy.rakotomanga@ecole.mg'), 'ETU20240025', 'RAKOTOMANGA', 'Sarobidy', '2006-10-25', 'Antananarivo', 'F', NULL, NULL, 'Analamanga', 'Malgache', '0341234525');

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut) VALUES
    (1, 1, 1, 'nouvelle', '2025-08-15', 'active'),
    (2, 1, 1, 'nouvelle', '2025-08-15', 'active'),
    (3, 1, 1, 'nouvelle', '2025-08-16', 'active'),
    (4, 1, 1, 'nouvelle', '2025-08-16', 'active'),
    (5, 3, 1, 'reinscription', '2025-08-15', 'active'),
    (6, 3, 1, 'reinscription', '2025-08-15', 'active'),
    (7, 5, 1, 'reinscription', '2025-08-16', 'active'),
    (8, 5, 1, 'reinscription', '2025-08-16', 'active');

INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT
    pe.id,
    1,
    (SELECT id FROM annees_scolaires WHERE est_active = true LIMIT 1),
    'nouvelle',
    CURRENT_DATE,
    'active'
FROM profils_etudiants pe
WHERE pe.matricule BETWEEN 'ETU20240001' AND 'ETU20240025';

INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo) VALUES
    (1, 1, 1, 1, 6.0),
    (1, 1, 3, 1, 6.0),
    (1, 1, 6, 1, 8.0),
    (2, 2, 1, 1, 4.0),
    (2, 2, 3, 1, 5.0),
    (2, 2, 6, 1, 6.0),
    (3, 3, 1, 1, 3.0),
    (3, 3, 3, 1, 4.0),
    (4, 4, 1, 1, 4.0),
    (4, 4, 4, 1, 4.0),
    (4, 4, 5, 1, 4.0);

INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo)
SELECT
    (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR'),
    id,
    2,
    (SELECT id FROM annees_scolaires WHERE est_active = true LIMIT 1),
    2
FROM matieres
WHERE code IN ('MATH', 'FRAN', 'SVT');

INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin) VALUES
    (1, 1, 1, '08:00:00', '10:00:00'),
    (1, 1, 3, '10:00:00', '12:00:00'),
    (2, 3, 2, '08:00:00', '10:00:00'),
    (2, 3, 4, '14:00:00', '16:00:00'),
    (3, 3, 1, '10:00:00', '12:00:00'),
    (3, 3, 3, '08:00:00', '10:00:00'),
    (3, 3, 5, '14:00:00', '16:00:00'),
    (4, 5, 2, '10:00:00', '12:00:00'),
    (4, 5, 4, '08:00:00', '10:00:00'),
    (5, 5, 1, '14:00:00', '16:00:00'),
    (5, 5, 3, '14:00:00', '16:00:00'),
    (6, 5, 2, '14:00:00', '16:00:00'),
    (6, 5, 4, '10:00:00', '12:00:00');

INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin)
SELECT
    id,
    NULL,
    CASE
        WHEN matiere_id = (SELECT id FROM matieres WHERE code = 'MATH') THEN 1
        WHEN matiere_id = (SELECT id FROM matieres WHERE code = 'FRAN') THEN 2
        WHEN matiere_id = (SELECT id FROM matieres WHERE code = 'SVT') THEN 3
    END,
    '08:00:00',
    '10:00:00'
FROM affectations_enseignement
WHERE professeur_id = (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR');

INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu) VALUES
    (1, '2026-01-13', '08:00:00', '10:00:00', TRUE),
    (7, '2026-01-13', '10:00:00', '12:00:00', TRUE),
    (9, '2026-01-13', '14:00:00', '16:00:00', TRUE),
    (8, '2026-01-14', '08:00:00', '10:00:00', TRUE),
    (4, '2026-01-14', '10:00:00', '12:00:00', TRUE),
    (12, '2026-01-14', '14:00:00', '16:00:00', TRUE),
    (10, '2026-01-15', '08:00:00', '10:00:00', TRUE),
    (2, '2026-01-15', '10:00:00', '12:00:00', TRUE),
    (11, '2026-01-15', '14:00:00', '16:00:00', TRUE),
    (5, '2026-01-16', '08:00:00', '10:00:00', TRUE),
    (13, '2026-01-16', '10:00:00', '12:00:00', TRUE),
    (3, '2026-01-16', '14:00:00', '16:00:00', TRUE),
    (6, '2026-01-17', '14:00:00', '16:00:00', TRUE);

INSERT INTO absences (seance_id, etudiant_id, type, motif, saisi_par) VALUES
    (1, 1, 'non_justifiee', NULL, 1),
    (2, 2, 'justifiee', 'Maladie', 1),
    (4, 3, 'retard', 'Transport en panne', 2),
    (9, 4, 'non_justifiee', NULL, 2),
    (11, 5, 'non_justifiee', NULL, 1),
    (13, 6, 'justifiee', 'Raison familiale', 2);

INSERT INTO notes (etudiant_id, affectation_id, periode_id, type_evaluation, valeur, sur, commentaire, saisi_par) VALUES
    (1, 1, 1, 'devoir_1', 15.50, 20.00, 'Bon travail', 1),
    (1, 1, 1, 'devoir_2', 14.00, 20.00, 'À améliorer', 1),
    (1, 1, 1, 'composition', 16.00, 20.00, 'Excellent', 1),
    (2, 1, 1, 'devoir_1', 12.50, 20.00, 'Passable', 1),
    (2, 1, 1, 'devoir_2', 13.00, 20.00, 'En progression', 1),
    (2, 1, 1, 'composition', 14.50, 20.00, 'Bien', 1),
    (3, 1, 1, 'devoir_1', 18.00, 20.00, 'Très bien', 1),
    (3, 1, 1, 'devoir_2', 17.50, 20.00, 'Excellent', 1),
    (3, 1, 1, 'composition', 19.00, 20.00, 'Remarquable', 1),
    (4, 1, 1, 'devoir_1', 10.00, 20.00, 'Insuffisant', 1),
    (4, 1, 1, 'devoir_2', 11.50, 20.00, 'À retravailler', 1),
    (4, 1, 1, 'composition', 12.00, 20.00, 'Peut mieux faire', 1),
    (1, 4, 1, 'devoir_1', 14.00, 20.00, 'Bien', 2),
    (1, 4, 1, 'tp', 16.00, 20.00, 'Très bon TP', 2),
    (1, 4, 1, 'composition', 15.00, 20.00, 'Bon résultat', 2),
    (2, 4, 1, 'devoir_1', 13.00, 20.00, 'Correct', 2),
    (2, 4, 1, 'tp', 14.50, 20.00, 'Bon TP', 2),
    (2, 4, 1, 'composition', 14.00, 20.00, 'Satisfaisant', 2),
    (5, 2, 1, 'devoir_1', 16.50, 20.00, 'Excellent', 1),
    (5, 2, 1, 'devoir_2', 17.00, 20.00, 'Très bien', 1),
    (5, 2, 1, 'composition', 18.00, 20.00, 'Remarquable', 1),
    (6, 2, 1, 'devoir_1', 14.00, 20.00, 'Bien', 1),
    (6, 2, 1, 'devoir_2', 15.00, 20.00, 'Bien', 1),
    (6, 2, 1, 'composition', 15.50, 20.00, 'Très bien', 1),
    (1, 10, 1, 'devoir_1', 13.00, 20.00, 'Correct', 4),
    (1, 10, 1, 'oral', 15.00, 20.00, 'Bonne expression', 4),
    (1, 10, 1, 'composition', 14.50, 20.00, 'Bien', 4),
    (2, 10, 1, 'devoir_1', 14.50, 20.00, 'Bien', 4),
    (2, 10, 1, 'oral', 16.00, 20.00, 'Très bonne expression', 4),
    (2, 10, 1, 'composition', 15.00, 20.00, 'Très bien', 4);

INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, rang, effectif_classe) VALUES
    (1, 1, 1, 1, 15.17, 2, 4),
    (1, 1, 1, 2, 15.00, 2, 4),
    (1, 1, 1, 4, 14.17, 3, 4),
    (2, 2, 1, 1, 13.33, 3, 4),
    (2, 2, 1, 2, 13.83, 3, 4),
    (2, 2, 1, 4, 15.17, 2, 4),
    (3, 3, 1, 1, 18.17, 1, 4),
    (4, 4, 1, 1, 11.17, 4, 4),
    (5, 5, 1, 1, 17.17, 1, 2),
    (6, 6, 1, 1, 14.83, 2, 2);

INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, rang, effectif_classe) VALUES
    (1, 1, 1, NULL, 14.78, 2, 4),
    (2, 2, 1, NULL, 14.11, 3, 4),
    (3, 3, 1, NULL, 18.17, 1, 4),
    (4, 4, 1, NULL, 11.17, 4, 4),
    (5, 5, 1, NULL, 17.17, 1, 2),
    (6, 6, 1, NULL, 14.83, 2, 2);

INSERT INTO evenements (etablissement_id, titre, description, type, est_recurrente, type_recurrence, jour_recurrence, mois_recurrence, duree_jours, heure_debut_defaut, heure_fin_defaut, annule_cours, concerne_toute_ecole) VALUES
    (1, 'Journée de la Rentrée', 'Cérémonie de rentrée scolaire', 'fete', FALSE, NULL, NULL, NULL, 1, '08:00:00', '12:00:00', TRUE, TRUE),
    (1, 'Composition du 1er Trimestre', 'Examen de fin de 1er trimestre', 'examen', FALSE, NULL, NULL, NULL, 3, '08:00:00', '17:00:00', TRUE, TRUE),
    (1, 'Fête de l''Indépendance', 'Célébration de l''indépendance nationale', 'fete', TRUE, 'annuelle', 26, 6, 1, NULL, NULL, TRUE, TRUE),
    (1, 'Conseil de classe - Seconde A', 'Réunion parents-professeurs', 'conseil_classe', FALSE, NULL, NULL, NULL, 1, '17:00:00', '19:00:00', FALSE, FALSE);

INSERT INTO evenements_instances (evenement_id, annee_scolaire_id, classe_id, date_debut, date_fin, heure_debut, heure_fin, statut, notes) VALUES
    (1, 1, NULL, '2025-09-01', NULL, '08:00:00', '12:00:00', 'realise', 'Cérémonie réussie'),
    (2, 1, NULL, '2025-11-25', '2025-11-27', '08:00:00', '17:00:00', 'realise', 'Composition terminée'),
    (3, 1, NULL, '2026-06-26', NULL, NULL, NULL, 'planifie', 'À venir'),
    (4, 1, 1, '2025-12-10', NULL, '17:00:00', '19:00:00', 'realise', 'Présence de 80% des parents');

INSERT INTO notifications (user_id, type_id, titre, message, lien_action, est_lu, entite_type, entite_id) VALUES
    (1, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (2, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (1, 5, 'Emploi du temps modifié', 'Le cours de Mathématiques du 2026-01-20 a été modifié : Salle changée.', '/professeur/calendar', FALSE, 'edt', 1),
    (2, 6, 'Nouvel événement au calendrier', 'L''événement "Composition du 1er Trimestre" est prévu le 2025-11-25.', '/professeur/calendar', TRUE, 'evenement', 2),
    (5, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (6, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (5, 2, 'Alerte baisse de notes', 'Votre moyenne en Mathématiques a baissé significativement.', '/etudiant/notes', FALSE, 'note', 15),
    (7, 3, 'Absences fréquentes', 'Votre taux d''absence dépasse 10%. Veuillez régulariser.', '/etudiant/absences', TRUE, 'absence', 1);

INSERT INTO supports_cours (affectation_id, type_fichier_id, titre, description, fichier_url, type_contenu, date_limite, accepte_retard, cree_par) VALUES
    (1, 1, 'Chapitre 1 : Les nombres réels', 'Introduction aux nombres réels et opérations', '/uploads/maths/seconde/chap1_nombres_reels.pdf', 'lecon', NULL, FALSE, 1),
    (1, 2, 'Exercices sur les équations', 'Série d''exercices sur les équations du premier degré', '/uploads/maths/seconde/exercices_equations.docx', 'exercice', NULL, FALSE, 1),
    (1, 1, 'Devoir maison n°1', 'Devoir à rendre pour le 20 janvier', '/uploads/maths/seconde/dm1.pdf', 'devoir_maison', '2026-01-20 23:59:59', TRUE, 1),
    (4, 1, 'Chapitre 1 : L''atome', 'Structure de l''atome et modèle de Bohr', '/uploads/physique/seconde/chap1_atome.pdf', 'lecon', NULL, FALSE, 2),
    (4, 3, 'Tableau périodique', 'Tableau périodique des éléments', '/uploads/physique/seconde/tableau_periodique.xlsx', 'lecon', NULL, FALSE, 2),
    (4, 1, 'TP n°1 - Mesures de masse', 'Compte-rendu de TP à rendre', '/uploads/physique/seconde/tp1_mesures.pdf', 'exercice', '2026-01-25 23:59:59', FALSE, 2),
    (10, 1, 'Lecture analytique n°1', 'Analyse du texte "Le Horla"', '/uploads/francais/seconde/lecture_horla.pdf', 'lecon', NULL, FALSE, 4),
    (10, 1, 'Devoir maison - Résumé', 'Résumé du roman étudié', '/uploads/francais/seconde/dm_resume.pdf', 'devoir_maison', '2026-01-22 23:59:59', TRUE, 4),
    (2, 1, 'Chapitre 1 : Les polynômes', 'Étude des polynômes du second degré', '/uploads/maths/premiere/chap1_polynomes.pdf', 'lecon', NULL, FALSE, 1),
    (2, 1, 'Composition blanche', 'Sujet de composition blanche', '/uploads/maths/premiere/composition_blanche.pdf', 'exercice', '2026-02-01 23:59:59', FALSE, 1);

INSERT INTO titulaires_classes (professeur_id, classe_id, annee_scolaire_id, date_nomination) VALUES
    (1, 1, 1, '2025-09-01');

UPDATE emploi_du_temps e
SET horaire_edt_id = h.id
FROM horaire_edt h
WHERE e.horaire_edt_id IS NULL
  AND e.heure_debut = h.heure_debut
  AND e.heure_fin = h.heure_fin;

DO $$
DECLARE
    v_annee_id     INT;
    v_annee_debut  DATE;
    v_annee_fin    DATE;
    v_len_jours    INT;
    v_p1_id INT; v_p1_debut DATE; v_p1_fin DATE;
    v_p2_id INT; v_p2_debut DATE; v_p2_fin DATE;
    v_p3_id INT; v_p3_debut DATE; v_p3_fin DATE;
    v_edt_math_id INT;
    v_edt_fran_id INT;
    v_edt_svt_id INT;
    rec        RECORD;
    v_rank     INT;
    v_total_seances INT;
    v_nb_absences   INT;
    v_moy_p1 NUMERIC; v_moy_p2 NUMERIC; v_moy_p3 NUMERIC;
    v_prof_user_id INT;
BEGIN
    SELECT id, date_debut, date_fin INTO v_annee_id, v_annee_debut, v_annee_fin
    FROM annees_scolaires
    WHERE est_active = TRUE
    LIMIT 1;

    IF v_annee_id IS NULL THEN
        RAISE NOTICE 'Aucune année scolaire active trouvée.';
        RETURN;
    END IF;

    v_len_jours := (v_annee_fin - v_annee_debut) / 3;

    v_p1_debut := v_annee_debut;
    v_p1_fin   := v_annee_debut + v_len_jours;
    v_p2_debut := v_p1_fin + 1;
    v_p2_fin   := v_p2_debut + v_len_jours;
    v_p3_debut := v_p2_fin + 1;
    v_p3_fin   := v_annee_fin;

    SELECT id INTO v_p1_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 1;
    IF v_p1_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '1er Trimestre', 'trimestre', 1, v_p1_debut, v_p1_fin, v_p1_fin, true)
        RETURNING id INTO v_p1_id;
    END IF;

    SELECT id INTO v_p2_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 2;
    IF v_p2_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '2ème Trimestre', 'trimestre', 2, v_p2_debut, v_p2_fin, v_p2_fin, true)
        RETURNING id INTO v_p2_id;
    END IF;

    SELECT id INTO v_p3_id FROM periodes WHERE annee_scolaire_id = v_annee_id AND ordre = 3;
    IF v_p3_id IS NULL THEN
        INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes, est_cloturee)
        VALUES (v_annee_id, '3ème Trimestre', 'trimestre', 3, v_p3_debut, v_p3_fin, v_p3_fin, false)
        RETURNING id INTO v_p3_id;
    END IF;

    SELECT date_debut, date_fin INTO v_p1_debut, v_p1_fin FROM periodes WHERE id = v_p1_id;
    SELECT date_debut, date_fin INTO v_p2_debut, v_p2_fin FROM periodes WHERE id = v_p2_id;
    SELECT date_debut, date_fin INTO v_p3_debut, v_p3_fin FROM periodes WHERE id = v_p3_id;

    SELECT id INTO v_prof_user_id FROM users WHERE email = 'prof.test.decrochage@ecole.mg';

    SELECT id INTO v_edt_math_id FROM emploi_du_temps 
    WHERE affectation_id IN (
        SELECT id FROM affectations_enseignement 
        WHERE matiere_id = (SELECT id FROM matieres WHERE code = 'MATH')
        AND professeur_id = (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR')
    ) LIMIT 1;

    SELECT id INTO v_edt_fran_id FROM emploi_du_temps 
    WHERE affectation_id IN (
        SELECT id FROM affectations_enseignement 
        WHERE matiere_id = (SELECT id FROM matieres WHERE code = 'FRAN')
        AND professeur_id = (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR')
    ) LIMIT 1;

    SELECT id INTO v_edt_svt_id FROM emploi_du_temps 
    WHERE affectation_id IN (
        SELECT id FROM affectations_enseignement 
        WHERE matiere_id = (SELECT id FROM matieres WHERE code = 'SVT')
        AND professeur_id = (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR')
    ) LIMIT 1;

    IF v_edt_math_id IS NOT NULL THEN
        INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
        SELECT v_edt_math_id, d::date, '08:00', '10:00', true
        FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
        WHERE NOT EXISTS (
            SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_math_id AND date_seance = d::date
        );
    END IF;

    IF v_edt_fran_id IS NOT NULL THEN
        INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
        SELECT v_edt_fran_id, d::date, '08:00', '10:00', true
        FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
        WHERE NOT EXISTS (
            SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_fran_id AND date_seance = d::date
        );
    END IF;

    IF v_edt_svt_id IS NOT NULL THEN
        INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu)
        SELECT v_edt_svt_id, d::date, '08:00', '10:00', true
        FROM generate_series(v_p1_debut, v_p3_fin, interval '7 days') AS d
        WHERE NOT EXISTS (
            SELECT 1 FROM seances WHERE emploi_du_temps_id = v_edt_svt_id AND date_seance = d::date
        );
    END IF;

    SELECT COUNT(*) INTO v_total_seances
    FROM seances
    WHERE emploi_du_temps_id IN (v_edt_math_id, v_edt_fran_id, v_edt_svt_id);

    v_rank := 0;
    FOR rec IN
        SELECT pe.id AS etudiant_id, i.id AS inscription_id, pe.matricule
        FROM profils_etudiants pe
        JOIN inscriptions i ON i.etudiant_id = pe.id AND i.annee_scolaire_id = v_annee_id
        WHERE pe.matricule BETWEEN 'ETU20240001' AND 'ETU20240025'
        ORDER BY pe.matricule
    LOOP
        v_rank := v_rank + 1;

        IF v_rank BETWEEN 1 AND 5 THEN
            v_moy_p1 := 14.5 - (v_rank * 0.2);
            v_moy_p2 := v_moy_p1 - 1.8;
            v_moy_p3 := v_moy_p2 - 1.7;
            v_nb_absences := round(v_total_seances * (0.18 + (v_rank * 0.02)));
        ELSIF v_rank BETWEEN 6 AND 10 THEN
            v_moy_p1 := 12.0 + (v_rank * 0.1);
            v_moy_p2 := v_moy_p1 - 0.3;
            v_moy_p3 := v_moy_p2 - 0.3;
            v_nb_absences := round(v_total_seances * (0.16 + ((v_rank - 5) * 0.015)));
        ELSIF v_rank BETWEEN 11 AND 15 THEN
            v_moy_p1 := 15.5 - ((v_rank - 10) * 0.1);
            v_moy_p2 := v_moy_p1 - 1.4;
            v_moy_p3 := v_moy_p2 - 1.4;
            v_nb_absences := round(v_total_seances * (0.03 + ((v_rank - 10) * 0.005)));
        ELSE
            v_moy_p1 := 11.0 + ((v_rank - 15) * 0.3);
            v_moy_p2 := v_moy_p1 + 0.2;
            v_moy_p3 := v_moy_p2 + 0.3;
            v_nb_absences := round(v_total_seances * (0.02 + ((v_rank - 15) * 0.004)));
        END IF;

        v_moy_p1 := GREATEST(LEAST(v_moy_p1, 20), 0);
        v_moy_p2 := GREATEST(LEAST(v_moy_p2, 20), 0);
        v_moy_p3 := GREATEST(LEAST(v_moy_p3, 20), 0);
        v_nb_absences := GREATEST(LEAST(v_nb_absences, v_total_seances), 0);

        INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, effectif_classe)
        VALUES
            (rec.etudiant_id, rec.inscription_id, v_p1_id, NULL, ROUND(v_moy_p1, 2), 25),
            (rec.etudiant_id, rec.inscription_id, v_p2_id, NULL, ROUND(v_moy_p2, 2), 25),
            (rec.etudiant_id, rec.inscription_id, v_p3_id, NULL, ROUND(v_moy_p3, 2), 25);

        IF v_edt_math_id IS NOT NULL AND v_edt_fran_id IS NOT NULL AND v_edt_svt_id IS NOT NULL AND v_prof_user_id IS NOT NULL THEN
            INSERT INTO absences (seance_id, etudiant_id, type, saisi_par)
            SELECT s.id, rec.etudiant_id, 'non_justifiee', v_prof_user_id
            FROM seances s
            WHERE s.emploi_du_temps_id IN (v_edt_math_id, v_edt_fran_id, v_edt_svt_id)
              AND NOT EXISTS (
                  SELECT 1 FROM absences a WHERE a.seance_id = s.id AND a.etudiant_id = rec.etudiant_id
              )
            ORDER BY s.date_seance, s.id
            LIMIT v_nb_absences;
        END IF;

    END LOOP;

END $$;

COMMIT;
