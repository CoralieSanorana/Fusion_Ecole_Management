-- =============================================================
-- etudiants_test.sql (25 étudiants)
-- Prérequis :
--   etablissement id=1, niveau id=1, classe id=1
-- =============================================================

BEGIN;

-- =============================================================
-- USERS
-- =============================================================

INSERT INTO users (email, password, is_active, created_at, updated_at) VALUES
('jean.rakoto@ecole.mg','etudiant123',true,NOW(),NOW()),
('marie.rasoa@ecole.mg','etudiant123',true,NOW(),NOW()),
('paul.andry@ecole.mg','etudiant123',true,NOW(),NOW()),
('soa.rabe@ecole.mg','etudiant123',true,NOW(),NOW()),
('hery.rajoana@ecole.mg','etudiant123',true,NOW(),NOW()),
('luc.rakotoniaina@ecole.mg','etudiant123',true,NOW(),NOW()),
('aina.randria@ecole.mg','etudiant123',true,NOW(),NOW()),
('fanja.ravelo@ecole.mg','etudiant123',true,NOW(),NOW()),
('toky.rabary@ecole.mg','etudiant123',true,NOW(),NOW()),
('nantenaina.rasoanaivo@ecole.mg','etudiant123',true,NOW(),NOW()),
('miora.rakotondrazaka@ecole.mg','etudiant123',true,NOW(),NOW()),
('fitia.ramamonjy@ecole.mg','etudiant123',true,NOW(),NOW()),
('hasina.ravelomanana@ecole.mg','etudiant123',true,NOW(),NOW()),
('anto.rakotobe@ecole.mg','etudiant123',true,NOW(),NOW()),
('tahina.ramanitra@ecole.mg','etudiant123',true,NOW(),NOW()),
('kiady.rasoanirina@ecole.mg','etudiant123',true,NOW(),NOW()),
('feno.randrianarisoa@ecole.mg','etudiant123',true,NOW(),NOW()),
('lalaina.rabeson@ecole.mg','etudiant123',true,NOW(),NOW()),
('sitraka.ramialison@ecole.mg','etudiant123',true,NOW(),NOW()),
('mihaja.rakotovao@ecole.mg','etudiant123',true,NOW(),NOW()),
('onja.rakotonirina@ecole.mg','etudiant123',true,NOW(),NOW()),
('rinah.randriamampionona@ecole.mg','etudiant123',true,NOW(),NOW()),
('tiana.raveloson@ecole.mg','etudiant123',true,NOW(),NOW()),
('andy.rabefitia@ecole.mg','etudiant123',true,NOW(),NOW()),
('sarobidy.rakotomanga@ecole.mg','etudiant123',true,NOW(),NOW())
ON CONFLICT (email) DO NOTHING;

-- =============================================================
-- PROFILS ETUDIANTS
-- =============================================================

INSERT INTO profils_etudiants
(user_id, matricule, nom, prenom, date_naissance, lieu_naissance,
 sexe, region, nationalite, telephone, is_archived, created_at, updated_at)
VALUES

