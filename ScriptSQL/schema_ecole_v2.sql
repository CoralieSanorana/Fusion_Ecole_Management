-- Active: 1773507358543@@127.0.0.1@5432@ecole
-- ============================================================
--  SCHeMA COMPLET — GESTION D'eCOLE
--  Base de donnees : PostgreSQL
--  Framework      : CodeIgniter 4 (PHP)
--  equipes        : etudiant | Professeur | Secretariat | Directeur
--
--  Convention de nommage :
--    • Tables     : snake_case, pluriel
--    • PK         : toujours "id SERIAL PRIMARY KEY"
--    • FK         : <table_singulier>_id
--    • Timestamps : created_at / updated_at (DEFAULT NOW())
--    • Soft delete : is_archived BOOLEAN (prefere au DELETE physique)
--
--  Modules inclus :
--    ✔ Authentification & roles
--    ✔ Structure de l'etablissement
--    ✔ Profils de tous les acteurs
--    ✔ Inscriptions & scolarite
--    ✔ Affectations d'enseignement
--    ✔ Emploi du temps (recurrence + modifications)
--    ✔ Seances & absences
--    ✔ Notes & moyennes
--    ✔ Finance — recettes ecolages
--    ✔ Finance — depenses reelles + previsions + budgets
--    ✔ evenements ponctuels & recurrents
--    ✔ Notifications
--    ✔ Documents generes
--    ✔ Workflow modifications dossier
--    ✔ Journal d'audit
-- ============================================================


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


-- ============================================================
-- SECTION 2 — STRUCTURE DE L'eTABLISSEMENT
-- Tables de reference partagees par toutes les equipes.
-- Concu pour supporter plusieurs etablissements (futur).
-- ============================================================

