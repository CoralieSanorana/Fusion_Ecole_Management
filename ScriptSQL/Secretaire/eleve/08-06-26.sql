-- Remplir est_admis basé sur la moyenne de l'étudiant
-- Logique: si moyenne >= 10, admis = true, sinon false

-- 1. Créer une table temporaire avec moyennes par étudiant
WITH moyennes_par_etudiant AS (
    SELECT 
        i.id as inscription_id,
        i.etudiant_id,
        COALESCE(AVG(n.valeur), 0) as moyenne
    FROM inscriptions i
    LEFT JOIN notes n ON n.etudiant_id = i.etudiant_id
    GROUP BY i.id, i.etudiant_id
)
-- 2. Mettre à jour est_admis
UPDATE inscriptions i
SET est_admis = (
    CASE 
        WHEN m.moyenne >= 10 THEN true
        ELSE false
    END
)
FROM moyennes_par_etudiant m
WHERE i.id = m.inscription_id;

-- Vérification
SELECT est_admis, COUNT(*) FROM inscriptions GROUP BY est_admis;
