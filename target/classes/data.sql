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

-- Insertion des actualités de test
INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Réunion Parents-Professeurs', 'La réunion parents-professeurs du 2ème trimestre se tiendra vendredi 19 avril à 9h dans la grande salle.', 'Direction', 1, 'Directeur Principal', 'fas fa-bullhorn', NOW(), true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Réunion Parents-Professeurs');

INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Concours National de Maths', 'Félicitations à nos élèves de Terminale C qui ont remporté la 2ème place au concours régional de mathématiques !', 'Événement', 1, 'Directeur Principal', 'fas fa-trophy', NOW() - INTERVAL '2 days', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Concours National de Maths');

INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Calendrier des Examens T2', 'Le calendrier des examens du deuxième trimestre est disponible. Les examens se dérouleront du 5 au 16 mai.', 'Examens', 1, 'Directeur Principal', 'fas fa-clipboard-list', NOW() - INTERVAL '4 days', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Calendrier des Examens T2');

-- Insertion des notifications de test
INSERT INTO notification_types (code, libelle, template_message)
SELECT 'PAYMENT_RECEIVED', 'Paiement reçu', 'Nouveau paiement reçu de {student_name}'
WHERE NOT EXISTS (SELECT 1 FROM notification_types WHERE code = 'PAYMENT_RECEIVED');

INSERT INTO notification_types (code, libelle, template_message)
SELECT 'GRADES_PUBLISHED', 'Notes publiées', 'Les notes de {subject} ont été publiées'
WHERE NOT EXISTS (SELECT 1 FROM notification_types WHERE code = 'GRADES_PUBLISHED');

INSERT INTO notification_types (code, libelle, template_message)
SELECT 'ANNOUNCEMENT', 'Annonce', '{message}'
WHERE NOT EXISTS (SELECT 1 FROM notification_types WHERE code = 'ANNOUNCEMENT');

INSERT INTO notifications (user_id, type_id, titre, message, est_lu, created_at)
SELECT 1, (SELECT id FROM notification_types WHERE code = 'PAYMENT_RECEIVED'), 'Nouveau paiement reçu — Rakoto Jean', '1ère A — 80 000 Ar — il y a 10 min', false, NOW()
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE titre = 'Nouveau paiement reçu — Rakoto Jean');

INSERT INTO notifications (user_id, type_id, titre, message, est_lu, created_at)
SELECT 1, (SELECT id FROM notification_types WHERE code = 'GRADES_PUBLISHED'), 'Notes publiées — Mathématiques Terminale C', 'Par Prof. Rabe — il y a 30 min', false, NOW() - INTERVAL '30 minutes'
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE titre = 'Notes publiées — Mathématiques Terminale C');

INSERT INTO notifications (user_id, type_id, titre, message, est_lu, created_at)
SELECT 1, (SELECT id FROM notification_types WHERE code = 'ANNOUNCEMENT'), 'Réunion parents-profs programmée', 'Vendredi 19 avril 2026 — 9h00', true, NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE titre = 'Réunion parents-profs programmée');
