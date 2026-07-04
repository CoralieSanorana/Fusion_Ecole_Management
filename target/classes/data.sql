INSERT INTO roles (nom, description)
SELECT 'super_admin', 'Accès total, gestion technique du système'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'super_admin');

INSERT INTO roles (nom, description)
SELECT 'directeur', 'Pilotage pédagogique et financier, validation'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'directeur');

INSERT INTO roles (nom, description)
SELECT 'secretariat', 'Inscriptions, dossiers, finance opérationnelle'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'secretariat');

INSERT INTO roles (nom, description)
SELECT 'comptable', 'Finances, paiements, rapports financiers'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'comptable');

INSERT INTO roles (nom, description)
SELECT 'professeur', 'Saisie notes, absences, emploi du temps'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'professeur');

INSERT INTO roles (nom, description)
SELECT 'etudiant', 'Consultation notes, dossier, emploi du temps'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'etudiant');

INSERT INTO roles (nom, description)
SELECT 'parent', 'Consultation dossier enfant, notifications'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'parent');

INSERT INTO users (email, password, is_active)
SELECT 'directeur@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'directeur@ecole.mg');

INSERT INTO users (email, password, is_active)
SELECT 'rakoto@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'rakoto@ecole.mg');

INSERT INTO users (email, password, is_active)
SELECT 'prof@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'prof@ecole.mg');

INSERT INTO users (email, password, is_active)
SELECT 'etudiant@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'etudiant@ecole.mg');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'directeur@ecole.mg'
    AND r.nom = 'directeur'
    AND NOT EXISTS (
            SELECT 1 FROM user_roles ur
            WHERE ur.user_id = u.id AND ur.role_id = r.id
    );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'rakoto@ecole.mg'
    AND r.nom = 'secretariat'
    AND NOT EXISTS (
            SELECT 1 FROM user_roles ur
            WHERE ur.user_id = u.id AND ur.role_id = r.id
    );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'prof@ecole.mg'
    AND r.nom = 'professeur'
    AND NOT EXISTS (
            SELECT 1 FROM user_roles ur
            WHERE ur.user_id = u.id AND ur.role_id = r.id
    );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'etudiant@ecole.mg'
    AND r.nom = 'etudiant'
    AND NOT EXISTS (
            SELECT 1 FROM user_roles ur
            WHERE ur.user_id = u.id AND ur.role_id = r.id
    );

INSERT INTO profils_directeurs (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT
    (SELECT id FROM users WHERE email = 'directeur@ecole.mg'),
    'Directeur',
    'Principal',
    NULL,
    NULL,
    'H',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM profils_directeurs pd
    WHERE pd.user_id = (SELECT id FROM users WHERE email = 'directeur@ecole.mg')
);

INSERT INTO profils_secretariat (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT
    (SELECT id FROM users WHERE email = 'rakoto@ecole.mg'),
    'Rakoto',
    'Secretaire',
    NULL,
    NULL,
    'F',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM profils_secretariat ps
    WHERE ps.user_id = (SELECT id FROM users WHERE email = 'rakoto@ecole.mg')
);

INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat)
SELECT
    (SELECT id FROM users WHERE email = 'prof@ecole.mg'),
    'PROFLOGIN001',
    'Rakoto',
    'Prof',
    NULL,
    'H',
    NULL,
    NULL,
    'Polyvalent',
    'permanent',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM profils_professeurs pp
    WHERE pp.user_id = (SELECT id FROM users WHERE email = 'prof@ecole.mg')
);

INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone)
SELECT
    (SELECT id FROM users WHERE email = 'etudiant@ecole.mg'),
    'ETULOGIN001',
    'Etudiant',
    'Test',
    NULL,
    NULL,
    'M',
    NULL,
    NULL,
    NULL,
    'Malgache',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM profils_etudiants pe
    WHERE pe.user_id = (SELECT id FROM users WHERE email = 'etudiant@ecole.mg')
);