-- L'etablissement scolaire principal
-- directeur_id est ajoute apres creation de profils_directeurs
-- pour eviter la reference circulaire entre les deux tables.
CREATE TABLE etablissements (
    id           SERIAL PRIMARY KEY,
    nom          VARCHAR(255) NOT NULL,
    adresse      TEXT,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    logo_url     VARCHAR(500),
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Annees scolaires (ex : "2024-2025")
-- Une seule peut etre marquee "active" a la fois, controle applicativement
CREATE TABLE annees_scolaires (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(50) NOT NULL,          -- ex : '2024-2025'
    date_debut       DATE NOT NULL,
    date_fin         DATE NOT NULL,
    est_active       BOOLEAN DEFAULT FALSE,          -- l'annee scolaire en cours
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Niveaux d'enseignement (ex : Seconde, Premiere, Terminale)
-- Le champ "ordre" permet d'afficher les niveaux du plus bas au plus haut
CREATE TABLE niveaux (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    libelle          VARCHAR(100) NOT NULL,
    ordre            INT NOT NULL,                   -- tri croissant : 1=Seconde, 2=Premiere…
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Classes (ex : "Terminale A 2024-2025")
-- Une classe est l'instance d'un niveau pour une annee donnee
CREATE TABLE classes (
    id                SERIAL PRIMARY KEY,
    niveau_id         INT REFERENCES niveaux(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    nom               VARCHAR(100) NOT NULL,         -- ex : 'Terminale A', '1ere S'
    capacite_max      INT DEFAULT 40,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- Salles de cours disponibles dans l'etablissement
CREATE TABLE salles (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(100) NOT NULL,           -- ex : 'Salle 12', 'Labo Chimie'
    capacite         INT,
    type             VARCHAR(50) DEFAULT 'cours',     -- 'cours', 'laboratoire', 'amphi', 'sport'
    is_active        BOOLEAN     DEFAULT TRUE,
    created_at       TIMESTAMP   DEFAULT NOW()
);

-- Matieres enseignees dans l'etablissement
CREATE TABLE matieres (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(150) NOT NULL,           -- ex : 'Mathematiques', 'Francais'
    code             VARCHAR(20),                     -- ex : 'MATH', 'FRAN', 'SVT'
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Coefficients d'une matiere selon le niveau
-- Le coeff varie selon le niveau (ex : Maths coeff 4 en Terminale, 3 en Premiere)
-- Indispensable pour le calcul correct des moyennes ponderees
CREATE TABLE coefficients (
    id         SERIAL PRIMARY KEY,
    matiere_id INT REFERENCES matieres(id) ON DELETE CASCADE,
    niveau_id  INT REFERENCES niveaux(id)  ON DELETE CASCADE,
    valeur     NUMERIC(4,2) NOT NULL,                 -- ex : 4.00, 3.00, 1.50
    UNIQUE (matiere_id, niveau_id)
);

-- Periodes d'evaluation (trimestres ou semestres selon l'ecole)
-- date_publication_notes : avant cette date, les eleves ne voient pas leurs notes
CREATE TABLE periodes (
    id                     SERIAL PRIMARY KEY,
    annee_scolaire_id      INT REFERENCES annees_scolaires(id),
    libelle                VARCHAR(100) NOT NULL,     -- ex : '1er Trimestre', '2eme Semestre'
    type                   VARCHAR(20) DEFAULT 'trimestre',  -- 'trimestre' | 'semestre'
    ordre                  INT NOT NULL,              -- 1, 2 ou 3
    date_debut             DATE,
    date_fin               DATE,
    date_publication_notes DATE,                      -- date de visibilite des notes pour les eleves
    est_cloturee           BOOLEAN DEFAULT FALSE      -- TRUE = plus aucune saisie/correction possible
);


-- ============================================================
-- SECTION 3 — PROFILS DES ACTEURS
-- Chaque acteur a son propre profil lie a un compte "users".
-- La separation des profils permet de n'exposer que les colonnes
-- necessaires a chaque equipe sans tout mettre dans "users".
-- ============================================================

-- Profil complet d'un etudiant
-- La colonne "region" est importante : elle sert aux criteres geographiques
CREATE TABLE profils_etudiants (
    id             SERIAL PRIMARY KEY,
    user_id        INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    matricule      VARCHAR(100) UNIQUE NOT NULL,      -- identifiant unique interne a l'ecole
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
    cin            VARCHAR(50),                       -- Carte d'Identite Nationale (si majeur 18+)
    telephone      VARCHAR(50),
    is_archived    BOOLEAN DEFAULT FALSE,             -- archive apres fin de scolarite, jamais supprime
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

-- Profil complet d'un professeur
-- type_contrat determine si le prof est permanent ou temporaire
CREATE TABLE profils_professeurs (
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
    specialite         VARCHAR(200),                  -- specialite academique principale
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

-- Ajout du directeur sur l'etablissement
-- Differe ici pour eviter la reference circulaire avec profils_directeurs
ALTER TABLE etablissements
    ADD COLUMN directeur_id INT REFERENCES profils_directeurs(id) ON DELETE SET NULL;

-- Profil secretariat (peut y avoir plusieurs secretaires dans un etablissement)
CREATE TABLE profils_secretariat (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Profil comptable (peut etre la meme personne que le secretariat ou separe)
CREATE TABLE profils_comptables (
    id         SERIAL PRIMARY KEY,
    user_id    INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(150) NOT NULL,
    prenom     VARCHAR(150) NOT NULL,
    telephone  VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Profil parent ou tuteur legal
-- user_id peut etre NULL : le parent n'est pas oblige d'avoir un compte
CREATE TABLE profils_parents (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE SET NULL,
    nom          VARCHAR(150) NOT NULL,
    prenom       VARCHAR(150) NOT NULL,
    telephone    VARCHAR(50),
    email        VARCHAR(255),
    profession   VARCHAR(200),
    lien_parente VARCHAR(100),                        -- 'pere', 'mere', 'tuteur', 'grand-parent'
    created_at   TIMESTAMP DEFAULT NOW()
);

-- Liaison etudiant ↔ parents/tuteurs (un etudiant peut avoir plusieurs tuteurs)
CREATE TABLE etudiants_parents (
    etudiant_id           INT REFERENCES profils_etudiants(id) ON DELETE CASCADE,
    parent_id             INT REFERENCES profils_parents(id)   ON DELETE CASCADE,
    est_contact_principal BOOLEAN DEFAULT FALSE,      -- un seul doit etre TRUE par etudiant
    PRIMARY KEY (etudiant_id, parent_id)
);


-- ============================================================
-- SECTION 4 — INSCRIPTIONS & SCOLARITe
-- Table pivot centrale : etudiant → classe → annee scolaire.
-- Notes, paiements et absences se rattachent tous a une inscription.
-- ============================================================

-- Inscription annuelle d'un etudiant dans une classe
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
    rang_final        INT,                             -- calcule et stocke en fin d'annee
    est_admis         BOOLEAN,                         -- resultat de passage en classe superieure
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, annee_scolaire_id)
);


-- ============================================================
-- SECTION 5 — AFFECTATIONS D'ENSEIGNEMENT
-- Definit qui enseigne quoi, dans quelle classe, pour quelle annee.
-- C'est la base de l'emploi du temps et de la saisie des notes.
-- UNIQUE sur (matiere, classe, annee) : une matiere = un seul prof par classe par an
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
-- Recurrence hebdomadaire + gestion des modifications.
--
-- Logique de recurrence :
--   emploi_du_temps stocke la ReGLE (ex : "chaque lundi 08h-10h")
--   date_debut_validite / date_fin_validite delimitent la periode
--   de validite d'une regle — si la salle change a partir du 15 mars,
--   on ferme la regle actuelle (date_fin_validite = 14 mars) et on
--   cree une nouvelle regle identique avec la nouvelle salle.
--
-- Logique des modifications :
--   portee = 'ponctuel'  → exception sur UNE date precise uniquement
--   portee = 'permanent' → declenche la fermeture + recreation de regle
-- ============================================================

-- Regles de cours recurrentes hebdomadaires
CREATE TABLE emploi_du_temps (
    id                  SERIAL PRIMARY KEY,
    affectation_id      INT REFERENCES affectations_enseignement(id),
    salle_id            INT REFERENCES salles(id),
    jour_semaine        INT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    -- 1=Lundi, 2=Mardi, 3=Mercredi, 4=Jeudi, 5=Vendredi, 6=Samedi
    heure_debut         TIME NOT NULL,
    heure_fin           TIME NOT NULL,
    date_debut_validite DATE,                          -- NULL = depuis le debut de l'annee scolaire
    date_fin_validite   DATE,                          -- NULL = jusqu'a la fin de l'annee scolaire
    created_at          TIMESTAMP DEFAULT NOW()
);

-- Modifications ponctuelles ou permanentes d'un creneau
-- portee = 'ponctuel'  : ne touche que la date_concernee
-- portee = 'permanent' : le code applicatif doit fermer l'ancienne regle
--                        et en creer une nouvelle a partir de date_concernee
CREATE TABLE modifications_edt (
    id                   SERIAL PRIMARY KEY,
    emploi_du_temps_id   INT REFERENCES emploi_du_temps(id),
    date_concernee       DATE NOT NULL,                -- la date exacte du cours impacte
    portee               VARCHAR(20) DEFAULT 'ponctuel',
    -- 'ponctuel'  : exception sur ce seul jour
    -- 'permanent' : changement definitif a partir de ce jour
    type_modification    VARCHAR(50) NOT NULL,
    -- 'annulation'         : cours supprime ce jour
    -- 'deplacement_horaire': heure changee
    -- 'changement_salle'   : salle changee
    -- 'remplacement_prof'  : prof remplace
    motif                VARCHAR(500),
    nouvelle_salle_id    INT REFERENCES salles(id),    -- rempli si changement de salle
    nouvelle_heure_debut TIME,                         -- rempli si deplacement horaire
    nouvelle_heure_fin   TIME,
    remplacant_id        INT REFERENCES profils_professeurs(id),  -- rempli si remplacement
    cree_par             INT REFERENCES users(id),
    created_at           TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 7 — SeANCES & ABSENCES
-- Seances : instanciation concrete de chaque creneau EDT pour
-- un jour precis, necessaire pour attacher un pointage reel.
-- Absences : un enregistrement par etudiant absent par seance.
-- ============================================================

-- Chaque occurrence reelle d'un creneau (generee en debut d'annee ou a la volee)
-- a_eu_lieu = FALSE si le cours est annule (prof absent, evenement, etc.)
CREATE TABLE seances (
    id                 SERIAL PRIMARY KEY,
    emploi_du_temps_id INT REFERENCES emploi_du_temps(id),
    date_seance        DATE NOT NULL,
    heure_debut        TIME,
    heure_fin          TIME,
    a_eu_lieu          BOOLEAN DEFAULT TRUE,           -- FALSE = cours annule
    created_at         TIMESTAMP DEFAULT NOW()
);

-- Pointage des absences par etudiant pour chaque seance
-- UNIQUE sur (seance_id, etudiant_id) : un seul enregistrement par eleve par cours
CREATE TABLE absences (
    id               SERIAL PRIMARY KEY,
    seance_id        INT REFERENCES seances(id),
    etudiant_id      INT REFERENCES profils_etudiants(id),
    type             VARCHAR(50) DEFAULT 'non_justifiee',
    -- 'non_justifiee', 'justifiee', 'retard'
    motif            TEXT,                             -- obligatoire si type = 'justifiee'
    justificatif_url VARCHAR(500),                    -- scan du justificatif fourni
    saisi_par        INT REFERENCES users(id),         -- professeur qui fait le pointage
    valide_par       INT REFERENCES users(id),         -- secretariat qui valide la justification
    date_validation  TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    UNIQUE (seance_id, etudiant_id)
);


-- ============================================================
-- SECTION 8 — NOTES & MOYENNES
-- equipe Professeur : saisie et correction de notes.
-- equipe etudiant   : lecture, graphiques, trajectoire.
--
-- Denormalisation intentionnelle dans "moyennes" :
--   Stocker les moyennes calculees evite de les recalculer a chaque
--   affichage. Elles sont invalidees et recalculees apres chaque
--   saisie ou correction de note.
-- ============================================================

-- Notes individuelles — chaque devoir/composition = une ligne
-- "sur" permet des notes sur 10 ou sur 100 si l'ecole le souhaite
CREATE TABLE notes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    affectation_id  INT REFERENCES affectations_enseignement(id),
    -- affectation_id regroupe : matiere + classe + professeur + annee
    periode_id      INT REFERENCES periodes(id),
    type_evaluation VARCHAR(100),
    -- 'devoir_1', 'devoir_2', 'composition', 'examen_blanc', 'oral', 'tp'
    valeur          NUMERIC(5,2) NOT NULL CHECK (valeur >= 0),
    sur             NUMERIC(5,2) DEFAULT 20.00,        -- note sur X (defaut /20)
    commentaire     TEXT,
    -- Tracabilite de la saisie initiale
    saisi_par       INT REFERENCES users(id),          -- le professeur
    date_saisie     TIMESTAMP DEFAULT NOW(),
    est_valide      BOOLEAN DEFAULT TRUE,
    -- Tracabilite des corrections (necessite validation secretariat)
    ancienne_valeur NUMERIC(5,2),                     -- valeur avant correction
    corrige_par     INT REFERENCES users(id),
    date_correction TIMESTAMP,
    motif_correction TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- Moyennes stockees (denormalisation pour performance)
-- periode_id NULL  → moyenne annuelle
-- matiere_id NULL  → moyenne generale toutes matieres confondues
-- rang + effectif_classe permettent d'afficher "5eme sur 32 eleves"
CREATE TABLE moyennes (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    inscription_id  INT REFERENCES inscriptions(id),
    periode_id      INT REFERENCES periodes(id),       -- NULL = moyenne annuelle
    matiere_id      INT REFERENCES matieres(id),       -- NULL = moyenne generale
    valeur          NUMERIC(5,2),
    rang            INT,                               -- rang dans la classe, ex : 5
    effectif_classe INT,                               -- nb eleves dans la classe, ex : 32
    calculated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (etudiant_id, inscription_id, periode_id, matiere_id)
);


-- ============================================================
-- SECTION 9 — FINANCE : RECETTES (eCOLAGES)
-- Ce que l'ecole attend de recevoir des familles.
-- Flux : grille tarifaire → echeancier → echeances → paiements recus
-- ============================================================

-- Tarifs annuels par niveau (definis en debut d'annee par le directeur)
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

-- Plan de paiement assigne a un etudiant pour son inscription
-- Definit si l'etudiant paye comptant ou en plusieurs tranches
CREATE TABLE echeanciers (
    id             SERIAL PRIMARY KEY,
    inscription_id INT REFERENCES inscriptions(id),
    grille_id      INT REFERENCES grilles_tarifaires(id),
    type           VARCHAR(50),
    -- 'comptant', 'echelonne_2', 'echelonne_3', 'personnalise'
    montant_total  NUMERIC(12,2),                      -- peut differer de la grille (remise accordee)
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

-- Versements reels effectues par les familles
-- Plusieurs paiements peuvent couvrir une meme echeance (paiement partiel)
CREATE TABLE paiements (
    id                    SERIAL PRIMARY KEY,
    echeance_id           INT REFERENCES echeances(id),
    inscription_id        INT REFERENCES inscriptions(id),
    montant               NUMERIC(12,2) NOT NULL,
    date_paiement         DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    -- 'especes', 'virement', 'mvola', 'orange_money', 'cheque'
    reference_transaction VARCHAR(200),                -- numero de recu ou de transaction
    saisi_par             INT REFERENCES users(id),    -- secretaire ou comptable
    notes                 TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 10 — FINANCE : DePENSES & PReVISIONS
-- Ce que l'ecole depense ou prevoit de depenser.
--
-- Architecture :
--   categories_depenses  → arborescence de classification
--   fournisseurs         → prestataires et creanciers
--   contrats_charges     → obligations contractuelles recurrentes (loyer, salaires…)
--   echeances_contrats   → tranches generees automatiquement par les contrats
--   previsions_depenses  → depenses planifiees non contractuelles (variables, urgentes)
--   depenses             → toutes les sorties d'argent reelles (fixes + variables + urgentes)
--   budgets              → enveloppes previsionnelles par categorie et par annee
-- ============================================================

-- Arborescence des categories de depenses
-- parent_id NULL = categorie racine
-- Exemples racines : 'Ressources Humaines', 'Infrastructure', 'Pedagogie', 'Administratif'
-- Exemples enfants : 'Salaires', 'Charges sociales', 'Loyer', 'electricite', 'Fournitures'
CREATE TABLE categories_depenses (
    id          SERIAL PRIMARY KEY,
    parent_id   INT REFERENCES categories_depenses(id) ON DELETE SET NULL,
    nom         VARCHAR(150) NOT NULL,
    type_charge VARCHAR(20) DEFAULT 'variable',
    -- 'fixe'     : montant stable revenant regulierement (loyer, salaire)
    -- 'variable' : montant fluctuant ou ponctuel (fournitures, reparation)
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Fournisseurs, prestataires et creanciers de l'ecole
CREATE TABLE fournisseurs (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    nom              VARCHAR(255) NOT NULL,            -- ex : 'JIRAMA', 'Imprimerie Centrale'
    type             VARCHAR(100),
    -- 'utilite'           : eau, electricite, telephone
    -- 'bailleur'          : proprietaire du batiment
    -- 'prestataire'       : services divers
    -- 'fournisseur_materiel' : papeterie, informatique
    -- 'assurance'
    contact_nom      VARCHAR(200),
    telephone        VARCHAR(50),
    email            VARCHAR(255),
    adresse          TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Contrats de charges recurrentes (engagements contractuels)
-- Represente l'OBLIGATION de payer, pas encore le paiement reel.
-- Le systeme genere automatiquement les echeances_contrats a partir de ces regles.
--
-- Exemples :
--   'Loyer Batiment Principal' — mensuel — 500 000 Ar — le 01 du mois
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
    montant_prevu    NUMERIC(12,2) NOT NULL,            -- montant attendu a chaque occurrence
    jour_echeance    INT,
    -- Pour 'mensuel' : jour du mois (ex : 30 = fin du mois, 1 = debut)
    -- Pour les autres frequences : ce champ est ignore, gere dans echeances_contrats
    date_debut       DATE NOT NULL,                    -- date d'entree en vigueur du contrat
    date_fin         DATE,                             -- NULL si contrat sans terme defini
    statut           VARCHAR(50) DEFAULT 'actif',      -- 'actif', 'suspendu', 'resilie'
    numero_contrat   VARCHAR(150),                     -- reference du document contractuel
    document_url     VARCHAR(500),                     -- scan ou chemin vers le contrat signe
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- echeances generees automatiquement a partir des contrats recurrents
-- Creees en debut d'annee (ou de mois) par un job planifie CI4
-- periode_concernee : libelle lisible ex 'Mai 2025', '2eme trimestre 2025'
CREATE TABLE echeances_contrats (
    id               SERIAL PRIMARY KEY,
    contrat_id       INT REFERENCES contrats_charges(id) ON DELETE CASCADE,
    periode_concernee VARCHAR(50) NOT NULL,
    date_echeance    DATE NOT NULL,
    montant_prevu    NUMERIC(12,2) NOT NULL,            -- copie du contrat, peut etre revise
    statut           VARCHAR(50) DEFAULT 'en_attente',
    -- 'en_attente', 'payee', 'en_retard', 'annulee'
    created_at       TIMESTAMP DEFAULT NOW()
);

-- Previsions de depenses non contractuelles (planifiees a l'avance)
-- Permet d'anticiper les sorties d'argent variables avant qu'elles soient realisees.
-- Une fois la depense effectuee, depense_id est renseigne pour lier les deux.
--
-- Exemples :
--   'Achat fournitures rentree'      — variable  — planifiee en Aout
--   'Organisation ceremonie diplome' — variable  — planifiee en Juin
--   'Reparation terrain sport'       — variable  — planifiee en Avril
CREATE TABLE previsions_depenses (
    id               SERIAL PRIMARY KEY,
    etablissement_id INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id     INT REFERENCES categories_depenses(id),
    fournisseur_id   INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    intitule         VARCHAR(255) NOT NULL,
    description      TEXT,
    montant_estime   NUMERIC(12,2) NOT NULL,            -- estimation du cout
    date_prevue      DATE NOT NULL,                    -- quand la depense est attendue
    type_charge      VARCHAR(20) NOT NULL,
    -- 'variable' : depense planifiee non contractuelle
    -- 'urgente'  : besoin imprevu identifie en avance (ex : equipement en panne)
    statut           VARCHAR(50) DEFAULT 'planifiee',
    -- 'planifiee'  : prevue mais pas encore approuvee
    -- 'approuvee'  : validee par le directeur, en attente de realisation
    -- 'realisee'   : depense effectuee (depense_id renseigne)
    -- 'annulee'    : prevision abandonnee
    approuve_par     INT REFERENCES users(id),          -- directeur qui approuve
    date_approbation TIMESTAMP,
    depense_id       INT,                              -- FK vers depenses (ajoutee apres)
    cree_par         INT REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- Depenses reelles effectuees (toutes natures confondues)
-- type_charge determine l'origine :
--   'fixe'     → liee a un contrat (echeance_contrat_id renseigne)
--   'variable' → ponctuelle planifiee (prevision_id peut etre renseigne)
--   'urgente'  → imprevu necessitant approbation directeur
-- Les depenses urgentes ou depassant un seuil passent par un workflow d'approbation.
CREATE TABLE depenses (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    annee_scolaire_id     INT REFERENCES annees_scolaires(id),
    categorie_id          INT REFERENCES categories_depenses(id),
    fournisseur_id        INT REFERENCES fournisseurs(id) ON DELETE SET NULL,
    contrat_id            INT REFERENCES contrats_charges(id),          -- NULL si non contractuel
    echeance_contrat_id   INT REFERENCES echeances_contrats(id),        -- NULL si non contractuel
    prevision_id          INT REFERENCES previsions_depenses(id),       -- NULL si non planifiee
    intitule              VARCHAR(255) NOT NULL,
    -- ex : 'Salaire Mai 2025 - Prof Rakoto', 'Facture JIRAMA Avril', 'Reparation toiture'
    type_charge           VARCHAR(20) NOT NULL,
    -- 'fixe', 'variable', 'urgente'
    motif                 TEXT,                                         -- obligatoire si 'urgente'
    montant               NUMERIC(12,2) NOT NULL,
    date_depense          DATE NOT NULL,
    mode_paiement         VARCHAR(100),
    -- 'especes', 'virement', 'cheque', 'mobile_money'
    reference             VARCHAR(200),                                 -- numero de virement ou recu
    justificatif_url      VARCHAR(500),                                 -- scan de la facture/recu
    -- Workflow d'approbation (active si depense urgente ou montant > seuil)
    necessite_approbation BOOLEAN DEFAULT FALSE,
    statut_approbation    VARCHAR(50) DEFAULT 'approuvee',
    -- 'en_attente', 'approuvee', 'refusee'
    approuve_par          INT REFERENCES users(id),                     -- directeur
    date_approbation      TIMESTAMP,
    saisi_par             INT REFERENCES users(id),                     -- comptable / secretariat
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

-- Liaison retour : previsions_depenses.depense_id → depenses.id
-- Ajoutee ici pour eviter la reference circulaire entre les deux tables
ALTER TABLE previsions_depenses
    ADD CONSTRAINT fk_prevision_depense
    FOREIGN KEY (depense_id) REFERENCES depenses(id) ON DELETE SET NULL;

-- Budgets previsionnels par categorie et par annee
-- Permet au directeur de fixer des enveloppes et de suivre le depassement
-- Requete type : SELECT montant_prevu - SUM(depenses.montant) AS solde_restant ...
CREATE TABLE budgets (
    id                SERIAL PRIMARY KEY,
    etablissement_id  INT REFERENCES etablissements(id),
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    categorie_id      INT REFERENCES categories_depenses(id),
    montant_prevu     NUMERIC(12,2) NOT NULL,           -- enveloppe allouee pour l'annee
    created_by        INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    UNIQUE (annee_scolaire_id, categorie_id)
);


-- ============================================================
-- SECTION 11 — eVeNEMENTS
-- evenements ponctuels ou recurrents apparaissant dans le calendrier
-- et/ou l'emploi du temps.
--
-- Architecture en deux tables :
--   evenements          → le MODeLE (definition + regle de recurrence)
--   evenements_instances → les OCCURRENCES concretes (une par an pour les recurrents)
--
-- Recurrence supportee :
--   'aucune'  : evenement unique, une seule instance generee
--   'annuelle': se repete chaque annee le meme jour et mois
--               (ex : Journee nationale le 26 juin chaque annee)
--
-- Le code applicatif genere les instances de l'annee active
-- au debut de chaque annee scolaire pour les modeles recurrents.
-- ============================================================

-- Modele d'evenement (template)
-- Pour un evenement non recurrent, une seule instance sera creee.
-- Pour un recurrent annuel, une instance est generee chaque annee scolaire.
CREATE TABLE evenements (
    id                    SERIAL PRIMARY KEY,
    etablissement_id      INT REFERENCES etablissements(id),
    titre                 VARCHAR(255) NOT NULL,
    description           TEXT,
    type                  VARCHAR(100),
    -- 'examen', 'composition', 'fete', 'journee_pedagogique',
    -- 'sortie_scolaire', 'conseil_classe', 'sport', 'ceremonie'
    -- Recurrence
    est_recurrente        BOOLEAN DEFAULT FALSE,
    type_recurrence       VARCHAR(20),
    -- NULL si est_recurrente = FALSE
    -- 'annuelle' : meme jour et mois chaque annee
    jour_recurrence       INT CHECK (jour_recurrence BETWEEN 1 AND 31),
    -- Utilise si type_recurrence = 'annuelle' : le jour du mois
    mois_recurrence       INT CHECK (mois_recurrence BETWEEN 1 AND 12),
    -- Utilise si type_recurrence = 'annuelle' : le mois
    -- Duree et horaires par defaut (surchargeable sur l'instance)
    duree_jours           INT DEFAULT 1,              -- duree en jours (1 = journee unique)
    heure_debut_defaut    TIME,                        -- NULL si journee entiere
    heure_fin_defaut      TIME,
    -- Impact sur les cours
    annule_cours          BOOLEAN DEFAULT FALSE,       -- TRUE = suspend les cours normaux
    concerne_toute_ecole  BOOLEAN DEFAULT TRUE,        -- FALSE = seulement certaines classes
    concerne_matiere_id   INT REFERENCES matieres(id), -- NULL = pas lie a une matiere
    cree_par              INT REFERENCES users(id),
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP DEFAULT NOW()
);

-- Occurrences concretes d'un evenement (une par annee pour les recurrents)
-- C'est cette table qui est affichee dans le calendrier et l'emploi du temps
-- statut permet de confirmer, annuler ou marquer comme realise chaque occurrence
CREATE TABLE evenements_instances (
    id                SERIAL PRIMARY KEY,
    evenement_id      INT REFERENCES evenements(id) ON DELETE CASCADE,
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    classe_id         INT REFERENCES classes(id),     -- NULL = toute l'ecole
    date_debut        DATE NOT NULL,
    date_fin          DATE,                           -- NULL si duree_jours = 1
    heure_debut       TIME,                           -- surcharge l'heure du modele si renseignee
    heure_fin         TIME,
    salle_id          INT REFERENCES salles(id),      -- NULL si hors etablissement ou journee entiere
    lieu_externe      VARCHAR(255),                   -- adresse si la sortie est hors ecole
    statut            VARCHAR(50) DEFAULT 'planifie',
    -- 'planifie'  : prevu mais pas encore confirme
    -- 'confirme'  : confirme, sera affiche dans l'emploi du temps
    -- 'annule'    : annule pour cette occurrence uniquement
    -- 'realise'   : passe, archive
    notes             TEXT,                           -- precisions specifiques a cette occurrence
    cree_par          INT REFERENCES users(id),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 12 — NOTIFICATIONS
-- Envoyees a tous les acteurs selon les evenements du systeme.
-- Le template dans notification_types sert a generer le message
-- dynamiquement cote application en remplacant les {variables}.
-- ============================================================

-- Catalogue des types de notifications avec leurs templates
CREATE TABLE notification_types (
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(100) UNIQUE NOT NULL,
    -- 'notes_publiees'           : notes disponibles pour la periode
    -- 'baisse_notes_alerte'      : chute significative de moyenne
    -- 'absence_frequente'        : taux d'absence depasse le seuil
    -- 'echeance_approchante'     : paiement du dans N jours
    -- 'edt_modifie'              : emploi du temps modifie
    -- 'evenement_confirme'       : evenement ajoute au calendrier
    -- 'document_disponible'      : PDF genere pret au telechargement
    -- 'depense_a_approuver'      : depense urgente en attente (→ directeur)
    -- 'budget_depasse'           : enveloppe budgetaire depassee
    libelle          VARCHAR(255),
    template_message TEXT                             -- ex : 'Vos notes du {periode} sont disponibles.'
);

-- Notifications envoyees aux utilisateurs (boite de reception in-app)
-- entite_type + entite_id forment une reference polymorphe vers l'entite declenchante
-- ex : entite_type='note', entite_id=42 → la note id=42 a declenche cette notif
CREATE TABLE notifications (
    id           SERIAL PRIMARY KEY,
    user_id      INT REFERENCES users(id) ON DELETE CASCADE,
    type_id      INT REFERENCES notification_types(id),
    titre        VARCHAR(255) NOT NULL,
    message      TEXT NOT NULL,
    lien_action  VARCHAR(500),                        -- URL vers la page concernee dans l'app
    est_lu       BOOLEAN   DEFAULT FALSE,
    date_lecture TIMESTAMP,
    entite_type  VARCHAR(100),                        -- 'note', 'paiement', 'edt', 'evenement'…
    entite_id    INT,
    created_at   TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 13 — DOCUMENTS GeNeReS
-- PDFs produits par le systeme a la demande.
-- est_valide passe a FALSE si les donnees source ont change depuis
-- la generation (ex : note corrigee apres impression du releve).
-- ============================================================

CREATE TABLE documents (
    id                SERIAL PRIMARY KEY,
    etudiant_id       INT REFERENCES profils_etudiants(id),
    type_document     VARCHAR(100) NOT NULL,
    -- 'certificat_scolarite', 'releve_notes', 'recu_paiement', 'attestation_frequentation'
    titre             VARCHAR(255),
    fichier_url       VARCHAR(500),                    -- chemin vers le fichier PDF genere
    annee_scolaire_id INT REFERENCES annees_scolaires(id),
    periode_id        INT REFERENCES periodes(id),     -- NULL si document annuel
    genere_par        INT REFERENCES users(id),
    genere_le         TIMESTAMP DEFAULT NOW(),
    est_valide        BOOLEAN DEFAULT TRUE
);


-- ============================================================
-- SECTION 14 — WORKFLOW MODIFICATIONS DE DOSSIER
-- Un etudiant peut demander la modification de ses donnees
-- personnelles. La modification n'est appliquee qu'apres
-- validation explicite du secretariat.
-- ============================================================

CREATE TABLE demandes_modification_dossier (
    id              SERIAL PRIMARY KEY,
    etudiant_id     INT REFERENCES profils_etudiants(id),
    champ_modifie   VARCHAR(150) NOT NULL,             -- nom exact de la colonne : 'adresse', 'telephone'…
    ancienne_valeur TEXT,
    nouvelle_valeur TEXT,
    motif           TEXT,
    statut          VARCHAR(50) DEFAULT 'en_attente',  -- 'en_attente', 'approuvee', 'refusee'
    soumis_par      INT REFERENCES users(id),          -- l'etudiant ou son parent
    traite_par      INT REFERENCES users(id),          -- le secretariat
    date_traitement TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW()
);


-- ============================================================
-- SECTION 15 — JOURNAL D'AUDIT
-- Tracabilite complete des actions sensibles.
-- Les colonnes JSONB stockent un snapshot avant/apres en JSON
-- pour permettre de reconstituer l'etat a n'importe quel moment.
-- ============================================================

CREATE TABLE audit_log (
    id                SERIAL PRIMARY KEY,
    user_id           INT REFERENCES users(id) ON DELETE SET NULL,
    action            VARCHAR(200) NOT NULL,
    -- 'creation', 'modification', 'suppression', 'connexion',
    -- 'correction_note', 'approbation_depense', 'validation_dossier'
    table_concernee   VARCHAR(100),
    entite_id         INT,
    anciennes_valeurs JSONB,                           -- etat avant l'action
    nouvelles_valeurs JSONB,                           -- etat apres l'action
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

-- Finance depenses
CREATE INDEX idx_depenses_annee             ON depenses(annee_scolaire_id);
CREATE INDEX idx_depenses_categorie         ON depenses(categorie_id);
CREATE INDEX idx_depenses_date              ON depenses(date_depense);
CREATE INDEX idx_depenses_statut            ON depenses(statut_approbation);
CREATE INDEX idx_echeances_contrats_contrat ON echeances_contrats(contrat_id);
CREATE INDEX idx_echeances_contrats_statut  ON echeances_contrats(statut, date_echeance);
CREATE INDEX idx_previsions_annee           ON previsions_depenses(annee_scolaire_id, statut);
CREATE INDEX idx_budgets_annee              ON budgets(annee_scolaire_id);

-- evenements
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
-- SECTION 17 — DONNeES INITIALES (SEED)
-- ============================================================

-- Roles systeme
INSERT INTO roles (nom, description) VALUES
    ('super_admin',  'Acces total, gestion technique du systeme'),
    ('directeur',    'Pilotage pedagogique et financier, validation'),
    ('secretariat',  'Inscriptions, dossiers, finance operationnelle'),
    ('comptable',    'Finances, paiements, rapports financiers'),
    ('professeur',   'Saisie notes, absences, emploi du temps'),
    ('etudiant',     'Consultation notes, dossier, emploi du temps'),
    ('parent',       'Consultation dossier enfant, notifications');

-- Types de notifications
INSERT INTO notification_types (code, libelle, template_message) VALUES
    ('notes_publiees',        'Notes disponibles',              'Vos notes du {periode} sont maintenant disponibles.'),
    ('baisse_notes_alerte',   'Alerte baisse de notes',         'Votre moyenne en {matiere} a baisse significativement.'),
    ('absence_frequente',     'Absences frequentes',            'Votre taux d''absence depasse {seuil}%. Veuillez regulariser.'),
    ('echeance_approchante',  'echeance de paiement proche',    'Un paiement de {montant} Ar est attendu avant le {date}.'),
    ('edt_modifie',           'Emploi du temps modifie',        'Le cours de {matiere} du {date} a ete modifie : {motif}.'),
    ('evenement_confirme',    'Nouvel evenement au calendrier', 'L''evenement "{titre}" est prevu le {date}.'),
    ('document_disponible',   'Document pret',                  'Votre {type_document} est disponible au telechargement.'),
    ('depense_a_approuver',   'Depense en attente d''approbation', 'Une depense urgente de {montant} Ar attend votre validation.'),
    ('budget_depasse',        'Depassement budgetaire',         'Le budget "{categorie}" est depasse de {ecart} Ar.');

-- Categories de depenses racines
INSERT INTO categories_depenses (parent_id, nom, type_charge) VALUES
    (NULL, 'Ressources Humaines', 'fixe'),
    (NULL, 'Infrastructure',      'fixe'),
    (NULL, 'Pedagogie',           'variable'),
    (NULL, 'Administratif',       'variable'),
    (NULL, 'evenements',          'variable');

-- ============================================================
-- FIN DU SCHeMA
-- Tables : 36  |  Index : 33  |  Sections : 17
-- ============================================================
