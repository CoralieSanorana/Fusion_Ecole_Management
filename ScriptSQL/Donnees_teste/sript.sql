-- TRUNCATE TABLE user_roles CASCADE;
-- TRUNCATE TABLE users CASCADE;

INSERT INTO users (email, password, is_active, created_at, updated_at)
VALUES (
    'directeur@ecole.mg',
    '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i',
    true, NOW(), NOW()
);

INSERT INTO users (email, password, is_active, created_at, updatemd_at)
VALUES (
    'rakoto@ecole.mg',
    '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i',
    true, NOW(), NOW()
);

INSERT INTO users (email, password, is_active, created_at, updatemd_at)
VALUES (
    'prof@ecole.mg',
    '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i',
    true, NOW(), NOW()
);

INSERT INTO users (email, password, is_active, created_at, updatemd_at)
VALUES (
    'etudiant@ecole.mg',
    '$2a$10$KDkZGCG.UZjj6LSg1DbT6.qQGpCnUaksEvtKlMZvqs1yYG30g944i',
    true, NOW(), NOW()
);

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'rakoto@ecole.mg'),
    3 -- secretariat
);

-- Lui donner les 2 roles
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'prof@ecole.mg'),
    5  -- professeur
);

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'directeur@ecole.mg'),
    2 -- directeur
);

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'etudiant@ecole.mg'),
    4  -- etudiant
);

