-- Active: 1773507358543@@127.0.0.1@5432@ecole
-- ============================================================
--  SCHÉMA COMPLET — GESTION D'ÉCOLE
--  Base de données : PostgreSQL
--  Framework      : CodeIgniter 4 (PHP)
--  Équipes        : Étudiant | Professeur | Secrétariat | Directeur
--
--  Convention de nommage :
--    • Tables     : snake_case, pluriel
--    • PK         : toujours "id SERIAL PRIMARY KEY"
--    • FK         : <table_singulier>_id
--    • Timestamps : created_at / updated_at (DEFAULT NOW())
--    • Soft delete : is_archived BOOLEAN (préféré au DELETE physique)
--
--  Modules inclus :
--    ✔ Authentification & rôles
--    ✔ Structure de l'établissement
--    ✔ Profils de tous les acteurs
--    ✔ Inscriptions & scolarité
--    ✔ Affectations d'enseignement
--    ✔ Emploi du temps (récurrence + modifications)
--    ✔ Séances & absences
--    ✔ Notes & moyennes
--    ✔ Finance — recettes écolages
--    ✔ Finance — dépenses réelles + prévisions + budgets
--    ✔ Événements ponctuels & récurrents
--    ✔ Notifications
--    ✔ Documents générés
--    ✔ Workflow modifications dossier
--    ✔ Journal d'audit
-- ============================================================


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


-- ============================================================
-- SECTION 2 — STRUCTURE DE L'ÉTABLISSEMENT
-- Tables de référence partagées par toutes les équipes.
-- Conçu pour supporter plusieurs établissements (futur).
-- ============================================================

