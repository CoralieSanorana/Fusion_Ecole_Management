-- TRUNCATE TABLE user_roles CASCADE;
-- TRUNCATE TABLE users CASCADE;

-- 1. Un user secrétaire (mot de passe : "secret123" hashé en BCrypt)
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

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'rakoto@ecole.mg'),
    3 -- secretariat
);

-- Lui donner les 2 rôles
INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'rakoto@ecole.mg'),
    5  -- professeur
);

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'directeur@ecole.mg'),
    2 -- directeur
);

INSERT INTO user_roles (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'rakoto@ecole.mg'),
    3  -- secretariat
);

-- Vérifier
SELECT u.email, r.nom 
FROM users u
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id
WHERE u.email = 'rakoto@ecole.mg';