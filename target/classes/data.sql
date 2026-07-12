BEGIN;

-- ============================================================
-- 1. DONNÉES DE RÉFÉRENCE (tables de type)
-- ============================================================

-- Rôles
INSERT INTO roles (nom, description) VALUES
    ('super_admin', 'Accès total, gestion technique du système'),
    ('directeur', 'Pilotage pédagogique et financier, validation'),
    ('secretariat', 'Inscriptions, dossiers, finance opérationnelle'),
    ('comptable', 'Finances, paiements, rapports financiers'),
    ('professeur', 'Saisie notes, absences, emploi du temps'),
    ('etudiant', 'Consultation notes, dossier, emploi du temps'),
    ('parent', 'Consultation dossier enfant, notifications')
ON CONFLICT (nom) DO NOTHING;

-- Types de notifications
INSERT INTO notification_types (code, libelle, template_message) VALUES
    ('notes_publiees', 'Notes disponibles', 'Vos notes du {periode} sont maintenant disponibles.'),
    ('baisse_notes_alerte', 'Alerte baisse de notes', 'Votre moyenne en {matiere} a baissé significativement.'),
    ('absence_frequente', 'Absences fréquentes', 'Votre taux d''absence dépasse {seuil}%. Veuillez régulariser.'),
    ('echeance_approchante', 'Échéance de paiement proche', 'Un paiement de {montant} Ar est attendu avant le {date}.'),
    ('edt_modifie', 'Emploi du temps modifié', 'Le cours de {matiere} du {date} a été modifié : {motif}.'),
    ('evenement_confirme', 'Nouvel événement au calendrier', 'L''événement "{titre}" est prévu le {date}.'),
    ('document_disponible', 'Document prêt', 'Votre {type_document} est disponible au téléchargement.'),
    ('depense_a_approuver', 'Dépense en attente d''approbation', 'Une dépense urgente de {montant} Ar attend votre validation.'),
    ('budget_depasse', 'Dépassement budgétaire', 'Le budget "{categorie}" est dépassé de {ecart} Ar.')
ON CONFLICT (code) DO NOTHING;

-- Catégories de dépenses
INSERT INTO categories_depenses (parent_id, nom, type_charge) VALUES
    (NULL, 'Ressources Humaines', 'fixe'),
    (NULL, 'Infrastructure', 'fixe'),
    (NULL, 'Pédagogie', 'variable'),
    (NULL, 'Administratif', 'variable'),
    (NULL, 'Événements', 'variable')
ON CONFLICT DO NOTHING;

-- Types de fichiers
INSERT INTO types_fichiers (libelle) VALUES
    ('Document PDF'),
    ('Document Word (Docx)'),
    ('Feuille de calcul Excel'),
    ('Présentation (PPTX)'),
    ('Lien Externe (Vidéo/Site Web)'),
    ('Archive compressée (ZIP/RAR)')
ON CONFLICT (libelle) DO NOTHING;

-- Types de contrats employés
INSERT INTO types_contrats_employes (code, libelle, duree_mois, description) VALUES
    ('permanent', 'Permanent', NULL, 'Contrat sans échéance fixe'),
    ('vacataire', 'Vacataire', 12, 'Contrat à durée limitée pour heures ponctuelles'),
    ('contractuel', 'Contractuel', 12, 'Contrat à durée déterminée renouvelable')
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 2. TYPES_DEPENSES (pour la nouvelle colonne)
-- ============================================================
INSERT INTO types_depenses (libelle, description) VALUES
    ('Dépense générale', 'Type de secours pour les dépenses déjà présentes en base'),
    ('Salaires professeurs', 'Rémunération mensuelle des professeurs'),
    ('Chaises', 'Achat ou remplacement des chaises'),
    ('Craies', 'Consommables de classe'),
    ('Rénovation', 'Travaux de rénovation et d''entretien'),
    ('Création de nouvelles salles', 'Matériaux et main d''œuvre pour nouvelles salles'),
    ('Matériel pédagogique', 'Supports, fournitures et matériel éducatif')
ON CONFLICT (libelle) DO NOTHING;

-- ============================================================
-- 3. DONNÉES DE DÉMONSTRATION
-- ============================================================

-- Établissement (un seul)
INSERT INTO etablissements (id, nom, adresse, telephone, email)
SELECT 1, 'Lycée Technique de Tananarive', 'Antananarivo, Madagascar', '+261 34 00 000 01', 'contact@lycee-tana.mg'
WHERE NOT EXISTS (SELECT 1 FROM etablissements WHERE id = 1);

-- Année scolaire active
INSERT INTO annees_scolaires (id, etablissement_id, libelle, date_debut, date_fin, est_active)
SELECT 1, 1, '2025-2026', '2025-09-01', '2026-07-31', TRUE
WHERE NOT EXISTS (SELECT 1 FROM annees_scolaires WHERE id = 1);

-- Année scolaire supplémentaire pour tester les filtres multi-années
INSERT INTO annees_scolaires (id, etablissement_id, libelle, date_debut, date_fin, est_active)
SELECT 2, 1, '2024-2025', '2024-09-01', '2025-07-31', FALSE
WHERE NOT EXISTS (SELECT 1 FROM annees_scolaires WHERE id = 2);

