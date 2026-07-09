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