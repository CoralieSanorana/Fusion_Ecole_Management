-- ============================================================
-- DONNÉES DE TEST - SECTEUR PROFESSEUR
-- Script: 2026-06-13-n02-Co.sql
-- Description: Données de test pour tester les fonctionnalités du secteur professeur
-- Tables concernées: users, roles, user_roles, niveaux, classes, salles,
--                    matieres, coefficients, profils_etudiants, profils_professeurs,
--                    affectations_enseignement, emploi_du_temps, seances, absences,
--                    notes, moyennes, evenements, evenements_instances,
--                    notification_types, notifications, supports_cours
-- ============================================================

-- ============================================================
-- SECTION 1 - STRUCTURE DE L'ÉTABLISSEMENT
-- ============================================================

-- Établissement
INSERT INTO etablissements (nom, adresse, telephone, email) VALUES
    ('Lycée Technique de Tananarive', 'Antananarivo, Madagascar', '+261 34 00 000 01', 'contact@lycee-tana.mg');

-- Année scolaire
INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active) VALUES
    (1, '2025-2026', '2025-09-01', '2026-07-31', TRUE);

-- Niveaux
INSERT INTO niveaux (etablissement_id, libelle, ordre) VALUES
    (1, 'Seconde', 1),
    (1, 'Première', 2),
    (1, 'Terminale', 3);

-- Classes
INSERT INTO classes (niveau_id, annee_scolaire_id, nom, capacite_max) VALUES
    (1, 1, 'Seconde A', 40),
    (1, 1, 'Seconde B', 40),
    (2, 1, 'Première S', 35),
    (2, 1, 'Première ES', 35),
    (3, 1, 'Terminale A', 30),
    (3, 1, 'Terminale C', 30);

-- Salles
INSERT INTO salles (etablissement_id, nom, capacite, type) VALUES
    (1, 'Salle 101', 40, 'cours'),
    (1, 'Salle 102', 40, 'cours'),
    (1, 'Salle 201', 35, 'cours'),
    (1, 'Salle 202', 35, 'cours'),
    (1, 'Labo Physique', 30, 'laboratoire'),
    (1, 'Labo Chimie', 30, 'laboratoire'),
    (1, 'Salle Informatique', 25, 'laboratoire');

-- Matières
INSERT INTO matieres (etablissement_id, nom, code) VALUES
    (1, 'Mathématiques', 'MATH'),
    (1, 'Physique-Chimie', 'PC'),
    (1, 'Sciences de la Vie et de la Terre', 'SVT'),
    (1, 'Français', 'FRAN'),
    (1, 'Anglais', 'ANGL'),
    (1, 'Histoire-Géographie', 'HIST'),
    (1, 'Philosophie', 'PHIL'),
    (1, 'Éducation Physique et Sportive', 'EPS');

-- Coefficients par niveau
INSERT INTO coefficients (matiere_id, niveau_id, valeur) VALUES
    -- Seconde
    (1, 1, 4.00), (2, 1, 4.00), (3, 1, 3.00), (4, 1, 3.00), (5, 1, 3.00), (6, 1, 2.00), (8, 1, 2.00),
    -- Première
    (1, 2, 5.00), (2, 2, 5.00), (3, 2, 4.00), (4, 2, 3.00), (5, 2, 3.00), (6, 2, 2.00), (8, 2, 2.00),
    -- Terminale
    (1, 3, 6.00), (2, 3, 6.00), (3, 3, 5.00), (4, 3, 3.00), (5, 3, 3.00), (7, 3, 4.00), (8, 3, 2.00);

-- Périodes d'évaluation
INSERT INTO periodes (annee_scolaire_id, libelle, type, ordre, date_debut, date_fin, date_publication_notes) VALUES
    (1, '1er Trimestre', 'trimestre', 1, '2025-09-01', '2025-11-30', '2025-12-15'),
    (1, '2ème Trimestre', 'trimestre', 2, '2025-12-01', '2026-03-31', '2026-04-15'),
    (1, '3ème Trimestre', 'trimestre', 3, '2026-04-01', '2026-07-31', '2026-08-15');

-- ============================================================
-- SECTION 2 - UTILISATEURS ET PROFILS
-- ============================================================