-- Niveaux
INSERT INTO niveaux (id, etablissement_id, libelle, ordre)
VALUES
    (1, 1, 'Seconde', 1),
    (2, 1, 'Première', 2),
    (3, 1, 'Terminale', 3)
ON CONFLICT (id) DO NOTHING;

-- Classes
INSERT INTO classes (id, niveau_id, annee_scolaire_id, nom, capacite_max)
VALUES
    (1, 1, 1, 'Seconde A', 40),
    (2, 1, 1, 'Seconde B', 40),
    (3, 2, 1, 'Première S', 35),
    (4, 2, 1, 'Première ES', 35),
    (5, 3, 1, 'Terminale A', 30),
    (6, 3, 1, 'Terminale C', 30)
ON CONFLICT (id) DO NOTHING;

-- Salles
INSERT INTO salles (id, etablissement_id, nom, capacite, type)
VALUES
    (1, 1, 'Salle 101', 40, 'cours'),
    (2, 1, 'Salle 102', 40, 'cours'),
    (3, 1, 'Salle 201', 35, 'cours'),
    (4, 1, 'Salle 202', 35, 'cours'),
    (5, 1, 'Labo Physique', 30, 'laboratoire'),
    (6, 1, 'Labo Chimie', 30, 'laboratoire'),
    (7, 1, 'Salle Informatique', 25, 'laboratoire')
ON CONFLICT (id) DO NOTHING;

-- Matières
INSERT INTO matieres (id, etablissement_id, nom, code)
VALUES
    (1, 1, 'Mathématiques', 'MATH'),
    (2, 1, 'Physique-Chimie', 'PC'),
    (3, 1, 'Sciences de la Vie et de la Terre', 'SVT'),
    (4, 1, 'Français', 'FRAN'),
    (5, 1, 'Anglais', 'ANGL'),
    (6, 1, 'Histoire-Géographie', 'HIST'),
    (7, 1, 'Philosophie', 'PHIL'),
    (8, 1, 'Éducation Physique et Sportive', 'EPS')
ON CONFLICT (id) DO NOTHING;

-- Coefficients
INSERT INTO coefficients (matiere_id, niveau_id, valeur) VALUES
    (1, 1, 4.00), (2, 1, 4.00), (3, 1, 3.00), (4, 1, 3.00), (5, 1, 3.00), (6, 1, 2.00), (8, 1, 2.00),
    (1, 2, 5.00), (2, 2, 5.00), (3, 2, 4.00), (4, 2, 3.00), (5, 2, 3.00), (6, 2, 2.00), (8, 2, 2.00),
    (1, 3, 6.00), (2, 3, 6.00), (3, 3, 5.00), (4, 3, 3.00), (5, 3, 3.00), (7, 3, 4.00), (8, 3, 2.00)
ON CONFLICT DO NOTHING;

-- Périodes
INSERT INTO periodes (id, annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes)
VALUES
    (1, 1, '1er Trimestre', 'trimestre', 1, '2025-09-01', '2025-11-30', '2025-12-15'),
    (2, 1, '2ème Trimestre', 'trimestre', 2, '2025-12-01', '2026-03-31', '2026-04-15'),
    (3, 1, '3ème Trimestre', 'trimestre', 3, '2026-04-01', '2026-07-31', '2026-08-15')
ON CONFLICT (id) DO NOTHING;

INSERT INTO periodes (id, annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes)
VALUES
    (4, 2, '1er Trimestre', 'trimestre', 1, '2024-09-01', '2024-11-30', '2024-12-15'),
    (5, 2, '2ème Trimestre', 'trimestre', 2, '2024-12-01', '2025-03-31', '2025-04-15'),
    (6, 2, '3ème Trimestre', 'trimestre', 3, '2025-04-01', '2025-07-31', '2025-08-15')
ON CONFLICT (id) DO NOTHING;

-- Horaire EDT (référentiel)
INSERT INTO horaire_edt (id, libelle, heure_debut, heure_fin, ordre)
VALUES
    (1, '07h00 - 08h00', '07:00', '08:00', 1),
    (2, '08h00 - 09h00', '08:00', '09:00', 2),
    (3, '09h00 - 10h00', '09:00', '10:00', 3),
    (4, '10h00 - 11h00', '10:00', '11:00', 4),
    (5, '11h00 - 12h00', '11:00', '12:00', 5),
    (6, '13h00 - 14h00', '13:00', '14:00', 6),
    (7, '14h00 - 15h00', '14:00', '15:00', 7),
    (8, '15h00 - 16h00', '15:00', '16:00', 8),
    (9, '16h00 - 17h00', '16:00', '17:00', 9)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 4. UTILISATEURS ET PROFILS
-- ============================================================

