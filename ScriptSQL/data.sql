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
INSERT INTO users (email, password, is_active) VALUES
    ('directeur@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('rakoto@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('prof@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE),
    ('etudiant@ecole.mg', '$2a$10$cDhdDnmh8rsr0IGdsvqTnuoswY47vKD01K1eACxt1lb7gXYlqTzXS', TRUE)
ON CONFLICT (id) DO NOTHING;

-- User_roles
INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE email='directeur@ecole.mg'), 2),
    ((SELECT id FROM users WHERE email='rakoto@ecole.mg'), 3),
    ((SELECT id FROM users WHERE email='prof@ecole.mg'), 5),
    ((SELECT id FROM users WHERE email='etudiant@ecole.mg'), 6)
ON CONFLICT DO NOTHING;

-- Profils directeur, secrétariat, professeur, etudiant.
INSERT INTO profils_directeurs (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT id, 'Directeur', 'Principal', NULL, NULL, 'H', NULL
FROM users WHERE email = 'directeur@ecole.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO profils_secretariat (user_id, nom, prenom, telephone, photo_url, sexe, id_contrat)
SELECT id, 'Rakoto', 'Secretaire', NULL, NULL, 'F', NULL
FROM users WHERE email = 'rakoto@ecole.mg'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO profils_professeurs (user_id, matricule, nom, prenom, date_naissance, sexe, telephone, adresse, specialite, type_contrat, date_debut_contrat)
SELECT id, 'PROF001', 'Rakoto', 'Prof', NULL, 'H', NULL, NULL, 'Polyvalent', 'permanent', NULL
FROM users WHERE email = 'prof@ecole.mg'
ON CONFLICT (matricule) DO NOTHING;

INSERT INTO profils_etudiants (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, adresse, commune, region, nationalite, telephone)
SELECT id, 'ETU001', 'Etudiant', 'Test', NULL, NULL, 'M', NULL, NULL, NULL, 'Malgache', NULL
FROM users WHERE email = 'etudiant@ecole.mg'
ON CONFLICT (matricule) DO NOTHING;

-- Mise à jour de horaire_edt_id
UPDATE emploi_du_temps e
SET horaire_edt_id = h.id
FROM horaire_edt h
WHERE e.horaire_edt_id IS NULL
  AND e.heure_debut = h.heure_debut
  AND e.heure_fin = h.heure_fin;

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