-- Utilisateurs - Professeurs
INSERT INTO users (email, password, is_active) VALUES
    ('prof.rakoto@ecole.mg', '$2y$10$hashed_password_1', TRUE),
    ('prof.rasoa@ecole.mg', '$2y$10$hashed_password_2', TRUE),
    ('prof.andriamanitra@ecole.mg', '$2y$10$hashed_password_3', TRUE),
    ('prof.nirina@ecole.mg', '$2y$10$hashed_password_4', TRUE);

-- Utilisateurs - Étudiants
INSERT INTO users (email, password, is_active) VALUES
    ('etudiant1@ecole.mg', '$2y$10$hashed_password_5', TRUE),
    ('etudiant2@ecole.mg', '$2y$10$hashed_password_6', TRUE),
    ('etudiant3@ecole.mg', '$2y$10$hashed_password_7', TRUE),
    ('etudiant4@ecole.mg', '$2y$10$hashed_password_8', TRUE),
    ('etudiant5@ecole.mg', '$2y$10$hashed_password_9', TRUE),
    ('etudiant6@ecole.mg', '$2y$10$hashed_password_10', TRUE),
    ('etudiant7@ecole.mg', '$2y$10$hashed_password_11', TRUE),
    ('etudiant8@ecole.mg', '$2y$10$hashed_password_12', TRUE);

-- Rôles utilisateurs - Professeurs
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 5), -- prof.rakoto -> professeur
    (2, 5), -- prof.rasoa -> professeur
    (3, 5), -- prof.andriamanitra -> professeur
    (4, 5); -- prof.nirina -> professeur

-- Rôles utilisateurs - Étudiants
INSERT INTO user_roles (user_id, role_id) VALUES
    (5, 6), -- etudiant1 -> etudiant
    (6, 6), -- etudiant2 -> etudiant
    (7, 6), -- etudiant3 -> etudiant
    (8, 6), -- etudiant4 -> etudiant
    (9, 6), -- etudiant5 -> etudiant
    (10, 6), -- etudiant6 -> etudiant
    (11, 6), -- etudiant7 -> etudiant
    (12, 6); -- etudiant8 -> etudiant

-- Profils professeurs
INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat) VALUES
    (1, 'PROF001', 'Rakoto', 'Jean', '1980-05-15', 'M', '+261 34 00 001 01', 'Antananarivo', 'Mathématiques', 'permanent', '2015-09-01'),
    (2, 'PROF002', 'Rasoa', 'Marie', '1985-08-20', 'F', '+261 34 00 002 02', 'Antananarivo', 'Physique-Chimie', 'permanent', '2018-09-01'),
    (3, 'PROF003', 'Andriamanitra', 'Paul', '1978-03-10', 'M', '+261 34 00 003 03', 'Antananarivo', 'SVT', 'contractuel', '2020-09-01'),
    (4, 'PROF004', 'Nirina', 'Lucie', '1990-12-25', 'F', '+261 34 00 004 04', 'Antananarivo', 'Français', 'vacataire', '2023-09-01');