((SELECT id FROM users WHERE email='jean.rakoto@ecole.mg'),
'ETU20240001','RAKOTO','Jean','2006-03-15','Antananarivo','M','Analamanga','Malgache','0341234501',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='marie.rasoa@ecole.mg'),
'ETU20240002','RASOA','Marie','2006-07-22','Fianarantsoa','F','Haute Matsiatra','Malgache','0341234502',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='paul.andry@ecole.mg'),
'ETU20240003','ANDRY','Paul','2005-11-08','Antsirabe','M','Vakinankaratra','Malgache','0341234503',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='soa.rabe@ecole.mg'),
'ETU20240004','RABE','Soa','2006-01-30','Antsirabe','F','Vakinankaratra','Malgache','0341234504',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='hery.rajoana@ecole.mg'),
'ETU20240005','RAJOANA','Hery','2005-09-12','Mahajanga','M','Boeny','Malgache','0341234505',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='luc.rakotoniaina@ecole.mg'),
'ETU20240006','RAKOTONIAINA','Luc','2006-02-10','Antananarivo','M','Analamanga','Malgache','0341234506',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='aina.randria@ecole.mg'),
'ETU20240007','RANDRIA','Aina','2006-05-11','Toamasina','F','Atsinanana','Malgache','0341234507',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='fanja.ravelo@ecole.mg'),
'ETU20240008','RAVELO','Fanja','2006-08-12','Morondava','F','Menabe','Malgache','0341234508',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='toky.rabary@ecole.mg'),
'ETU20240009','RABARY','Toky','2005-10-21','Antananarivo','M','Analamanga','Malgache','0341234509',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='nantenaina.rasoanaivo@ecole.mg'),
'ETU20240010','RASOANAIVO','Nantenaina','2005-12-18','Antsirabe','M','Vakinankaratra','Malgache','0341234510',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='miora.rakotondrazaka@ecole.mg'),
'ETU20240011','RAKOTONDRAZAKA','Miora','2006-03-08','Antananarivo','F','Analamanga','Malgache','0341234511',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='fitia.ramamonjy@ecole.mg'),
'ETU20240012','RAMAMONJY','Fitia','2006-06-14','Fianarantsoa','F','Haute Matsiatra','Malgache','0341234512',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='hasina.ravelomanana@ecole.mg'),
'ETU20240013','RAVELOMANANA','Hasina','2005-07-17','Antananarivo','M','Analamanga','Malgache','0341234513',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='anto.rakotobe@ecole.mg'),
'ETU20240014','RAKOTOBE','Anto','2006-09-22','Mahajanga','M','Boeny','Malgache','0341234514',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='tahina.ramanitra@ecole.mg'),
'ETU20240015','RAMANITRA','Tahina','2005-04-03','Toamasina','F','Atsinanana','Malgache','0341234515',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='kiady.rasoanirina@ecole.mg'),
'ETU20240016','RASOANIRINA','Kiady','2006-11-30','Antananarivo','F','Analamanga','Malgache','0341234516',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='feno.randrianarisoa@ecole.mg'),
'ETU20240017','RANDRIANARISOA','Feno','2006-01-19','Antsirabe','M','Vakinankaratra','Malgache','0341234517',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='lalaina.rabeson@ecole.mg'),
'ETU20240018','RABESON','Lalaina','2005-05-05','Antananarivo','F','Analamanga','Malgache','0341234518',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='sitraka.ramialison@ecole.mg'),
'ETU20240019','RAMIALISON','Sitraka','2005-08-28','Fianarantsoa','M','Haute Matsiatra','Malgache','0341234519',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='mihaja.rakotovao@ecole.mg'),
'ETU20240020','RAKOTOVAO','Mihaja','2006-02-27','Mahajanga','M','Boeny','Malgache','0341234520',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='onja.rakotonirina@ecole.mg'),
'ETU20240021','RAKOTONIRINA','Onja','2006-07-01','Antananarivo','M','Analamanga','Malgache','0341234521',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='rinah.randriamampionona@ecole.mg'),
'ETU20240022','RANDRIAMAMPIONONA','Rinah','2005-09-13','Toamasina','F','Atsinanana','Malgache','0341234522',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='tiana.raveloson@ecole.mg'),
'ETU20240023','RAVELOSON','Tiana','2006-04-18','Antananarivo','M','Analamanga','Malgache','0341234523',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='andy.rabefitia@ecole.mg'),
'ETU20240024','RABEFITIA','Andy','2005-12-09','Antsirabe','M','Vakinankaratra','Malgache','0341234524',false,NOW(),NOW()),

((SELECT id FROM users WHERE email='sarobidy.rakotomanga@ecole.mg'),
'ETU20240025','RAKOTOMANGA','Sarobidy','2006-10-25','Antananarivo','F','Analamanga','Malgache','0341234525',false,NOW(),NOW())

ON CONFLICT (matricule) DO NOTHING;

-- =============================================================
-- INSCRIPTIONS
-- =============================================================

INSERT INTO inscriptions
(etudiant_id, classe_id, annee_scolaire_id, type_inscription,
 date_inscription, statut, created_at, updated_at)
SELECT
    pe.id,
    1,
    (SELECT id FROM annees_scolaires WHERE est_active = true LIMIT 1),
    'nouvelle',
    CURRENT_DATE,
    'active',
    NOW(),
    NOW()
FROM profils_etudiants pe
WHERE pe.matricule BETWEEN 'ETU20240001' AND 'ETU20240025'
ON CONFLICT (etudiant_id, annee_scolaire_id) DO NOTHING;

COMMIT;