ALTER TABLE profils_professeurs DROP CONSTRAINT IF EXISTS profils_professeurs_sexe_check;
ALTER TABLE profils_professeurs ADD CONSTRAINT profils_professeurs_sexe_check CHECK (sexe IN ('H', 'F'));