-- Profils étudiants
INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone) VALUES
    (5, 'ETU001', 'Rasoarimanana', 'Mirana', '2008-02-14', 'Antananarivo', 'F', 'Lot IV 123', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 010 01'),
    (6, 'ETU002', 'Randrianarivony', 'Tiana', '2008-06-22', 'Fianarantsoa', 'M', 'Lot V 456', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 020 02'),
    (7, 'ETU003', 'Rakotobe', 'Niry', '2008-09-30', 'Toamasina', 'F', 'Lot VI 789', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 030 03'),
    (8, 'ETU004', 'Andrianasolo', 'Fidy', '2008-11-11', 'Mahajanga', 'M', 'Lot VII 012', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 040 04'),
    (9, 'ETU005', 'Rasolofomanana', 'Miora', '2007-04-05', 'Antsirabe', 'F', 'Lot VIII 345', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 050 05'),
    (10, 'ETU006', 'Randriamanantena', 'Rado', '2007-07-18', 'Toliara', 'M', 'Lot IX 678', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 060 06'),
    (11, 'ETU007', 'Rakotonirina', 'Lala', '2007-10-25', 'Diego Suarez', 'F', 'Lot X 901', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 070 07'),
    (12, 'ETU008', 'Andriamalala', 'Toky', '2007-01-08', 'Antananarivo', 'M', 'Lot XI 234', 'Antananarivo', 'Analamanga', 'Malgache', '+261 34 00 080 08');

-- ============================================================
-- SECTION 3 - INSCRIPTIONS
-- ============================================================

-- Inscriptions des étudiants
INSERT INTO inscriptions (etudiant_id, classe_id, annee_scolaire_id, type_inscription, date_inscription, statut) VALUES
    (1, 1, 1, 'nouvelle', '2025-08-15', 'active'),
    (2, 1, 1, 'nouvelle', '2025-08-15', 'active'),
    (3, 1, 1, 'nouvelle', '2025-08-16', 'active'),
    (4, 1, 1, 'nouvelle', '2025-08-16', 'active'),
    (5, 3, 1, 'reinscription', '2025-08-15', 'active'),
    (6, 3, 1, 'reinscription', '2025-08-15', 'active'),
    (7, 5, 1, 'reinscription', '2025-08-16', 'active'),
    (8, 5, 1, 'reinscription', '2025-08-16', 'active');

-- ============================================================
-- SECTION 4 - AFFECTATIONS D'ENSEIGNEMENT
-- ============================================================

-- Affectations des professeurs aux classes et matières
INSERT INTO affectations_enseignement (professeur_id, matiere_id, classe_id, annee_scolaire_id, heures_hebdo) VALUES
    -- Prof Rakoto (Maths)
    (1, 1, 1, 1, 6.0),  -- Seconde A
    (1, 1, 3, 1, 6.0),  -- Première S
    (1, 1, 6, 1, 8.0),  -- Terminale C
    -- Prof Rasoa (Physique-Chimie)
    (2, 2, 1, 1, 4.0),  -- Seconde A
    (2, 2, 3, 1, 5.0),  -- Première S
    (2, 2, 6, 1, 6.0),  -- Terminale C
    -- Prof Andriamanitra (SVT)
    (3, 3, 1, 1, 3.0),  -- Seconde A
    (3, 3, 3, 1, 4.0),  -- Première S
    -- Prof Nirina (Français)
    (4, 4, 1, 1, 4.0),  -- Seconde A
    (4, 4, 4, 1, 4.0),  -- Première ES
    (4, 4, 5, 1, 4.0);  -- Terminale A

-- ============================================================
-- SECTION 5 - EMPLOI DU TEMPS
-- ============================================================

-- Emploi du temps - Prof Rakoto (Maths)
INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin) VALUES
    (1, 1, 1, '08:00:00', '10:00:00'),  -- Lundi 8h-10h Seconde A Salle 101
    (1, 1, 3, '10:00:00', '12:00:00'),  -- Mercredi 10h-12h Seconde A Salle 101
    (2, 3, 2, '08:00:00', '10:00:00'),  -- Mardi 8h-10h Première S Salle 201
    (2, 3, 4, '14:00:00', '16:00:00'),  -- Jeudi 14h-16h Première S Salle 201
    (3, 3, 1, '10:00:00', '12:00:00'),  -- Lundi 10h-12h Terminale C Salle 201
    (3, 3, 3, '08:00:00', '10:00:00'),  -- Mercredi 8h-10h Terminale C Salle 201
    (3, 3, 5, '14:00:00', '16:00:00');  -- Vendredi 14h-16h Terminale C Salle 201

-- Emploi du temps - Prof Rasoa (Physique-Chimie)
INSERT INTO emploi_du_temps (affectation_id, salle_id, jour_semaine, heure_debut, heure_fin) VALUES
    (4, 5, 2, '10:00:00', '12:00:00'),  -- Mardi 10h-12h Seconde A Labo Physique
    (4, 5, 4, '08:00:00', '10:00:00'),  -- Jeudi 8h-10h Seconde A Labo Physique
    (5, 5, 1, '14:00:00', '16:00:00'),  -- Lundi 14h-16h Première S Labo Physique
    (5, 5, 3, '14:00:00', '16:00:00'),  -- Mercredi 14h-16h Première S Labo Physique
    (6, 5, 2, '14:00:00', '16:00:00'),  -- Mardi 14h-16h Terminale C Labo Physique
    (6, 5, 4, '10:00:00', '12:00:00');  -- Jeudi 10h-12h Terminale C Labo Physique

-- ============================================================
-- SECTION 6 - SÉANCES
-- ============================================================

-- Séances pour la semaine du 13 au 17 janvier 2026
INSERT INTO seances (emploi_du_temps_id, date_seance, heure_debut, heure_fin, a_eu_lieu) VALUES
    -- Lundi 13 janvier
    (1, '2026-01-13', '08:00:00', '10:00:00', TRUE),
    (7, '2026-01-13', '10:00:00', '12:00:00', TRUE),
    (9, '2026-01-13', '14:00:00', '16:00:00', TRUE),
    -- Mardi 14 janvier
    (8, '2026-01-14', '08:00:00', '10:00:00', TRUE),
    (4, '2026-01-14', '10:00:00', '12:00:00', TRUE),
    (12, '2026-01-14', '14:00:00', '16:00:00', TRUE),
    -- Mercredi 15 janvier
    (10, '2026-01-15', '08:00:00', '10:00:00', TRUE),
    (2, '2026-01-15', '10:00:00', '12:00:00', TRUE),
    (11, '2026-01-15', '14:00:00', '16:00:00', TRUE),
    -- Jeudi 16 janvier
    (5, '2026-01-16', '08:00:00', '10:00:00', TRUE),
    (13, '2026-01-16', '10:00:00', '12:00:00', TRUE),
    (3, '2026-01-16', '14:00:00', '16:00:00', TRUE),
    -- Vendredi 17 janvier
    (6, '2026-01-17', '14:00:00', '16:00:00', TRUE);

-- ============================================================
-- SECTION 7 - ABSENCES
-- ============================================================

-- Absences des étudiants
INSERT INTO absences (seance_id, etudiant_id, type, motif, saisi_par) VALUES
    (1, 1, 'non_justifiee', NULL, 1),  -- Mirana absente cours Maths Lundi
    (2, 2, 'justifiee', 'Maladie', 1),  -- Tiana absente cours Maths Mercredi (justifiée)
    (4, 3, 'retard', 'Transport en panne', 2),  -- Niry en retard cours Physique Mardi
    (9, 4, 'non_justifiee', NULL, 2),  -- Fidy absent cours Physique Lundi
    (11, 5, 'non_justifiee', NULL, 1),  -- Miora absente cours Maths Mercredi
    (13, 6, 'justifiee', 'Raison familiale', 2);  -- Rado absent cours Physique Jeudi (justifié)

-- ============================================================
-- SECTION 8 - NOTES
-- ============================================================

-- Notes des étudiants - 1er Trimestre
INSERT INTO notes (etudiant_id, affectation_id, periode_id, type_evaluation, valeur, sur, commentaire, saisi_par) VALUES
    -- Maths Seconde A (Prof Rakoto)
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
    
    -- Physique Seconde A (Prof Rasoa)
    (1, 4, 1, 'devoir_1', 14.00, 20.00, 'Bien', 2),
    (1, 4, 1, 'tp', 16.00, 20.00, 'Très bon TP', 2),
    (1, 4, 1, 'composition', 15.00, 20.00, 'Bon résultat', 2),
    (2, 4, 1, 'devoir_1', 13.00, 20.00, 'Correct', 2),
    (2, 4, 1, 'tp', 14.50, 20.00, 'Bon TP', 2),
    (2, 4, 1, 'composition', 14.00, 20.00, 'Satisfaisant', 2),
    
    -- Maths Première S (Prof Rakoto)
    (5, 2, 1, 'devoir_1', 16.50, 20.00, 'Excellent', 1),
    (5, 2, 1, 'devoir_2', 17.00, 20.00, 'Très bien', 1),
    (5, 2, 1, 'composition', 18.00, 20.00, 'Remarquable', 1),
    (6, 2, 1, 'devoir_1', 14.00, 20.00, 'Bien', 1),
    (6, 2, 1, 'devoir_2', 15.00, 20.00, 'Bien', 1),
    (6, 2, 1, 'composition', 15.50, 20.00, 'Très bien', 1),
    
    -- Français Seconde A (Prof Nirina)
    (1, 10, 1, 'devoir_1', 13.00, 20.00, 'Correct', 4),
    (1, 10, 1, 'oral', 15.00, 20.00, 'Bonne expression', 4),
    (1, 10, 1, 'composition', 14.50, 20.00, 'Bien', 4),
    (2, 10, 1, 'devoir_1', 14.50, 20.00, 'Bien', 4),
    (2, 10, 1, 'oral', 16.00, 20.00, 'Très bonne expression', 4),
    (2, 10, 1, 'composition', 15.00, 20.00, 'Très bien', 4);

-- ============================================================
-- SECTION 9 - MOYENNES
-- ============================================================

-- Moyennes par matière et période
INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, rang, effectif_classe) VALUES
    -- Seconde A - 1er Trimestre
    (1, 1, 1, 1, 15.17, 2, 4),  -- Mirana - Maths
    (1, 1, 1, 2, 15.00, 2, 4),  -- Mirana - Physique
    (1, 1, 1, 4, 14.17, 3, 4),  -- Mirana - Français
    (2, 2, 1, 1, 13.33, 3, 4),  -- Tiana - Maths
    (2, 2, 1, 2, 13.83, 3, 4),  -- Tiana - Physique
    (2, 2, 1, 4, 15.17, 2, 4),  -- Tiana - Français
    (3, 3, 1, 1, 18.17, 1, 4),  -- Niry - Maths
    (4, 4, 1, 1, 11.17, 4, 4),  -- Fidy - Maths
    -- Première S - 1er Trimestre
    (5, 5, 1, 1, 17.17, 1, 2),  -- Miora - Maths
    (6, 6, 1, 1, 14.83, 2, 2);  -- Rado - Maths

-- Moyennes générales
INSERT INTO moyennes (etudiant_id, inscription_id, periode_id, matiere_id, valeur, rang, effectif_classe) VALUES
    (1, 1, 1, NULL, 14.78, 2, 4),  -- Mirana - Moyenne générale
    (2, 2, 1, NULL, 14.11, 3, 4),  -- Tiana - Moyenne générale
    (3, 3, 1, NULL, 18.17, 1, 4),  -- Niry - Moyenne générale
    (4, 4, 1, NULL, 11.17, 4, 4),  -- Fidy - Moyenne générale
    (5, 5, 1, NULL, 17.17, 1, 2),  -- Miora - Moyenne générale
    (6, 6, 1, NULL, 14.83, 2, 2);  -- Rado - Moyenne générale

-- ============================================================
-- SECTION 10 - ÉVÉNEMENTS
-- ============================================================

-- Événements
INSERT INTO evenements (etablissement_id, titre, description, type, est_recurrente, type_recurrence, jour_recurrence, mois_recurrence, duree_jours, heure_debut_defaut, heure_fin_defaut, annule_cours, concerne_toute_ecole) VALUES
    (1, 'Journée de la Rentrée', 'Cérémonie de rentrée scolaire', 'fete', FALSE, NULL, NULL, NULL, 1, '08:00:00', '12:00:00', TRUE, TRUE),
    (1, 'Composition du 1er Trimestre', 'Examen de fin de 1er trimestre', 'examen', FALSE, NULL, NULL, NULL, 3, '08:00:00', '17:00:00', TRUE, TRUE),
    (1, 'Fête de l''Indépendance', 'Célébration de l''indépendance nationale', 'fete', TRUE, 'annuelle', 26, 6, 1, NULL, NULL, TRUE, TRUE),
    (1, 'Conseil de classe - Seconde A', 'Réunion parents-professeurs', 'conseil_classe', FALSE, NULL, NULL, NULL, 1, '17:00:00', '19:00:00', FALSE, FALSE);

-- Instances d'événements
INSERT INTO evenements_instances (evenement_id, annee_scolaire_id, classe_id, date_debut, date_fin, heure_debut, heure_fin, statut, notes) VALUES
    (1, 1, NULL, '2025-09-01', NULL, '08:00:00', '12:00:00', 'realise', 'Cérémonie réussie'),
    (2, 1, NULL, '2025-11-25', '2025-11-27', '08:00:00', '17:00:00', 'realise', 'Composition terminée'),
    (3, 1, NULL, '2026-06-26', NULL, NULL, NULL, 'planifie', 'À venir'),
    (4, 1, 1, '2025-12-10', NULL, '17:00:00', '19:00:00', 'realise', 'Présence de 80% des parents');

-- ============================================================
-- SECTION 11 - NOTIFICATIONS
-- ============================================================

-- Notifications pour les professeurs
INSERT INTO notifications (user_id, type_id, titre, message, lien_action, est_lu, entite_type, entite_id) VALUES
    (1, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maINTenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (2, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maINTenant disponibles.', '/professeur/notes', FALSE, 'periode', 1),
    (1, 5, 'Emploi du temps modifié', 'Le cours de Mathématiques du 2026-01-20 a été modifié : Salle changée.', '/professeur/calendar', FALSE, 'edt', 1),
    (2, 6, 'Nouvel événement au calendrier', 'L''événement "Composition du 1er Trimestre" est prévu le 2025-11-25.', '/professeur/calendar', TRUE, 'evenement', 2);

-- Notifications pour les étudiants
INSERT INTO notifications (user_id, type_id, titre, message, lien_action, est_lu, entite_type, entite_id) VALUES
    (5, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maINTenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (6, 1, 'Notes publiées', 'Vos notes du 1er Trimestre sont maINTenant disponibles.', '/etudiant/notes', FALSE, 'periode', 1),
    (5, 2, 'Alerte baisse de notes', 'Votre moyenne en Mathématiques a baissé significativement.', '/etudiant/notes', FALSE, 'note', 15),
    (7, 3, 'Absences fréquentes', 'Votre taux d''absence dépasse 10%. Veuillez régulariser.', '/etudiant/absences', TRUE, 'absence', 1);

-- ============================================================
-- SECTION 12 - SUPPORTS DE COURS
-- ============================================================

-- Supports de cours publiés par les professeurs
INSERT INTO supports_cours (affectation_id, type_fichier_id, titre, description, fichier_url, type_contenu, date_limite, accepte_retard, cree_par) VALUES
    -- Prof Rakoto - Maths Seconde A
    (1, 1, 'Chapitre 1 : Les nombres réels', 'INTroduction aux nombres réels et opérations', '/uploads/maths/seconde/chap1_nombres_reels.pdf', 'lecon', NULL, FALSE, 1),
    (1, 2, 'Exercices sur les équations', 'Série d''exercices sur les équations du premier degré', '/uploads/maths/seconde/exercices_equations.docx', 'exercice', NULL, FALSE, 1),
    (1, 1, 'Devoir maison n°1', 'Devoir à rendre pour le 20 janvier', '/uploads/maths/seconde/dm1.pdf', 'devoir_maison', '2026-01-20 23:59:59', TRUE, 1),
    -- Prof Rasoa - Physique Seconde A
    (4, 1, 'Chapitre 1 : L''atome', 'Structure de l''atome et modèle de Bohr', '/uploads/physique/seconde/chap1_atome.pdf', 'lecon', NULL, FALSE, 2),
    (4, 3, 'Tableau périodique', 'Tableau périodique des éléments', '/uploads/physique/seconde/tableau_periodique.xlsx', 'lecon', NULL, FALSE, 2),
    (4, 1, 'TP n°1 - Mesures de masse', 'Compte-rendu de TP à rendre', '/uploads/physique/seconde/tp1_mesures.pdf', 'exercice', '2026-01-25 23:59:59', FALSE, 2),
    -- Prof Nirina - Français Seconde A
    (10, 1, 'Lecture analytique n°1', 'Analyse du texte "Le Horla"', '/uploads/francais/seconde/lecture_horla.pdf', 'lecon', NULL, FALSE, 4),
    (10, 1, 'Devoir maison - Résumé', 'Résumé du roman étudié', '/uploads/francais/seconde/dm_resume.pdf', 'devoir_maison', '2026-01-22 23:59:59', TRUE, 4),
    -- Prof Rakoto - Maths Première S
    (2, 1, 'Chapitre 1 : Les polynômes', 'Étude des polynômes du second degré', '/uploads/maths/premiere/chap1_polynomes.pdf', 'lecon', NULL, FALSE, 1),
    (2, 1, 'Composition blanche', 'Sujet de composition blanche', '/uploads/maths/premiere/composition_blanche.pdf', 'exercice', '2026-02-01 23:59:59', FALSE, 1);

-- ============================================================
-- FIN DU SCRIPT DE TEST
-- Total des enregistrements insérés :
-- - Établissements: 1
-- - Années scolaires: 1
-- - Niveaux: 3
-- - Classes: 6
-- - Salles: 7
-- - Matières: 8
-- - Coefficients: 18
-- - Périodes: 3
-- - Users: 12
-- - User roles: 12
-- - Profils professeurs: 4
-- - Profils étudiants: 8
-- - Inscriptions: 8
-- - Affectations enseignement: 10
-- - Emploi du temps: 13
-- - Séances: 13
-- - Absences: 6
-- - Notes: 27
-- - Moyennes: 8
-- - Événements: 4
-- - Événements instances: 4
-- - Notifications: 8
-- - Supports de cours: 11
-- ============================================================