-- L'établissement scolaire principal
-- directeur_id est ajouté après création de profils_directeurs
-- pour éviter la référence circulaire entre les deux tables.
CREATE TABLE etablissements (
    id           SERIAL PRIMARY KEY,
    nom          VARCHAR(255) NOT NULL,
    adresse      TEXT,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    logo_url     VARCHAR(500),
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Années scolaires (ex : "2024-2025")
-- Une seule peut être marquée "active" à la fois, contrôlé applicativement
CREATE TABLE annees_scolaires (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(50) NOT NULL,          -- ex : '2024-2025'
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    est_active       BOOLEAN DEFAULT FALSE,          -- l'année scolaire en cours
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Niveaux d'enseignement (ex : Seconde, Première, Terminale)
-- Le champ "ordre" permet d'afficher les niveaux du plus bas au plus haut
CREATE TABLE niveaux (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(100) NOT NULL,
    ordre            INT NOT NULL,                   -- tri croissant : 1=Seconde, 2=Première…
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Classes (ex : "Terminale A 2024-2025")
-- Une classe est l'instance d'un niveau pour une année donnée
CREATE TABLE classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,         -- ex : 'Terminale A', '1ère S'
    capacite_max      INT DEFAULT 40,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- Salles de cours disponibles dans l'établissement
CREATE TABLE salles (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(100) NOT NULL,           -- ex : 'Salle 12', 'Labo Chimie'
    capacite         INT,
    type             VARCHAR(50) DEFAULT 'cours',     -- 'cours', 'laboratoire', 'amphi', 'sport'
    is_active        BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT NOW()
);

-- Matières enseignées dans l'établissement
CREATE TABLE matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,           -- ex : 'Mathématiques', 'Français'
    code             VARCHAR(20),                     -- ex : 'MATH', 'FRAN', 'SVT'
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Coefficients d'une matière selon le niveau
-- Le coeff varie selon le niveau (ex : Maths coeff 4 en Terminale, 3 en Première)
-- Indispensable pour le calcul correct des moyennes pondérées
CREATE TABLE coefficients (
    id         SERIAL PRIMARY KEY,
    matiere_id INT REFERENCES matieres(id) ON DELETE CASCADE,
    niveau_id  INT REFERENCES niveaux(id)  ON DELETE CASCADE,
    valeur     NUMERIC(4,2) NOT NULL,                 -- ex : 4.00, 3.00, 1.50
    UNIQUE (matiere_id, niveau_id)
);

-- Périodes d'évaluation (trimestres ou semestres selon l'école)
-- date_publication_notes : avant cette date, les élèves ne voient pas leurs notes
CREATE TABLE periodes (
    id                     SERIAL PRIMARY KEY,
    annee_scolaire_id      INT REFERENCES annees_scolaires(id),
    libelle                VARCHAR(100) NOT NULL,     -- ex : '1er Trimestre', '2ème Semestre'
    type                   VARCHAR(20) DEFAULT 'trimestre',  -- 'trimestre' | 'semestre'
    ordre                  INT NOT NULL,              -- 1, 2 ou 3
    date_debut             DATE,
    date_fin               DATE,
    date_publication_notes DATE,                      -- date de visibilité des notes pour les élèves
    est_cloturee           BOOLEAN DEFAULT FALSE      -- TRUE = plus aucune saisie/correction possible
);


-- ============================================================
-- SECTION 3 — PROFILS DES ACTEURS
-- Chaque acteur a son propre profil lié à un compte "users".
-- La séparation des profils permet de n'exposer que les colonnes
-- nécessaires à chaque équipe sans tout mettre dans "users".
-- ============================================================

-- Profil complet d'un étudiant
-- La colonne "region" est importante : elle sert aux critères géographiques
CREATE TABLE profils_etudiants (
    id             SERIAL PRIMARY KEY,
    user_id        INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    matricule      VARCHAR(100) UNIQUE NOT NULL,      -- identifiant unique interne à l'école
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
    cin            VARCHAR(50),                       -- Carte d'Identité Nationale (si majeur 18+)
    telephone      VARCHAR(50),
    is_archived    BOOLEAN DEFAULT FALSE,             -- archivé après fin de scolarité, jamais supprimé
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

-- Profil complet d'un professeur
-- type_contrat détermine si le prof est permanent ou temporaire
CREATE TABLE profils_professeurs (
    id                 SERIAL PRIMARY KEY,
    user_id            INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    matricule          VARCHAR(100) UNIQUE NOT NULL,
    nom                VARCHAR(150) NOT NULL,
    prenom             VARCHAR(150) NOT NULL,
    date_naissance     DATE,
    sexe               CHAR(1) CHECK (sexe IN ('M', 'F')),
    photo_url          VARCHAR(500),
    telephone          VARCHAR(50),
    adresse            TEXT,
    specialite         VARCHAR(200),                  -- spécialité académique principale
    type_contrat       VARCHAR(50),                   -- 'permanent', 'vacataire', 'contractuel'
    date_debut_contrat DATE,
    date_fin_contrat   DATE,                          -- NULL si contrat sans terme (permanent)
    is_archived        BOOLEAN DEFAULT FALSE,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP DEFAULT NOW()
);

-- Profil directeur
CREATE TABLE profils_directeurs (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    photo_url  VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Ajout du directeur sur l'établissement
-- Différé ici pour éviter la référence circulaire avec profils_directeurs
ALTER TABLE etablissements
    ADD COLUMN directeur_id INT REFERENCES profils_directeurs(id) ON DELETE SET NULL;

-- Profil secrétariat (peut y avoir plusieurs secrétaires dans un établissement)
CREATE TABLE profils_secretariat (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Profil comptable (peut être la même personne que le secrétariat ou séparé)
CREATE TABLE profils_comptables (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Profil parent ou tuteur légal
-- user_id peut être NULL : le parent n'est pas obligé d'avoir un compte
CREATE TABLE profils_parents (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE SET NULL,
    nom          VARCHAR(150) NOT NULL,
    prenom       VARCHAR(150) NOT NULL,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    profession   VARCHAR(200),
    lien_parente VARCHAR(100),                        -- 'père', 'mère', 'tuteur', 'grand-parent'
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Liaison étudiant ↔ parents/tuteurs (un étudiant peut avoir plusieurs tuteurs)
CREATE TABLE etudiants_parents (
    etudiant_id           INT REFERENCES profils_etudiants(id) ON DELETE CASCADE,
    parent_id             INT REFERENCES profils_parents(id)   ON DELETE CASCADE,
    est_contact_principal BOOLEAN DEFAULT FALSE,      -- un seul doit être TRUE par étudiant
    PRIMARY KEY (etudiant_id, parent_id)
);


-- ============================================================
-- SECTION 4 — INSCRIPTIONS & SCOLARITÉ
-- Table pivot centrale : étudiant → classe → année scolaire.
-- Notes, paiements et absences se rattachent tous à une inscription.
-- ============================================================

-- Inscription annuelle d'un étudiant dans une classe
-- UNIQUE sur (etudiant_id, annee_scolaire_id) : un dossier par an maximum
CREATE TABLE inscriptions (
    id                SERIAL PRIMARY KEY,
    etudiant_id       INT REFERENCES profils_etudiants(id),
    classe_id         INT REFERENCES classes(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    type_inscription  VARCHAR(50) DEFAULT 'reinscription',  -- 'nouvelle' | 'reinscription'
    date_inscription  DATE        DEFAULT CURRENT_DATE,
    statut            VARCHAR(50) DEFAULT 'active',
    -- 'active', 'transfere', 'exclu', 'diplome', 'abandonne'
    rang_final        INT,                             -- calculé et stocké en fin d'année
    est_admis         BOOLEAN,                         -- résultat de passage en classe supérieure
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, annee_scolaire_id)
);


-- ============================================================
-- SECTION 5 — AFFECTATIONS D'ENSEIGNEMENT
-- Définit qui enseigne quoi, dans quelle classe, pour quelle année.
-- C'est la base de l'emploi du temps et de la saisie des notes.
-- UNIQUE sur (matiere, classe, annee) : une matière = un seul prof par classe par an
-- ============================================================

CREATE TABLE affectations_enseignement (
    id                SERIAL PRIMARY KEY,
    professeur_id     INT REFERENCES profils_professeurs(id),
    matiere_id        INT REFERENCES matieres(id),
    classe_id         INT REFERENCES classes(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    heures_hebdo      NUMERIC(4,1),                   -- volume horaire hebdomadaire dans cette classe
    created_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (matiere_id, classe_id, annee_scolaire_id)
);


-- ============================================================
-- SECTION 6 — EMPLOI DU TEMPS
-- Récurrence hebdomadaire + gestion des modifications.
--
-- Logique de récurrence :
--   emploi_du_temps stocke la RÈGLE (ex : "chaque lundi 08h-10h")
--   date_debut_validite / date_fin_validite délimitent la période
--   de validité d'une règle — si la salle change à partir du 15 mars,
--   on ferme la règle actuelle (date_fin_validite = 14 mars) et on
--   crée une nouvelle règle identique avec la nouvelle salle.
--
-- Logique des modifications :
--   portee = 'ponctuel'  → exception sur UNE date précise uniquement
--   portee = 'permanent' → déclenche la fermeture + recréation de règle
-- ============================================================

-- Règles de cours récurrentes hebdomadaires
CREATE TABLE emploi_du_temps (
    id                  SERIAL PRIMARY KEY,
    affectation_id      INT REFERENCES affectations_enseignement(id),
    salle_id            INT REFERENCES salles(id),
    jour_semaine        INT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    -- 1=Lundi, 2=Mardi, 3=Mercredi, 4=Jeudi, 5=Vendredi, 6=Samedi
    heure_debut         TIME NOT NULL,
    heure_fin           TIME NOT NULL,
    date_debut_validite DATE,                          -- NULL = depuis le début de l'année scolaire
    date_fin_validite   DATE,                          -- NULL = jusqu'à la fin de l'année scolaire
    created_at          TIMESTAMP DEFAULT NOW()
);

-- Modifications ponctuelles ou permanentes d'un créneau
-- portee = 'ponctuel'  : ne touche que la date_concernee
-- portee = 'permanent' : le code applicatif doit fermer l'ancienne règle
--                        et en créer une nouvelle à partir de date_concernee
CREATE TABLE modifications_edt (
    id                   SERIAL PRIMARY KEY,
    emploi_du_temps_id   INT REFERENCES emploi_du_temps(id),
    date_concernee       DATE NOT NULL,                -- la date exacte du cours impacté
    portee               VARCHAR(20) DEFAULT 'ponctuel',
    -- 'ponctuel'  : exception sur ce seul jour
    -- 'permanent' : changement définitif à partir de ce jour
    type_modification    VARCHAR(50) NOT NULL,
    -- 'annulation'         : cours supprimé ce jour
    -- 'deplacement_horaire': heure changée
    -- 'changement_salle'   : salle changée
    -- 'remplacement_prof'  : prof remplacé
    motif                VARCHAR(500),
    nouvelle_salle_id    INT REFERENCES salles(id),    -- rempli si changement de salle
    nouvelle_heure_debut TIME,                         -- rempli si déplacement horaire
    nouvelle_heure_fin   TIME,
    remplacant_id        INT REFERENCES profils_professeurs(id),  -- rempli si remplacement
    cree_par             INT REFERENCES users(id),
    created_at           TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 7 — SÉANCES & ABSENCES
-- Séances : instanciation concrète de chaque créneau EDT pour
-- un jour précis, nécessaire pour attacher un pointage réel.
-- Absences : un enregistrement par étudiant absent par séance.
-- ============================================================

-- Chaque occurrence réelle d'un créneau (générée en début d'année ou à la volée)
-- a_eu_lieu = FALSE si le cours est annulé (prof absent, événement, etc.)
CREATE TABLE seances (
    id                 SERIAL PRIMARY KEY,
    emploi_du_temps_id INT REFERENCES emploi_du_temps(id),
    date_seance        DATE NOT NULL,
    heure_debut        TIME,
    heure_fin          TIME,
    a_eu_lieu          BOOLEAN DEFAULT TRUE,           -- FALSE = cours annulé
    created_at         TIMESTAMP DEFAULT NOW()
);

-- Pointage des absences par étudiant pour chaque séance
-- UNIQUE sur (seance_id, etudiant_id) : un seul enregistrement par élève par cours
CREATE TABLE absences (
    id               SERIAL PRIMARY KEY,
    seance_id        INT REFERENCES seances(id),
    etudiant_id      INT REFERENCES profils_etudiants(id),
    type             VARCHAR(50) DEFAULT 'non_justifiee',
    -- 'non_justifiee', 'justifiee', 'retard'
    motif            TEXT,                             -- obligatoire si type = 'justifiee'
    justificatif_url VARCHAR(500),                    -- scan du justificatif fourni
    saisi_par        INT REFERENCES users(id),         -- professeur qui fait le pointage
    valide_par       INT REFERENCES users(id),         -- secrétariat qui valide la justification
    date_validation  TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    UNIQUE (seance_id, etudiant_id)
);


-- ============================================================
-- SECTION 8 — NOTES & MOYENNES
-- Équipe Professeur : saisie et correction de notes.
-- Équipe Étudiant   : lecture, graphiques, trajectoire.
--
-- Dénormalisation intentionnelle dans "moyennes" :
--   Stocker les moyennes calculées évite de les recalculer à chaque
--   affichage. Elles sont invalidées et recalculées après chaque
--   saisie ou correction de note.
-- ============================================================

-- Notes individuelles — chaque devoir/composition = une ligne
-- "sur" permet des notes sur 10 ou sur 100 si l'école le souhaite
CREATE TABLE notes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    affectation_id  INT REFERENCES affectations_enseignement(id),
    -- affectation_id regroupe : matière + classe + professeur + année
    periode_id      INT REFERENCES periodes(id),
    type_evaluation VARCHAR(100),
    -- 'devoir_1', 'devoir_2', 'composition', 'examen_blanc', 'oral', 'tp'
    valeur          NUMERIC(5,2) NOT NULL CHECK (valeur >= 0),
    sur             NUMERIC(5,2) DEFAULT 20.00,        -- note sur X (défaut /20)
    commentaire     TEXT,
    -- Traçabilité de la saisie initiale
    saisi_par       INT REFERENCES users(id),          -- le professeur
    date_saisie     TIMESTAMP DEFAULT NOW(),
    est_valide      BOOLEAN DEFAULT TRUE,
    -- Traçabilité des corrections (nécessite validation secrétariat)
    ancienne_valeur NUMERIC(5,2),                     -- valeur avant correction
    corrige_par     INT REFERENCES users(id),
    date_correction TIMESTAMP,
    motif_correction TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- Moyennes stockées (dénormalisation pour performance)
-- periode_id NULL  → moyenne annuelle
-- matiere_id NULL  → moyenne générale toutes matières confondues
-- rang + effectif_classe permettent d'afficher "5ème sur 32 élèves"
CREATE TABLE moyennes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    inscription_id  INT REFERENCES inscriptions(id),
    periode_id      INT REFERENCES periodes(id),       -- NULL = moyenne annuelle
    matiere_id      INT REFERENCES matieres(id),       -- NULL = moyenne générale
    valeur          NUMERIC(5,2),
    rang            INT,                               -- rang dans la classe, ex : 5
    effectif_classe INT,                               -- nb élèves dans la classe, ex : 32
    calculated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, inscription_id, periode_id, matiere_id)
);


-- ============================================================
-- SECTION 9 — FINANCE : RECETTES (ÉCOLAGES)
-- Ce que l'école attend de recevoir des familles.
-- Flux : grille tarifaire → échéancier → échéances → paiements reçus
-- ============================================================

-- Tarifs annuels par niveau (définis en début d'année par le directeur)
-- Exemple : Terminale 2024-2025 = 1 000 000 Ar
CREATE TABLE grilles_tarifaires (
    id                SERIAL PRIMARY KEY,
    etablissement_id  INT REFERENCES etablissements(id),
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    montant_total     NUMERIC(12,2) NOT NULL,           -- frais annuels bruts
    description       TEXT,
    created_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (niveau_id, annee_scolaire_id)
);

-- Plan de paiement assigné à un étudiant pour son inscription
-- Définit si l'étudiant paye comptant ou en plusieurs tranches
CREATE TABLE echeanciers (
    id             SERIAL PRIMARY KEY,
    inscription_id INT REFERENCES inscriptions(id),
    grille_id      INT REFERENCES grilles_tarifaires(id),
    type           VARCHAR(50),
    -- 'comptant', 'echelonne_2', 'echelonne_3', 'personnalise'
    montant_total  NUMERIC(12,2),                      -- peut différer de la grille (remise accordée)
    created_at     TIMESTAMP DEFAULT NOW()
);

-- Tranches individuelles d'un plan de paiement
-- est_soldee = TRUE quand la somme des paiements couvre montant_attendu
CREATE TABLE echeances (
    id               SERIAL PRIMARY KEY,
    echeancier_id    INT REFERENCES echeanciers(id) ON DELETE CASCADE,
    numero_tranche   INT NOT NULL,                     -- 1, 2, 3…
    montant_attendu  NUMERIC(12,2) NOT NULL,
    date_limite      DATE NOT NULL,
    est_soldee       BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Versements réels effectués par les familles
-- Plusieurs paiements peuvent couvrir une même échéance (paiement partiel)
CREATE TABLE paiements (
    id                    SERIAL PRIMARY KEY,
    echeance_id           INT REFERENCES echeances(id),
    inscription_id        INT REFERENCES inscriptions(id),
    montant               NUMERIC(12,2) NOT NULL,
    date_paiement         DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    -- 'especes', 'virement', 'mvola', 'orange_money', 'cheque'
    reference_transaction VARCHAR(200),                -- numéro de reçu ou de transaction
    saisi_par             INT REFERENCES users(id),    -- secrétaire ou comptable
    notes                 TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 10 — FINANCE : DÉPENSES & PRÉVISIONS
-- Ce que l'école dépense ou prévoit de dépenser.
--
-- Architecture :
--   categories_depenses  → arborescence de classification
--   fournisseurs         → prestataires et créanciers
--   contrats_charges     → obligations contractuelles récurrentes (loyer, salaires…)
--   echeances_contrats   → tranches générées automatiquement par les contrats
--   previsions_depenses  → dépenses planifiées non contractuelles (variables, urgentes)
--   depenses             → toutes les sorties d'argent réelles (fixes + variables + urgentes)
--   budgets              → enveloppes prévisionnelles par catégorie et par année
-- ============================================================

-- Arborescence des catégories de dépenses
-- parent_id NULL = catégorie racine
-- Exemples racines : 'Ressources Humaines', 'Infrastructure', 'Pédagogie', 'Administratif'
-- Exemples enfants : 'Salaires', 'Charges sociales', 'Loyer', 'Électricité', 'Fournitures'
CREATE TABLE categories_depenses (
    id          SERIAL PRIMARY KEY,
    parent_id   INT REFERENCES categories_depenses(id) ON DELETE SET NULL,
    nom         VARCHAR(150) NOT NULL,
    type_charge VARCHAR(20) DEFAULT 'variable',
    -- 'fixe'     : montant stable revenant régulièrement (loyer, salaire)
    -- 'variable' : montant fluctuant ou ponctuel (fournitures, réparation)
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Fournisseurs, prestataires et créanciers de l'école
CREATE TABLE fournisseurs (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(255) NOT NULL,            -- ex : 'JIRAMA', 'Imprimerie Centrale'
    type             VARCHAR(100),
    -- 'utilite'           : eau, électricité, téléphone
    -- 'bailleur'          : propriétaire du bâtiment
    -- 'prestataire'       : services divers
    -- 'fournisseur_materiel' : papeterie, informatique
    -- 'assurance'
    contact_nom      VARCHAR(200),
    telephone        VARCHAR(50),
    email            VARCHAR(255),
    adresse          TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Contrats de charges récurrentes (engagements contractuels)
-- Représente l'OBLIGATION de payer, pas encore le paiement réel.
-- Le système génère automatiquement les écheances_contrats à partir de ces règles.
--
-- Exemples :
--   'Loyer Bâtiment Principal' — mensuel — 500 000 Ar — le 01 du mois
--   'Salaire Prof Rakoto'      — mensuel — 800 000 Ar — le 30 du mois
--   'Abonnement JIRAMA'        — mensuel — 150 000 Ar — le 15 du mois
--   'Maintenance photocopieur' — trimestriel — 200 000 Ar
CREATE TABLE contrats_charges (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    fournisseur_id   INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    categorie_id     INT REFERENCES categories_depenses(id),
    intitule         VARCHAR(255) NOT NULL,
    description      TEXT,
    type_recurrence  VARCHAR(50) NOT NULL,
    -- 'mensuel', 'trimestriel', 'semestriel', 'annuel'
    montant_prevu    NUMERIC(12,2) NOT NULL,            -- montant attendu à chaque occurrence
    jour_echeance    INT,
    -- Pour 'mensuel' : jour du mois (ex : 30 = fin du mois, 1 = début)
    -- Pour les autres fréquences : ce champ est ignoré, géré dans echeances_contrats
    date_debut       DATE NOT NULL,                    -- date d'entrée en vigueur du contrat
    date_fin         DATE,                             -- NULL si contrat sans terme défini
    statut           VARCHAR(50) DEFAULT 'actif',      -- 'actif', 'suspendu', 'resilie'
    numero_contrat   VARCHAR(150),                     -- référence du document contractuel
    document_url     VARCHAR(500),                     -- scan ou chemin vers le contrat signé
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- Échéances générées automatiquement à partir des contrats récurrents
-- Créées en début d'année (ou de mois) par un job planifié CI4
-- periode_concernee : libellé lisible ex 'Mai 2025', '2ème trimestre 2025'
CREATE TABLE echeances_contrats (
    id               SERIAL PRIMARY KEY,
    contrat_id       INT REFERENCES contrats_charges(id) ON DELETE CASCADE,
    periode_concernee VARCHAR(50) NOT NULL,
    date_echeance    DATE NOT NULL,
    montant_prevu    NUMERIC(12,2) NOT NULL,            -- copié du contrat, peut être révisé
    statut           VARCHAR(50) DEFAULT 'en_attente',
    -- 'en_attente', 'payee', 'en_retard', 'annulee'
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Prévisions de dépenses non contractuelles (planifiées à l'avance)
-- Permet d'anticiper les sorties d'argent variables avant qu'elles soient réalisées.
-- Une fois la dépense effectuée, depense_id est renseigné pour lier les deux.
--
-- Exemples :
--   'Achat fournitures rentrée'      — variable  — planifiée en Août
--   'Organisation cérémonie diplôme' — variable  — planifiée en Juin
--   'Réparation terrain sport'       — variable  — planifiée en Avril
CREATE TABLE previsions_depenses (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id     INT REFERENCES categories_depenses(id),
    fournisseur_id   INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    intitule         VARCHAR(255) NOT NULL,
    description      TEXT,
    montant_estime   NUMERIC(12,2) NOT NULL,            -- estimation du coût
    date_prevue      DATE NOT NULL,                    -- quand la dépense est attendue
    type_charge      VARCHAR(20) NOT NULL,
    -- 'variable' : dépense planifiée non contractuelle
    -- 'urgente'  : besoin imprévu identifié en avance (ex : équipement en panne)
    statut           VARCHAR(50) DEFAULT 'planifiee',
    -- 'planifiee'  : prévue mais pas encore approuvée
    -- 'approuvee'  : validée par le directeur, en attente de réalisation
    -- 'realisee'   : dépense effectuée (depense_id renseigné)
    -- 'annulee'    : prévision abandonnée
    approuve_par     INT REFERENCES users(id),          -- directeur qui approuve
    date_approbation TIMESTAMP,
    depense_id       INT,                              -- FK vers depenses (ajoutée après)
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- Dépenses réelles effectuées (toutes natures confondues)
-- type_charge détermine l'origine :
--   'fixe'     → liée à un contrat (echeance_contrat_id renseigné)
--   'variable' → ponctuelle planifiée (prevision_id peut être renseigné)
--   'urgente'  → imprévu nécessitant approbation directeur
-- Les dépenses urgentes ou dépassant un seuil passent par un workflow d'approbation.
CREATE TABLE depenses (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    annee_scolaire_id     INT REFERENCES annees_scolaires(id),
    categorie_id          INT REFERENCES categories_depenses(id),
    fournisseur_id        INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    contrat_id            INT REFERENCES contrats_charges(id),          -- NULL si non contractuel
    echeance_contrat_id   INT REFERENCES echeances_contrats(id),        -- NULL si non contractuel
    prevision_id          INT REFERENCES previsions_depenses(id),       -- NULL si non planifiée
    intitule              VARCHAR(255) NOT NULL,
    -- ex : 'Salaire Mai 2025 - Prof Rakoto', 'Facture JIRAMA Avril', 'Réparation toiture'
    type_charge           VARCHAR(20) NOT NULL,
    -- 'fixe', 'variable', 'urgente'
    motif                 TEXT,                                         -- obligatoire si 'urgente'
    montant               NUMERIC(12,2) NOT NULL,
    date_depense          DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    -- 'especes', 'virement', 'cheque', 'mobile_money'
    reference             VARCHAR(200),                                 -- numéro de virement ou reçu
    justificatif_url      VARCHAR(500),                                 -- scan de la facture/reçu
    -- Workflow d'approbation (activé si dépense urgente ou montant > seuil)
    necessite_approbation BOOLEAN DEFAULT FALSE,
    statut_approbation    VARCHAR(50) DEFAULT 'approuvee',
    -- 'en_attente', 'approuvee', 'refusee'
    approuve_par          INT REFERENCES users(id),                     -- directeur
    date_approbation      TIMESTAMP,
    saisi_par             INT REFERENCES users(id),                     -- comptable / secrétariat
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

-- Liaison retour : previsions_depenses.depense_id → depenses.id
-- Ajoutée ici pour éviter la référence circulaire entre les deux tables
ALTER TABLE previsions_depenses
    ADD CONSTRAINT fk_prevision_depense
    FOREIGN KEY (depense_id) REFERENCES depenses(id) ON DELETE SET NULL;

-- Budgets prévisionnels par catégorie et par année
-- Permet au directeur de fixer des enveloppes et de suivre le dépassement
-- Requête type : SELECT montant_prevu - SUM(depenses.montant) AS solde_restant ...
CREATE TABLE budgets (
    id                SERIAL PRIMARY KEY,
    etablissement_id  INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id      INT REFERENCES categories_depenses(id),
    montant_prevu     NUMERIC(12,2) NOT NULL,           -- enveloppe allouée pour l'année
    created_by        INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (annee_scolaire_id, categorie_id)
);


-- ============================================================
-- SECTION 11 — ÉVÉNEMENTS
-- Événements ponctuels ou récurrents apparaissant dans le calendrier
-- et/ou l'emploi du temps.
--
-- Architecture en deux tables :
--   evenements          → le MODÈLE (définition + règle de récurrence)
--   evenements_instances → les OCCURRENCES concrètes (une par an pour les récurrents)
--
-- Récurrence supportée :
--   'aucune'  : événement unique, une seule instance générée
--   'annuelle': se répète chaque année le même jour et mois
--               (ex : Journée nationale le 26 juin chaque année)
--
-- Le code applicatif génère les instances de l'année active
-- au début de chaque année scolaire pour les modèles récurrents.
-- ============================================================

-- Modèle d'événement (template)
-- Pour un événement non récurrent, une seule instance sera créée.
-- Pour un récurrent annuel, une instance est générée chaque année scolaire.
CREATE TABLE evenements (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    titre                 VARCHAR(255) NOT NULL,
    description           TEXT,
    type                  VARCHAR(100),
    -- 'examen', 'composition', 'fete', 'journee_pedagogique',
    -- 'sortie_scolaire', 'conseil_classe', 'sport', 'ceremonie'
    -- Récurrence
    est_recurrente        BOOLEAN DEFAULT FALSE,
    type_recurrence       VARCHAR(20),
    -- NULL si est_recurrente = FALSE
    -- 'annuelle' : même jour et mois chaque année
    jour_recurrence       INT CHECK (jour_recurrence BETWEEN 1 AND 31),
    -- Utilisé si type_recurrence = 'annuelle' : le jour du mois
    mois_recurrence       INT CHECK (mois_recurrence BETWEEN 1 AND 12),
    -- Utilisé si type_recurrence = 'annuelle' : le mois
    -- Durée et horaires par défaut (surchargeable sur l'instance)
    duree_jours           INT DEFAULT 1,              -- durée en jours (1 = journée unique)
    heure_debut_defaut    TIME,                        -- NULL si journée entière
    heure_fin_defaut      TIME,
    -- Impact sur les cours
    annule_cours          BOOLEAN DEFAULT FALSE,       -- TRUE = suspend les cours normaux
    concerne_toute_ecole  BOOLEAN DEFAULT TRUE,        -- FALSE = seulement certaines classes
    concerne_matiere_id   INT REFERENCES matieres(id), -- NULL = pas lié à une matière
    cree_par              INT REFERENCES users(id),
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

-- Occurrences concrètes d'un événement (une par année pour les récurrents)
-- C'est cette table qui est affichée dans le calendrier et l'emploi du temps
-- statut permet de confirmer, annuler ou marquer comme réalisé chaque occurrence
CREATE TABLE evenements_instances (
    id                SERIAL PRIMARY KEY,
    evenement_id      INT REFERENCES evenements(id) ON DELETE CASCADE,
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    classe_id         INT REFERENCES classes(id),     -- NULL = toute l'école
    date_debut        DATE NOT NULL,
    date_fin          DATE,                           -- NULL si duree_jours = 1
    heure_debut       TIME,                           -- surcharge l'heure du modèle si renseignée
    heure_fin         TIME,
    salle_id          INT REFERENCES salles(id),      -- NULL si hors établissement ou journée entière
    lieu_externe      VARCHAR(255),                   -- adresse si la sortie est hors école
    statut            VARCHAR(50) DEFAULT 'planifie',
    -- 'planifie'  : prévu mais pas encore confirmé
    -- 'confirme'  : confirmé, sera affiché dans l'emploi du temps
    -- 'annule'    : annulé pour cette occurrence uniquement
    -- 'realise'   : passé, archivé
    notes             TEXT,                           -- précisions spécifiques à cette occurrence
    cree_par          INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 12 — NOTIFICATIONS
-- Envoyées à tous les acteurs selon les événements du système.
-- Le template dans notification_types sert à générer le message
-- dynamiquement côté application en remplaçant les {variables}.
-- ============================================================

-- Catalogue des types de notifications avec leurs templates
CREATE TABLE notification_types (
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(100) UNIQUE NOT NULL,
    -- 'notes_publiees'           : notes disponibles pour la période
    -- 'baisse_notes_alerte'      : chute significative de moyenne
    -- 'absence_frequente'        : taux d'absence dépasse le seuil
    -- 'echeance_approchante'     : paiement dû dans N jours
    -- 'edt_modifie'              : emploi du temps modifié
    -- 'evenement_confirme'       : événement ajouté au calendrier
    -- 'document_disponible'      : PDF généré prêt au téléchargement
    -- 'depense_a_approuver'      : dépense urgente en attente (→ directeur)
    -- 'budget_depasse'           : enveloppe budgétaire dépassée
    libelle          VARCHAR(255),
    template_message TEXT                             -- ex : 'Vos notes du {periode} sont disponibles.'
);

-- Notifications envoyées aux utilisateurs (boîte de réception in-app)
-- entite_type + entite_id forment une référence polymorphe vers l'entité déclenchante
-- ex : entite_type='note', entite_id=42 → la note id=42 a déclenché cette notif
CREATE TABLE notifications (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE CASCADE,
    type_id      INT REFERENCES notification_types(id),
    titre        VARCHAR(255) NOT NULL,
    message      TEXT NOT NULL,
    lien_action  VARCHAR(500),                        -- URL vers la page concernée dans l'app
    est_lu       BOOLEAN   DEFAULT FALSE,
    date_lecture TIMESTAMP,
    entite_type  VARCHAR(100),                        -- 'note', 'paiement', 'edt', 'evenement'…
    entite_id    INT,
    created_at   TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 13 — DOCUMENTS GÉNÉRÉS
-- PDFs produits par le système à la demande.
-- est_valide passe à FALSE si les données source ont changé depuis
-- la génération (ex : note corrigée après impression du relevé).
-- ============================================================

CREATE TABLE documents (
    id                SERIAL PRIMARY KEY,
    etudiant_id       INT REFERENCES profils_etudiants(id),
    type_document     VARCHAR(100) NOT NULL,
    -- 'certificat_scolarite', 'releve_notes', 'recu_paiement', 'attestation_frequentation'
    titre             VARCHAR(255),
    fichier_url       VARCHAR(500),                    -- chemin vers le fichier PDF généré
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    periode_id        INT REFERENCES periodes(id),     -- NULL si document annuel
    genere_par        INT REFERENCES users(id),
    genere_le         TIMESTAMP DEFAULT NOW(),
    est_valide        BOOLEAN DEFAULT TRUE
);


-- ============================================================
-- SECTION 14 — WORKFLOW MODIFICATIONS DE DOSSIER
-- Un étudiant peut demander la modification de ses données
-- personnelles. La modification n'est appliquée qu'après
-- validation explicite du secrétariat.
-- ============================================================

CREATE TABLE demandes_modification_dossier (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    champ_modifie   VARCHAR(150) NOT NULL,             -- nom exact de la colonne : 'adresse', 'telephone'…
    ancienne_valeur TEXT,
    nouvelle_valeur TEXT,
    motif           TEXT,
    statut          VARCHAR(50) DEFAULT 'en_attente',  -- 'en_attente', 'approuvee', 'refusee'
    soumis_par      INT REFERENCES users(id),          -- l'étudiant ou son parent
    traite_par      INT REFERENCES users(id),          -- le secrétariat
    date_traitement TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 15 — JOURNAL D'AUDIT
-- Traçabilité complète des actions sensibles.
-- Les colonnes JSONB stockent un snapshot avant/après en JSON
-- pour permettre de reconstituer l'état à n'importe quel moment.
-- ============================================================

CREATE TABLE audit_log (
    id                SERIAL PRIMARY KEY,
    user_id           INT REFERENCES users(id) ON DELETE SET NULL,
    action            VARCHAR(200) NOT NULL,
    -- 'creation', 'modification', 'suppression', 'connexion',
    -- 'correction_note', 'approbation_depense', 'validation_dossier'
    table_concernee   VARCHAR(100),
    entite_id         INT,
    anciennes_valeurs JSONB,                           -- état avant l'action
    nouvelles_valeurs JSONB,                           -- état après l'action
    ip_address        VARCHAR(45),
    user_agent        VARCHAR(500),
    created_at        TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 16 — INDEX DE PERFORMANCE
-- ============================================================

-- Auth
CREATE INDEX idx_users_email                ON users(email);
CREATE INDEX idx_user_roles_user            ON user_roles(user_id);

-- Profils
CREATE INDEX idx_etudiants_matricule        ON profils_etudiants(matricule);
CREATE INDEX idx_professeurs_matricule      ON profils_professeurs(matricule);

-- Structure scolaire
CREATE INDEX idx_classes_niveau_annee       ON classes(niveau_id, annee_scolaire_id);
CREATE INDEX idx_affectation_prof           ON affectations_enseignement(professeur_id);
CREATE INDEX idx_affectation_classe_annee   ON affectations_enseignement(classe_id, annee_scolaire_id);

-- Inscriptions
CREATE INDEX idx_inscriptions_etudiant      ON inscriptions(etudiant_id);
CREATE INDEX idx_inscriptions_classe        ON inscriptions(classe_id);
CREATE INDEX idx_inscriptions_annee         ON inscriptions(annee_scolaire_id);

-- EDT
CREATE INDEX idx_edt_affectation            ON emploi_du_temps(affectation_id);
CREATE INDEX idx_edt_validite               ON emploi_du_temps(date_debut_validite, date_fin_validite);
CREATE INDEX idx_modif_edt_date             ON modifications_edt(emploi_du_temps_id, date_concernee);
CREATE INDEX idx_seances_date               ON seances(date_seance);

-- Notes & Moyennes
CREATE INDEX idx_notes_etudiant             ON notes(etudiant_id);
CREATE INDEX idx_notes_affectation          ON notes(affectation_id);
CREATE INDEX idx_notes_periode              ON notes(periode_id);
CREATE INDEX idx_moyennes_etudiant          ON moyennes(etudiant_id, inscription_id);

-- Absences
CREATE INDEX idx_absences_etudiant          ON absences(etudiant_id);
CREATE INDEX idx_absences_seance            ON absences(seance_id);

-- Finance recettes
CREATE INDEX idx_paiements_inscription      ON paiements(inscription_id);
CREATE INDEX idx_echeances_echeancier       ON echeances(echeancier_id);
CREATE INDEX idx_echeances_date_limite      ON echeances(date_limite);

-- Finance dépenses
CREATE INDEX idx_depenses_annee             ON depenses(annee_scolaire_id);
CREATE INDEX idx_depenses_categorie         ON depenses(categorie_id);
CREATE INDEX idx_depenses_date              ON depenses(date_depense);
CREATE INDEX idx_depenses_statut            ON depenses(statut_approbation);
CREATE INDEX idx_echeances_contrats_contrat ON echeances_contrats(contrat_id);
CREATE INDEX idx_echeances_contrats_statut  ON echeances_contrats(statut, date_echeance);
CREATE INDEX idx_previsions_annee           ON previsions_depenses(annee_scolaire_id, statut);
CREATE INDEX idx_budgets_annee              ON budgets(annee_scolaire_id);

-- Événements
CREATE INDEX idx_evenements_recurrence      ON evenements(est_recurrente, type_recurrence);
CREATE INDEX idx_evt_instances_annee        ON evenements_instances(annee_scolaire_id);
CREATE INDEX idx_evt_instances_date         ON evenements_instances(date_debut);
CREATE INDEX idx_evt_instances_statut       ON evenements_instances(statut);

-- Notifications
CREATE INDEX idx_notif_user_lu              ON notifications(user_id, est_lu);
CREATE INDEX idx_notif_created              ON notifications(created_at);

-- Audit
CREATE INDEX idx_audit_user                 ON audit_log(user_id);
CREATE INDEX idx_audit_table_entite         ON audit_log(table_concernee, entite_id);
CREATE INDEX idx_audit_date                 ON audit_log(created_at);


-- ============================================================
-- SECTION 17 — DONNÉES INITIALES (SEED)
-- ============================================================

-- Rôles système
INSERT INTO roles (nom, description) VALUES
    ('super_admin',  'Accès total, gestion technique du système'),
    ('directeur',    'Pilotage pédagogique et financier, validation'),
    ('secretariat',  'Inscriptions, dossiers, finance opérationnelle'),
    ('comptable',    'Finances, paiements, rapports financiers'),
    ('professeur',   'Saisie notes, absences, emploi du temps'),
    ('etudiant',     'Consultation notes, dossier, emploi du temps'),
    ('parent',       'Consultation dossier enfant, notifications');

-- Types de notifications
INSERT INTO notification_types (code, libelle, template_message) VALUES
    ('notes_publiees',        'Notes disponibles',              'Vos notes du {periode} sont maintenant disponibles.'),
    ('baisse_notes_alerte',   'Alerte baisse de notes',         'Votre moyenne en {matiere} a baissé significativement.'),
    ('absence_frequente',     'Absences fréquentes',            'Votre taux d''absence dépasse {seuil}%. Veuillez régulariser.'),
    ('echeance_approchante',  'Échéance de paiement proche',    'Un paiement de {montant} Ar est attendu avant le {date}.'),
    ('edt_modifie',           'Emploi du temps modifié',        'Le cours de {matiere} du {date} a été modifié : {motif}.'),
    ('evenement_confirme',    'Nouvel événement au calendrier', 'L''événement "{titre}" est prévu le {date}.'),
    ('document_disponible',   'Document prêt',                  'Votre {type_document} est disponible au téléchargement.'),
    ('depense_a_approuver',   'Dépense en attente d''approbation', 'Une dépense urgente de {montant} Ar attend votre validation.'),
    ('budget_depasse',        'Dépassement budgétaire',         'Le budget "{categorie}" est dépassé de {ecart} Ar.');

-- Catégories de dépenses racines
INSERT INTO categories_depenses (parent_id, nom, type_charge) VALUES
    (NULL, 'Ressources Humaines', 'fixe'),
    (NULL, 'Infrastructure',      'fixe'),
    (NULL, 'Pédagogie',           'variable'),
    (NULL, 'Administratif',       'variable'),
    (NULL, 'Événements',          'variable');

-- ============================================================
-- FIN DU SCHÉMA
-- Tables : 36  |  Index : 33  |  Sections : 17
-- ============================================================
