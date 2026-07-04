-- 1. Établissement
INSERT INTO etablissements (nom, adresse, telephone, email)
VALUES ('Lycée Professionnel Analamanga', 'Antananarivo, Madagascar', '020 22 000 00', 'contact@lycee.mg');

-- 2. Année scolaire active
INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active)
VALUES (1, '2025-2026', '2025-09-01', '2026-07-31', true);

-- 3. Niveaux
INSERT INTO niveaux (etablissement_id, libelle, ordre)
VALUES 
    (1, 'Seconde', 1),
    (1, 'Première', 2),
    (1, 'Terminale', 3);

-- 4. Classes
INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max)
VALUES
    (1, 1, 'Seconde A', 40),
    (1, 1, 'Seconde B', 40),
    (2, 1, 'Première A', 38),
    (3, 1, 'Terminale A', 35),
    (3, 1, 'Terminale C', 35);

-- 5. Grille tarifaire par niveau
INSERT INTO grilles_tarifaires (etablissement_id, niveau_id, annee_scolaire_id, montant_total, description)
VALUES
    (1, 1, 1, 960000, 'Écolage annuel Seconde 2025-2026'),
    (1, 2, 1, 1080000, 'Écolage annuel Première 2025-2026'),
    (1, 3, 1, 1200000, 'Écolage annuel Terminale 2025-2026');

-- 6. Users étudiants
INSERT INTO users (email, password, is_active, created_at, updated_at)
VALUES
    ('rakoto.jean@eleve.mg', '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i', true, NOW(), NOW()),
    ('andria.miora@eleve.mg', '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i', true, NOW(), NOW()),
    ('ratsima.lova@eleve.mg', '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i', true, NOW(), NOW());

-- 7. Profils étudiants
INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, sexe, nationalite, is_archived)
VALUES
    ((SELECT id FROM users WHERE email='rakoto.jean@eleve.mg'), 'ETU001', 'Rakoto', 'Jean', 'M', 'Malgache', false),
    ((SELECT id FROM users WHERE email='andria.miora@eleve.mg'), 'ETU002', 'Andria', 'Miora', 'F', 'Malgache', false),
    ((SELECT id FROM users WHERE email='ratsima.lova@eleve.mg'), 'ETU003', 'Ratsima', 'Lova', 'F', 'Malgache', false);

-- 8. Inscriptions
INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, statut)
VALUES
    ((SELECT id FROM profils_etudiants WHERE matricule='ETU001'), 3, 1, 'reinscription', 'active'),
    ((SELECT id FROM profils_etudiants WHERE matricule='ETU002'), 2, 1, 'nouvelle', 'active'),
    ((SELECT id FROM profils_etudiants WHERE matricule='ETU003'), 5, 1, 'reinscription', 'active');

-- 9. Écheanciers (3 tranches par élève)
INSERT INTO echeanciers (inscription_id, grille_id, type, montant_total)
VALUES
    (1, 2, 'echelonne_3', 1080000),
    (2, 1, 'echelonne_3', 960000),
    (3, 3, 'echelonne_3', 1200000);

-- 10. Échéances (3 tranches par écheancier)
INSERT INTO echeances (echeancier_id, numero_tranche, montant_attendu, date_limite, est_soldee)
VALUES
    (1, 1, 360000, '2025-10-31', false),
    (1, 2, 360000, '2026-02-28', false),
    (1, 3, 360000, '2026-06-30', false),
    (2, 1, 320000, '2025-10-31', false),
    (2, 2, 320000, '2026-02-28', false),
    (2, 3, 320000, '2026-06-30', false),
    (3, 1, 400000, '2025-10-31', false),
    (3, 2, 400000, '2026-02-28', false),
    (3, 3, 400000, '2026-06-30', false);