-- Utilisateurs
INSERT INTO users (id, email, password, is_active) VALUES
    (1, 'prof.rakoto@ecole.mg', '$2y$10$hashed_password_1', TRUE),
    (2, 'prof.rasoa@ecole.mg', '$2y$10$hashed_password_2', TRUE),
    (3, 'prof.andriamanitra@ecole.mg', '$2y$10$hashed_password_3', TRUE),
    (4, 'prof.nirina@ecole.mg', '$2y$10$hashed_password_4', TRUE),
    (5, 'etudiant1@ecole.mg', '$2y$10$hashed_password_5', TRUE),
    (6, 'etudiant2@ecole.mg', '$2y$10$hashed_password_6', TRUE),
    (7, 'etudiant3@ecole.mg', '$2y$10$hashed_password_7', TRUE),
    (8, 'etudiant4@ecole.mg', '$2y$10$hashed_password_8', TRUE),
    (9, 'etudiant5@ecole.mg', '$2y$10$hashed_password_9', TRUE),
    (10, 'etudiant6@ecole.mg', '$2y$10$hashed_password_10', TRUE),
    (11, 'etudiant7@ecole.mg', '$2y$10$hashed_password_11', TRUE),
    (12, 'etudiant8@ecole.mg', '$2y$10$hashed_password_12', TRUE),
    (13, 'jean.rakoto@ecole.mg','etudiant123',true),
    (14, 'marie.rasoa@ecole.mg','etudiant123',true),
    (15, 'paul.andry@ecole.mg','etudiant123',true),
    (16, 'soa.rabe@ecole.mg','etudiant123',true),
    (17, 'hery.rajoana@ecole.mg','etudiant123',true),
    (18, 'luc.rakotoniaina@ecole.mg','etudiant123',true),
    (19, 'aina.randria@ecole.mg','etudiant123',true),
    (20, 'fanja.ravelo@ecole.mg','etudiant123',true),
    (21, 'toky.rabary@ecole.mg','etudiant123',true),
    (22, 'nantenaina.rasoanaivo@ecole.mg','etudiant123',true),
    (23, 'miora.rakotondrazaka@ecole.mg','etudiant123',true),
    (24, 'fitia.ramamonjy@ecole.mg','etudiant123',true),
    (25, 'hasina.ravelomanana@ecole.mg','etudiant123',true),
    (26, 'anto.rakotobe@ecole.mg','etudiant123',true),
    (27, 'tahina.ramanitra@ecole.mg','etudiant123',true),
    (28, 'kiady.rasoanirina@ecole.mg','etudiant123',true),
    (29, 'feno.randrianarisoa@ecole.mg','etudiant123',true),
    (30, 'lalaina.rabeson@ecole.mg','etudiant123',true),
    (31, 'sitraka.ramialison@ecole.mg','etudiant123',true),
    (32, 'mihaja.rakotovao@ecole.mg','etudiant123',true),
    (33, 'onja.rakotonirina@ecole.mg','etudiant123',true),
    (34, 'rinah.randriamampionona@ecole.mg','etudiant123',true),
    (35, 'tiana.raveloson@ecole.mg','etudiant123',true),
    (36, 'andy.rabefitia@ecole.mg','etudiant123',true),
    (37, 'sarobidy.rakotomanga@ecole.mg','etudiant123',true),
    (38, 'prof.test.decrochage@ecole.mg', 'prof123', true),
    (39, 'directeur@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    (40, 'rakoto@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    (41, 'prof@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    (42, 'etudiant@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Réaligner la séquence users après les insertions avec id explicites
SELECT setval(
    pg_get_serial_sequence('users', 'id'),
    COALESCE((SELECT MAX(id) FROM users), 0) + 1,
    false
);

-- User_roles
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 5), (2, 5), (3, 5), (4, 5),
    (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (11, 6), (12, 6),
    ((SELECT id FROM users WHERE email='directeur@ecole.mg'), 2),
    ((SELECT id FROM users WHERE email='rakoto@ecole.mg'), 3),
    ((SELECT id FROM users WHERE email='prof@ecole.mg'), 5),
    ((SELECT id FROM users WHERE email='etudiant@ecole.mg'), 6)
ON CONFLICT DO NOTHING;

-- Profils professeurs
INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat) VALUES
    (1, 'PROF001', 'Rakoto', 'Jean', '1980-05-15', 'H', '+261 34 00 001 01', 'Antananarivo', 'Mathématiques', 'permanent', '2015-09-01'),
    (2, 'PROF002', 'Rasoa', 'Marie', '1985-08-20', 'F', '+261 34 00 002 02', 'Antananarivo', 'Physique-Chimie', 'permanent', '2018-09-01'),
    (3, 'PROF003', 'Andriamanitra', 'Paul', '1978-03-10', 'H', '+261 34 00 003 03', 'Antananarivo', 'SVT', 'contractuel', '2020-09-01'),
    (4, 'PROF004', 'Nirina', 'Lucie', '1990-12-25', 'F', '+261 34 00 004 04', 'Antananarivo', 'Français', 'vacataire', '2023-09-01'),
    ((SELECT id FROM users WHERE email='prof.test.decrochage@ecole.mg'), 'PROF_TEST_DECR', 'RAZAFY', 'Michel', NULL, 'H', NULL, NULL, 'Polyvalent', 'permanent', NULL)
ON CONFLICT (matricule) DO NOTHING;

-- Profils étudiants (les 8 premiers)
INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone) VALUES
    (5, 'ETU001', 'Rasoarimanana', 'Mirana', '2008-02-14', 'Antananarivo', 'F', 'Lot IV 123', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 010 01'),
    (6, 'ETU002', 'Randrianarivony', 'Tiana', '2008-06-22', 'Fianarantsoa', 'M', 'Lot V 456', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 020 02'),
    (7, 'ETU003', 'Rakotobe', 'Niry', '2008-09-30', 'Toamasina', 'F', 'Lot VI 789', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 030 03'),
    (8, 'ETU004', 'Andrianasolo', 'Fidy', '2008-11-11', 'Mahajanga', 'M', 'Lot VII 012', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 040 04'),
    (9, 'ETU005', 'Rasolofomanana', 'Miora', '2007-04-05', 'Antsirabe', 'F', 'Lot VIII 345', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 050 05'),
    (10, 'ETU006', 'Randriamanantena', 'Rado', '2007-07-18', 'Toliara', 'M', 'Lot IX 678', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 060 06'),
    (11, 'ETU007', 'Rakotonirina', 'Lala', '2007-10-25', 'Diego Suarez', 'F', 'Lot X 901', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 070 07'),
    (12, 'ETU008', 'Andriamalala', 'Toky', '2007-01-08', 'Antananarivo', 'M', 'Lot XI 234', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 080 08')
ON CONFLICT (matricule) DO NOTHING;

-- Profils étudiants (les 25 supplémentaires) - avec CAST explicite sur les dates
INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone)
SELECT
    u.id,
    CASE u.id
        WHEN 13 THEN 'ETU20240001' WHEN 14 THEN 'ETU20240002' WHEN 15 THEN 'ETU20240003'
        WHEN 16 THEN 'ETU20240004' WHEN 17 THEN 'ETU20240005' WHEN 18 THEN 'ETU20240006'
        WHEN 19 THEN 'ETU20240007' WHEN 20 THEN 'ETU20240008' WHEN 21 THEN 'ETU20240009'
        WHEN 22 THEN 'ETU20240010' WHEN 23 THEN 'ETU20240011' WHEN 24 THEN 'ETU20240012'
        WHEN 25 THEN 'ETU20240013' WHEN 26 THEN 'ETU20240014' WHEN 27 THEN 'ETU20240015'
        WHEN 28 THEN 'ETU20240016' WHEN 29 THEN 'ETU20240017' WHEN 30 THEN 'ETU20240018'
        WHEN 31 THEN 'ETU20240019' WHEN 32 THEN 'ETU20240020' WHEN 33 THEN 'ETU20240021'
        WHEN 34 THEN 'ETU20240022' WHEN 35 THEN 'ETU20240023' WHEN 36 THEN 'ETU20240024'
        WHEN 37 THEN 'ETU20240025'
    END,
    CASE u.id
        WHEN 13 THEN 'RAKOTO' WHEN 14 THEN 'RASOA' WHEN 15 THEN 'ANDRY'
        WHEN 16 THEN 'RABE' WHEN 17 THEN 'RAJOANA' WHEN 18 THEN 'RAKOTONIAINA'
        WHEN 19 THEN 'RANDRIA' WHEN 20 THEN 'RAVELO' WHEN 21 THEN 'RABARY'
        WHEN 22 THEN 'RASOANAIVO' WHEN 23 THEN 'RAKOTONDRAZAKA' WHEN 24 THEN 'RAMAMONJY'
        WHEN 25 THEN 'RAVELOMANANA' WHEN 26 THEN 'RAKOTOBE' WHEN 27 THEN 'RAMANITRA'
        WHEN 28 THEN 'RASOANIRINA' WHEN 29 THEN 'RANDRIANARISOA' WHEN 30 THEN 'RABESON'
        WHEN 31 THEN 'RAMIALISON' WHEN 32 THEN 'RAKOTOVAO' WHEN 33 THEN 'RAKOTONIRINA'
        WHEN 34 THEN 'RANDRIAMAMPIONONA' WHEN 35 THEN 'RAVELOSON' WHEN 36 THEN 'RABEFITIA'
        WHEN 37 THEN 'RAKOTOMANGA'
    END,
    CASE u.id
        WHEN 13 THEN 'Jean' WHEN 14 THEN 'Marie' WHEN 15 THEN 'Paul'
        WHEN 16 THEN 'Soa' WHEN 17 THEN 'Hery' WHEN 18 THEN 'Luc'
        WHEN 19 THEN 'Aina' WHEN 20 THEN 'Fanja' WHEN 21 THEN 'Toky'
        WHEN 22 THEN 'Nantenaina' WHEN 23 THEN 'Miora' WHEN 24 THEN 'Fitia'
        WHEN 25 THEN 'Hasina' WHEN 26 THEN 'Anto' WHEN 27 THEN 'Tahina'
        WHEN 28 THEN 'Kiady' WHEN 29 THEN 'Feno' WHEN 30 THEN 'Lalaina'
        WHEN 31 THEN 'Sitraka' WHEN 32 THEN 'Mihaja' WHEN 33 THEN 'Onja'
        WHEN 34 THEN 'Rinah' WHEN 35 THEN 'Tiana' WHEN 36 THEN 'Andy'
        WHEN 37 THEN 'Sarobidy'
    END,
    -- 👇 CAST explicite en DATE pour éviter l'erreur
    CASE u.id
        WHEN 13 THEN '2006-03-15'::date WHEN 14 THEN '2006-07-22'::date WHEN 15 THEN '2005-11-08'::date
        WHEN 16 THEN '2006-01-30'::date WHEN 17 THEN '2005-09-12'::date WHEN 18 THEN '2006-02-10'::date
        WHEN 19 THEN '2006-05-11'::date WHEN 20 THEN '2006-08-12'::date WHEN 21 THEN '2005-10-21'::date
        WHEN 22 THEN '2005-12-18'::date WHEN 23 THEN '2006-03-08'::date WHEN 24 THEN '2006-06-14'::date
        WHEN 25 THEN '2005-07-17'::date WHEN 26 THEN '2006-09-22'::date WHEN 27 THEN '2005-04-03'::date
        WHEN 28 THEN '2006-11-30'::date WHEN 29 THEN '2006-01-19'::date WHEN 30 THEN '2005-05-05'::date
        WHEN 31 THEN '2005-08-28'::date WHEN 32 THEN '2006-02-27'::date WHEN 33 THEN '2006-07-01'::date
        WHEN 34 THEN '2005-09-13'::date WHEN 35 THEN '2006-04-18'::date WHEN 36 THEN '2005-12-09'::date
        WHEN 37 THEN '2006-10-25'::date
    END,
    CASE u.id
        WHEN 13 THEN 'Antananarivo' WHEN 14 THEN 'Fianarantsoa' WHEN 15 THEN 'Antsirabe'
        WHEN 16 THEN 'Antsirabe' WHEN 17 THEN 'Mahajanga' WHEN 18 THEN 'Antananarivo'
        WHEN 19 THEN 'Toamasina' WHEN 20 THEN 'Morondava' WHEN 21 THEN 'Antananarivo'
        WHEN 22 THEN 'Antsirabe' WHEN 23 THEN 'Antananarivo' WHEN 24 THEN 'Fianarantsoa'
        WHEN 25 THEN 'Antananarivo' WHEN 26 THEN 'Mahajanga' WHEN 27 THEN 'Toamasina'
        WHEN 28 THEN 'Antananarivo' WHEN 29 THEN 'Antsirabe' WHEN 30 THEN 'Antananarivo'
        WHEN 31 THEN 'Fianarantsoa' WHEN 32 THEN 'Mahajanga' WHEN 33 THEN 'Antananarivo'
        WHEN 34 THEN 'Toamasina' WHEN 35 THEN 'Antananarivo' WHEN 36 THEN 'Antsirabe'
        WHEN 37 THEN 'Antananarivo'
    END,
    CASE u.id
        WHEN 13 THEN 'M' WHEN 14 THEN 'F' WHEN 15 THEN 'M'
        WHEN 16 THEN 'F' WHEN 17 THEN 'M' WHEN 18 THEN 'M'
        WHEN 19 THEN 'F' WHEN 20 THEN 'F' WHEN 21 THEN 'M'
        WHEN 22 THEN 'M' WHEN 23 THEN 'F' WHEN 24 THEN 'F'
        WHEN 25 THEN 'M' WHEN 26 THEN 'M' WHEN 27 THEN 'F'
        WHEN 28 THEN 'F' WHEN 29 THEN 'M' WHEN 30 THEN 'F'
        WHEN 31 THEN 'M' WHEN 32 THEN 'M' WHEN 33 THEN 'M'
        WHEN 34 THEN 'F' WHEN 35 THEN 'M' WHEN 36 THEN 'M'
        WHEN 37 THEN 'F'
    END,
    NULL, NULL,
    CASE u.id
        WHEN 13 THEN 'Analamanga' WHEN 14 THEN 'Haute Matsiatra' WHEN 15 THEN 'Vakinankaratra'
        WHEN 16 THEN 'Vakinankaratra' WHEN 17 THEN 'Boeny' WHEN 18 THEN 'Analamanga'
        WHEN 19 THEN 'Atsinanana' WHEN 20 THEN 'Menabe' WHEN 21 THEN 'Analamanga'
        WHEN 22 THEN 'Vakinankaratra' WHEN 23 THEN 'Analamanga' WHEN 24 THEN 'Haute Matsiatra'
        WHEN 25 THEN 'Analamanga' WHEN 26 THEN 'Boeny' WHEN 27 THEN 'Atsinanana'
        WHEN 28 THEN 'Analamanga' WHEN 29 THEN 'Vakinankaratra' WHEN 30 THEN 'Analamanga'
        WHEN 31 THEN 'Haute Matsiatra' WHEN 32 THEN 'Boeny' WHEN 33 THEN 'Analamanga'
        WHEN 34 THEN 'Atsinanana' WHEN 35 THEN 'Analamanga' WHEN 36 THEN 'Vakinankaratra'
        WHEN 37 THEN 'Analamanga'
    END,
    'Malgache',
    CASE u.id
        WHEN 13 THEN '0341234501' WHEN 14 THEN '0341234502' WHEN 15 THEN '0341234503'
        WHEN 16 THEN '0341234504' WHEN 17 THEN '0341234505' WHEN 18 THEN '0341234506'
        WHEN 19 THEN '0341234507' WHEN 20 THEN '0341234508' WHEN 21 THEN '0341234509'
        WHEN 22 THEN '0341234510' WHEN 23 THEN '0341234511' WHEN 24 THEN '0341234512'
        WHEN 25 THEN '0341234513' WHEN 26 THEN '0341234514' WHEN 27 THEN '0341234515'
        WHEN 28 THEN '0341234516' WHEN 29 THEN '0341234517' WHEN 30 THEN '0341234518'
        WHEN 31 THEN '0341234519' WHEN 32 THEN '0341234520' WHEN 33 THEN '0341234521'
        WHEN 34 THEN '0341234522' WHEN 35 THEN '0341234523' WHEN 36 THEN '0341234524'
        WHEN 37 THEN '0341234525'
    END
FROM users u
WHERE u.id BETWEEN 13 AND 37
ON CONFLICT (matricule) DO NOTHING;

-- Profils directeur, secrétariat, etc.
INSERT INTO profils_directeurs (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT id, 'Directeur', 'Principal', NULL, NULL, 'H', NULL
FROM users WHERE email = 'directeur@ecole.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO profils_secretariat (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT id, 'Rakoto', 'Secretaire', NULL, NULL, 'F', NULL
FROM users WHERE email = 'rakoto@ecole.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat)
SELECT id, 'PROFLOGIN001', 'Rakoto', 'Prof', NULL, 'H', NULL, NULL, 'Polyvalent', 'permanent', NULL
FROM users WHERE email = 'prof@ecole.mg'
ON CONFLICT (matricule) DO NOTHING;

INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone)
SELECT id, 'ETULOGIN001', 'Etudiant', 'Test', NULL, NULL, 'M', NULL, NULL, NULL, 'Malgache', NULL
FROM users WHERE email = 'etudiant@ecole.mg'
ON CONFLICT (matricule) DO NOTHING;

-- ============================================================
-- 5. INSCRIPTIONS
-- ============================================================

-- Les 8 premiers étudiants
INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT
    pe.id,
    CASE pe.matricule
        WHEN 'ETU001' THEN 1
        WHEN 'ETU002' THEN 1
        WHEN 'ETU003' THEN 1
        WHEN 'ETU004' THEN 1
        WHEN 'ETU005' THEN 3
        WHEN 'ETU006' THEN 3
        WHEN 'ETU007' THEN 5
        WHEN 'ETU008' THEN 5
    END,
    1,
    CASE WHEN pe.matricule IN ('ETU001','ETU002','ETU003','ETU004') THEN 'nouvelle' ELSE 'reinscription' END,
    '2025-08-15'::date + (pe.id - 4) * interval '1 day',
    'active'
FROM profils_etudiants pe
WHERE pe.matricule IN ('ETU001','ETU002','ETU003','ETU004','ETU005','ETU006','ETU007','ETU008')
ON CONFLICT (etudiant_id, annee_scolaire_id) DO NOTHING;

-- Les 25 étudiants supplémentaires (inscrits en Seconde A)
INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut)
SELECT
    pe.id,
    1,
    (SELECT id FROM annees_scolaires WHERE est_active = true LIMIT 1),
    'nouvelle',
    CURRENT_DATE,
    'active'
FROM profils_etudiants pe
WHERE pe.matricule BETWEEN 'ETU20240001' AND 'ETU20240025'
ON CONFLICT (etudiant_id, annee_scolaire_id) DO NOTHING;

-- ============================================================
-- 6. AFFECTATIONS ENSEIGNEMENT
-- ============================================================

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
    (4, 4, 5, 1, 4.0)
ON CONFLICT (matiere_id, classe_id, annee_scolaire_id) DO NOTHING;

INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo)
SELECT
    (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR'),
    m.id,
    2,
    (SELECT id FROM annees_scolaires WHERE est_active = true LIMIT 1),
    2
FROM matieres m
WHERE m.code IN ('MATH', 'FRAN', 'SVT')
ON CONFLICT (matiere_id, classe_id, annee_scolaire_id) DO NOTHING;

-- ============================================================
-- 7. EMPLOI DU TEMPS
-- ============================================================

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
    (6, 5, 4, '10:00:00', '12:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin)
SELECT
    ae.id,
    NULL,
    CASE
        WHEN ae.matiere_id = (SELECT id FROM matieres WHERE code = 'MATH') THEN 1
        WHEN ae.matiere_id = (SELECT id FROM matieres WHERE code = 'FRAN') THEN 2
        WHEN ae.matiere_id = (SELECT id FROM matieres WHERE code = 'SVT') THEN 3
    END,
    '08:00:00',
    '10:00:00'
FROM affectations_enseignement ae
WHERE ae.professeur_id = (SELECT id FROM profils_professeurs WHERE matricule = 'PROF_TEST_DECR')
ON CONFLICT DO NOTHING;

-- Mise à jour de horaire_edt_id
UPDATE emploi_du_temps e
SET horaire_edt_id = h.id
FROM horaire_edt h
WHERE e.horaire_edt_id IS NULL
  AND e.heure_debut = h.heure_debut
  AND e.heure_fin = h.heure_fin;

-- ============================================================
-- 8. SÉANCES, ABSENCES, NOTES, MOYENNES
-- ============================================================

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
    (6, '2026-01-17', '14:00:00', '16:00:00', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO absences (seance_id, etudiant_id, type, motif, saisi_par) VALUES
    (1, 1, 'non_justifiee', NULL, 1),
    (2, 2, 'justifiee', 'Maladie', 1),
    (4, 3, 'retard', 'Transport en panne', 2),
    (9, 4, 'non_justifiee', NULL, 2),
    (11, 5, 'non_justifiee', NULL, 1),
    (13, 6, 'justifiee', 'Raison familiale', 2)
ON CONFLICT (seance_id, etudiant_id) DO NOTHING;

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
    (2, 10, 1, 'composition', 15.00, 20.00, 'Très bien', 4)
ON CONFLICT DO NOTHING;

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
    (6, 6, 1, 1, 14.83, 2, 2)
ON CONFLICT (etudiant_id, inscription_id, periode_id, matiere_id) DO NOTHING;

INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, rang, effectif_classe) VALUES
    (1, 1, 1, NULL, 14.78, 2, 4),
    (2, 2, 1, NULL, 14.11, 3, 4),
    (3, 3, 1, NULL, 18.17, 1, 4),
    (4, 4, 1, NULL, 11.17, 4, 4),
    (5, 5, 1, NULL, 17.17, 1, 2),
    (6, 6, 1, NULL, 14.83, 2, 2)
ON CONFLICT (etudiant_id, inscription_id, periode_id, matiere_id) DO NOTHING;

-- ============================================================
-- 9. ÉVÉNEMENTS ET NOTIFICATIONS
-- ============================================================

INSERT INTO evenements (etablissement_id, titre, description, type, est_recurrente, type_recurrence, jour_recurrence, mois_recurrence, duree_jours, heure_debut_defaut, heure_fin_defaut, annule_cours, concerne_toute_ecole) VALUES
    (1, 'Journée de la Rentrée', 'Cérémonie de rentrée scolaire', 'fete', FALSE, NULL, NULL, NULL, 1, '08:00:00', '12:00:00', TRUE, TRUE),
    (1, 'Composition du 1er Trimestre', 'Examen de fin de 1er trimestre', 'examen', FALSE, NULL, NULL, NULL, 3, '08:00:00', '17:00:00', TRUE, TRUE),
    (1, 'Fête de l''Indépendance', 'Célébration de l''indépendance nationale', 'fete', TRUE, 'annuelle', 26, 6, 1, NULL, NULL, TRUE, TRUE),
    (1, 'Conseil de classe - Seconde A', 'Réunion parents-professeurs', 'conseil_classe', FALSE, NULL, NULL, NULL, 1, '17:00:00', '19:00:00', FALSE, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO evenements_instances (evenement_id, annee_scolaire_id, classe_id, date_debut, date_fin, heure_debut, heure_fin, statut, notes) VALUES
    (1, 1, NULL, '2025-09-01', NULL, '08:00:00', '12:00:00', 'realise', 'Cérémonie réussie'),
    (2, 1, NULL, '2025-11-25', '2025-11-27', '08:00:00', '17:00:00', 'realise', 'Composition terminée'),
    (3, 1, NULL, '2026-06-26', NULL, NULL, NULL, 'planifie', 'À venir'),
    (4, 1, 1, '2025-12-10', NULL, '17:00:00', '19:00:00', 'realise', 'Présence de 80% des parents')
ON CONFLICT DO NOTHING;

INSERT INTO notifications (user_id, type_id, titre, message, lien_action, est_lu, entite_type, entite_id) VALUES
    (1, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (2, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (1, 5, 'Emploi du temps modifié', 'Le cours de Mathématiques du 2026-01-20 a été modifié : Salle changée.', '/professeur/calendar', FALSE, 'edt', 1),
    (2, 6, 'Nouvel événement au calendrier', 'L''événement "Composition du 1er Trimestre" est prévu le 2025-11-25.', '/professeur/calendar', TRUE, 'evenement', 2),
    (5, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (6, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maintenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (5, 2, 'Alerte baisse de notes', 'Votre moyenne en Mathématiques a baissé significativement.', '/etudiant/notes', FALSE, 'note', 15),
    (7, 3, 'Absences fréquentes', 'Votre taux d''absence dépasse 10%. Veuillez régulariser.', '/etudiant/absences', TRUE, 'absence', 1)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 10. SUPPORTS DE COURS
-- ============================================================

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
    (2, 1, 'Composition blanche', 'Sujet de composition blanche', '/uploads/maths/premiere/composition_blanche.pdf', 'exercice', '2026-02-01 23:59:59', FALSE, 1)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 11. TITULAIRES DE CLASSE
-- ============================================================

INSERT INTO titulaires_classes (professeur_id, classe_id, annee_scolaire_id, date_nomination) VALUES
    (1, 1, 1, '2025-09-01')
ON CONFLICT (classe_id, annee_scolaire_id) DO NOTHING;

-- ============================================================
-- 12. ACTUALITÉS (data.sql)
-- ============================================================

INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Réunion Parents-Professeurs', 'La réunion parents-professeurs du 2ème trimestre se tiendra vendredi 19 avril à 9h dans la grande salle.', 'Direction', 1, 'Directeur Principal', 'fas fa-bullhorn', NOW(), true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Réunion Parents-Professeurs');

INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Concours National de Maths', 'Félicitations à nos élèves de Terminale C qui ont remporté la 2ème place au concours régional de mathématiques !', 'Événement', 1, 'Directeur Principal', 'fas fa-trophy', NOW() - INTERVAL '2 days', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Concours National de Maths');

INSERT INTO actualites (titre, contenu, categorie, auteur_id, auteur_nom, icone_classe, date_publication, est_active, created_at, updated_at)
SELECT 'Calendrier des Examens T2', 'Le calendrier des examens du deuxième trimestre est disponible. Les examens se dérouleront du 5 au 16 mai.', 'Examens', 1, 'Directeur Principal', 'fas fa-clipboard-list', NOW() - INTERVAL '4 days', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM actualites WHERE titre = 'Calendrier des Examens T2');

-- ============================================================
-- 13. NOTIFICATIONS SUPPLÉMENTAIRES (data.sql)
-- ============================================================

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

-- ============================================================
-- 14. MISE À JOUR DES COLONNES AJOUTÉES À DEPENSES
-- ============================================================

UPDATE depenses d
SET
    description = COALESCE(NULLIF(d.description, ''), d.motif, d.intitule),
    prix_unitaire = COALESCE(d.prix_unitaire, d.montant, 0),
    quantite = COALESCE(d.quantite, 1),
    total = COALESCE(d.total, d.montant, COALESCE(d.prix_unitaire, d.montant, 0) * COALESCE(d.quantite, 1)),
    type_depense_id = COALESCE(
        d.type_depense_id,
        (SELECT td.id FROM types_depenses td WHERE td.libelle = 'Dépense générale' LIMIT 1)
    )
WHERE d.type_depense_id IS NULL
   OR d.description IS NULL
   OR d.prix_unitaire IS NULL
   OR d.quantite IS NULL
   OR d.total IS NULL;

-- ============================================================
-- 15. MISE À JOUR DES SEXE ET ID_CONTRAT DANS LES PROFILS
-- ============================================================

UPDATE profils_professeurs
SET sexe = CASE WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F' ELSE 'H' END;

UPDATE profils_secretariat
SET sexe = CASE WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F' ELSE 'H' END;

UPDATE profils_directeurs
SET sexe = CASE WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F' ELSE 'H' END;

UPDATE profils_comptables
SET sexe = CASE WHEN UPPER(COALESCE(sexe, '')) = 'F' THEN 'F' ELSE 'H' END;

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

-- ============================================================
-- 16. PASSAGE DES COLONNES DE DEPENSES EN NOT NULL
-- ============================================================

ALTER TABLE depenses
    ALTER COLUMN type_depense_id SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN prix_unitaire SET NOT NULL,
    ALTER COLUMN quantite SET NOT NULL,
    ALTER COLUMN total SET NOT NULL;

-- ============================================================
-- 17. GÉNÉRATION DE DONNÉES SUPPLÉMENTAIRES (MOYENNES, ABSENCES...)
--     (extrait du DO $$ final de Ecole.sql)
-- ============================================================

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
            (rec.etudiant_id, rec.inscription_id, v_p3_id, NULL, ROUND(v_moy_p3, 2), 25)
        ON CONFLICT (etudiant_id, inscription_id, periode_id, matiere_id) DO NOTHING;

        IF v_edt_math_id IS NOT NULL AND v_edt_fran_id IS NOT NULL AND v_edt_svt_id IS NOT NULL AND v_prof_user_id IS NOT NULL THEN
            INSERT INTO absences (seance_id, etudiant_id, type, saisi_par)
            SELECT s.id, rec.etudiant_id, 'non_justifiee', v_prof_user_id
            FROM seances s
            WHERE s.emploi_du_temps_id IN (v_edt_math_id, v_edt_fran_id, v_edt_svt_id)
              AND NOT EXISTS (
                  SELECT 1 FROM absences a WHERE a.seance_id = s.id AND a.etudiant_id = rec.etudiant_id
              )
            ORDER BY s.date_seance, s.id
            LIMIT v_nb_absences
            ON CONFLICT (seance_id, etudiant_id) DO NOTHING;
        END IF;

    END LOOP;

END $$;

COMMIT;