<?php

namespace App\Controllers\Directeur;

use App\Controllers\BaseController;
use App\Models\Directeur\MatiereModel;

class EmployesController extends BaseController
{
    public function index(): string
    {
        $rows = db_connect()->table('vue_employes_detail')
            ->orderBy('user_id', 'ASC')
            ->get()->getResultArray();

        $employes = [];
        foreach ($rows as $row) {
            $id = (int) $row['user_id'];
            if (!isset($employes[$id])) {
                $employes[$id] = [
                    'id' => $id,
                    'email' => $row['email'],
                    'is_active' => (bool) $row['is_active'],
                    'nom' => $row['nom'] ?? '',
                    'prenom' => $row['prenom'] ?? '',
                    'fonction' => $row['fonction'] ?? '',
                    'reference_contrat' => $row['reference_contrat'] ?? '',
                    'salaire_mensuel' => $row['salaire_mensuel'] ?? null,
                    'date_debut' => $row['date_debut'] ?? null,
                    'date_fin' => $row['date_fin'] ?? null,
                    'jours_restants' => $row['jours_restants'] ?? null,
                    'matiere_nom' => $row['matiere_nom'] ?? '',
                    'roles' => [],
                ];
            }
            if (!empty($row['role_nom'])) {
                $employes[$id]['roles'][] = $row['role_nom'];
            }
        }

        return view('directeur/employes/index', [
            'employes' => array_values($employes),
            'matieres' => (new MatiereModel())->orderBy('nom', 'ASC')->findAll(),
            'typesContrats' => db_connect()->table('types_contrats_employes')
                ->where('est_actif', true)
                ->orderBy('libelle', 'ASC')
                ->get()->getResultArray(),
            'fonctions' => [
                'professeur' => 'Professeur',
                'secretariat' => 'Secrétaire',
                'comptable' => 'Comptable',
                'directeur' => 'Directeur',
            ],
        ]);
    }

    public function store()
    {
        $db = db_connect();

        $fonction = trim((string) $this->request->getPost('fonction'));
        $email = trim((string) $this->request->getPost('email'));
        $password = trim((string) ($this->request->getPost('password') ?: 'ChangeMe123!'));
        $nom = trim((string) $this->request->getPost('nom'));
        $prenom = trim((string) $this->request->getPost('prenom'));
        $telephone = trim((string) ($this->request->getPost('telephone') ?: ''));
        $adresse = trim((string) ($this->request->getPost('adresse') ?: ''));
        $specialite = trim((string) ($this->request->getPost('specialite') ?: ''));
        $sexe = strtoupper(trim((string) ($this->request->getPost('sexe') ?: 'H')));
        $typeContratId = (int) $this->request->getPost('type_contrat_id');
        $matiereId = (int) ($this->request->getPost('matiere_id') ?: 0);
        $dateDebut = $this->request->getPost('date_debut') ?: null;
        $dateFin = $this->request->getPost('date_fin') ?: null;
        $salaire = (float) ($this->request->getPost('salaire_mensuel') ?: 0);
        $heures = (float) ($this->request->getPost('heures_hebdo') ?: 0);

        if (!in_array($sexe, ['H', 'F'], true)) {
            $sexe = 'H';
        }
        $sexeContrat = $this->resolveContractSexCode($sexe);
        $sexeProfil = $sexe;
        $telephoneNormalise = $this->normalizeMalagasyPhone($telephone);

        if ($fonction === '' || $email === '' || $nom === '' || $prenom === '' || $typeContratId < 1) {
            session()->setFlashdata('error', 'Impossible de créer l\'employé : la fonction, l\'email, le prénom, le nom et le type de contrat sont obligatoires.');
            return redirect()->to('/directeur/employes');
        }

        if ($telephone !== '' && $telephoneNormalise === null) {
            session()->setFlashdata('error', 'Impossible de créer l\'employé : le numéro « ' . $telephone . ' » n\'est pas un numéro malgache valide. Utilisez un numéro commençant par 032, 033, 034, 037 ou 038 avec 10 chiffres au total.');
            return redirect()->to('/directeur/employes');
        }

        $existingUser = $db->table('users')->select('id')->where('email', $email)->get()->getRowArray();
        if ($existingUser) {
            session()->setFlashdata('error', 'Impossible de créer l\'employé : l\'adresse email « ' . $email . ' » est déjà utilisée par un autre compte.');
            return redirect()->to('/directeur/employes');
        }

        if ($telephoneNormalise !== null) {
            $duplicatePhone = $this->findDuplicateEmployeePhone($telephoneNormalise);
            if ($duplicatePhone !== null) {
                session()->setFlashdata('error', 'Impossible de créer l\'employé : le numéro « ' . $telephoneNormalise . ' » existe déjà pour ' . $duplicatePhone['label'] . '. Chaque employé doit avoir un numéro unique.');
                return redirect()->to('/directeur/employes');
            }
        }

        $role = $db->table('roles')->select('id')->where('nom', $fonction)->get()->getRowArray();
        if (!$role) {
            session()->setFlashdata('error', 'Impossible de créer l\'employé : la fonction « ' . $fonction . ' » n\'est pas reconnue par le système.');
            return redirect()->to('/directeur/employes');
        }

        $db->transStart();

        try {
            $db->table('users')->insert([
                'email' => $email,
                'password' => password_hash($password, PASSWORD_DEFAULT),
                'is_active' => true,
            ]);
            $userId = (int) $db->insertID();

            $db->table('user_roles')->insert([
                'user_id' => $userId,
                'role_id' => (int) $role['id'],
            ]);

            $db->table('contrats_employes')->insert([
                'user_id' => $userId,
                'fonction' => $fonction,
                'type_contrat_id' => $typeContratId,
                'reference_contrat' => 'CTR-' . date('Ymd') . '-' . strtoupper(substr($fonction, 0, 3)) . '-' . $userId,
                'date_debut' => $dateDebut,
                'date_fin' => $dateFin,
                'salaire_mensuel' => $salaire,
                'heures_hebdo' => $heures,
                'sexe' => $sexeContrat,
                'statut' => 'actif',
            ]);
            $contratId = (int) $db->insertID();

            if ($fonction === 'professeur') {
                $matricule = 'PRF-' . date('Y') . '-' . str_pad((string) $userId, 3, '0', STR_PAD_LEFT);

                $db->table('profils_professeurs')->insert([
                    'user_id' => $userId,
                    'matricule' => $matricule,
                    'nom' => $nom,
                    'prenom' => $prenom,
                    'sexe' => $sexe,
                    'telephone' => $telephoneNormalise,
                    'adresse' => $adresse ?: null,
                    'specialite' => $specialite ?: null,
                    'type_contrat' => $this->resolveTypeContratCode($db, $typeContratId),
                    'date_debut_contrat' => $dateDebut,
                    'date_fin_contrat' => $dateFin,
                    'id_contrat' => $contratId,
                    'id_matiere' => $matiereId > 0 ? $matiereId : null,
                    'is_archived' => false,
                ]);
            } elseif ($fonction === 'secretariat') {
                $db->table('profils_secretariat')->insert([
                    'user_id' => $userId,
                    'nom' => $nom,
                    'prenom' => $prenom,
                    'telephone' => $telephoneNormalise,
                    'sexe' => $sexe,
                    'id_contrat' => $contratId,
                ]);
            } elseif ($fonction === 'directeur') {
                $db->table('profils_directeurs')->insert([
                    'user_id' => $userId,
                    'nom' => $nom,
                    'prenom' => $prenom,
                    'telephone' => $telephoneNormalise,
                    'sexe' => $sexe,
                    'id_contrat' => $contratId,
                ]);
            } elseif ($fonction === 'comptable') {
                $db->table('profils_comptables')->insert([
                    'user_id' => $userId,
                    'nom' => $nom,
                    'prenom' => $prenom,
                    'telephone' => $telephoneNormalise,
                    'sexe' => $sexe,
                    'id_contrat' => $contratId,
                ]);
            }

            $db->transComplete();

            if ($db->transStatus() === false) {
                session()->setFlashdata('error', 'Impossible de terminer la création de l\'employé : une erreur de base de données a interrompu l\'enregistrement.');
            } else {
                session()->setFlashdata('success', 'Employé créé avec succès.');
            }
        } catch (\Throwable $throwable) {
            $db->transRollback();
            $message = $this->describeEmployeeInsertError($throwable, $email, $fonction);
            session()->setFlashdata('error', $message);
        }

        return redirect()->to('/directeur/employes');
    }

    public function checkEmail()
    {
        $email = trim((string) $this->request->getGet('email'));
        if ($email === '') {
            return $this->response->setJSON([
                'exists' => false,
                'message' => '',
            ]);
        }

        $exists = (bool) db_connect()->table('users')
            ->select('id')
            ->where('email', $email)
            ->get()
            ->getRowArray();

        return $this->response->setJSON([
            'exists' => $exists,
            'message' => $exists
                ? 'Cette adresse email est déjà utilisée. Choisissez une autre adresse avant d\'enregistrer l\'employé.'
                : 'Cette adresse email est disponible.',
        ]);
    }

    public function checkPhone()
    {
        $telephone = trim((string) $this->request->getGet('telephone'));
        if ($telephone === '') {
            return $this->response->setJSON([
                'valid' => true,
                'exists' => false,
                'normalized' => '',
                'message' => '',
            ]);
        }

        $normalized = $this->normalizeMalagasyPhone($telephone);
        if ($normalized === null) {
            return $this->response->setJSON([
                'valid' => false,
                'exists' => false,
                'normalized' => '',
                'message' => 'Numéro invalide. Utilisez un numéro malgache à 10 chiffres commençant par 032, 033, 034, 037 ou 038.',
            ]);
        }

        $duplicatePhone = $this->findDuplicateEmployeePhone($normalized);
        if ($duplicatePhone !== null) {
            return $this->response->setJSON([
                'valid' => true,
                'exists' => true,
                'normalized' => $normalized,
                'message' => 'Ce numéro ' . $normalized . ' est déjà utilisé par ' . $duplicatePhone['label'] . '.',
            ]);
        }

        return $this->response->setJSON([
            'valid' => true,
            'exists' => false,
            'normalized' => $normalized,
            'message' => 'Numéro valide et disponible.',
        ]);
    }

    public function contrat(int $id): string
    {
        $employe = db_connect()->table('vue_employes_detail')
            ->where('user_id', $id)
            ->orderBy('contrat_id', 'DESC')
            ->get()->getRowArray();

        if (!$employe) {
            session()->setFlashdata('error', 'Employé introuvable.');
            return redirect()->to('/directeur/employes');
        }

        $dateFin = !empty($employe['date_fin']) ? new \DateTimeImmutable($employe['date_fin']) : null;
        $today = new \DateTimeImmutable('today');

        $employe['jours_restants'] = $dateFin ? max(0, (int) $today->diff($dateFin)->format('%r%a')) : null;
        $employe['texte_restant'] = $dateFin
            ? ($employe['jours_restants'] > 0 ? $employe['jours_restants'] . ' jour(s) restants' : 'Contrat arrivé à échéance')
            : 'Contrat sans date de fin';

        $employe['photo_url'] = $this->resolvePhotoUrl($employe);
        $employe['sexe'] = !empty($employe['sexe']) ? $employe['sexe'] : 'H';

        return view('directeur/employes/employe_contrat', ['employe' => $employe]);
    }

    public function uploadPhoto(int $id)
    {
        $db = db_connect();
        $employe = $db->table('vue_employes_detail')->where('user_id', $id)->get()->getRowArray();
        if (!$employe) {
            return redirect()->to('/directeur/employes');
        }

        $file = $this->request->getFile('photo_employe');

        if (!$file || !$file->isValid() || $file->hasMoved()) {
            session()->setFlashdata('error', 'Aucun fichier photo valide n\'a été fourni.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $ext = strtolower((string) $file->getExtension());
        if (!in_array($ext, ['jpg', 'jpeg', 'png', 'webp'], true)) {
            session()->setFlashdata('error', 'Format de photo non autorisé. Utilisez jpg, jpeg, png ou webp.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $targetDir = FCPATH . 'photo' . DIRECTORY_SEPARATOR . 'photo-employes';
        if (!is_dir($targetDir)) {
            mkdir($targetDir, 0755, true);
        }

        $filename = 'employe_' . $id . '_' . date('YmdHis') . '.webp';
        $destinationPath = $targetDir . DIRECTORY_SEPARATOR . $filename;

        if (!$this->optimizeEmployeePhoto($file->getTempName(), $destinationPath, $ext)) {
            session()->setFlashdata('error', 'Impossible de traiter l\'image. Vérifiez que le fichier est bien une photo valide.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $photoUrl = 'photo/photo-employes/' . $filename;

        $updatedPhoto = false;
        foreach (['profils_professeurs', 'profils_secretariat', 'profils_directeurs', 'profils_comptables'] as $tableName) {
            if ($this->tableHasColumn($db, $tableName, 'photo_url')) {
                $db->table($tableName)->where('user_id', $id)->update([
                    'photo_url' => $photoUrl,
                ]);
                $updatedPhoto = true;
            }
        }

        if (!$updatedPhoto) {
            $this->removeEmployeePhotoFile($photoUrl);
            session()->setFlashdata('error', 'Votre base de données ne contient pas encore de colonne photo_url pour enregistrer cette image. Appliquez le script de mise à jour du schéma, puis réessayez.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $this->removeEmployeePhotoFile($employe['photo_url'] ?? null);

        session()->setFlashdata('success', 'Photo de l\'employé enregistrée avec succès.');
        return redirect()->to('/directeur/employes/contrat/' . $id);
    }

    public function deletePhoto(int $id)
    {
        $db = db_connect();
        $employe = $db->table('vue_employes_detail')->where('user_id', $id)->get()->getRowArray();
        if (!$employe) {
            session()->setFlashdata('error', 'Employé introuvable.');
            return redirect()->to('/directeur/employes');
        }

        $currentPhoto = $employe['photo_url'] ?? null;
        if (empty($currentPhoto)) {
            session()->setFlashdata('error', 'Aucune photo personnalisée n\'est enregistrée pour cet employé.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $updatedPhoto = false;
        foreach (['profils_professeurs', 'profils_secretariat', 'profils_directeurs', 'profils_comptables'] as $tableName) {
            if ($this->tableHasColumn($db, $tableName, 'photo_url')) {
                $db->table($tableName)->where('user_id', $id)->update([
                    'photo_url' => null,
                ]);
                $updatedPhoto = true;
            }
        }

        if (!$updatedPhoto) {
            session()->setFlashdata('error', 'Aucune table de votre schéma ne permet de stocker la photo, donc la suppression ne peut pas être appliquée.');
            return redirect()->to('/directeur/employes/contrat/' . $id);
        }

        $this->removeEmployeePhotoFile($currentPhoto);

        session()->setFlashdata('success', 'La photo de l\'employé a été supprimée avec succès.');
        return redirect()->to('/directeur/employes/contrat/' . $id);
    }

    public function toggleActif(int $id)
    {
        $db = db_connect();
        $user = $db->table('users')->where('id', $id)->get()->getRowArray();
        if (!$user) {
            return redirect()->to('/directeur/employes');
        }

        $db->table('users')->where('id', $id)->update(['is_active' => !((bool) $user['is_active'])]);
        return redirect()->to('/directeur/employes');
    }

    private function resolveTypeContratCode($db, int $typeContratId): string
    {
        $row = $db->table('types_contrats_employes')
            ->select('code')
            ->where('id', $typeContratId)
            ->get()->getRowArray();

        return $row['code'] ?? 'contractuel';
    }

    private function tableHasColumn($db, string $tableName, string $columnName): bool
    {
        $fields = $db->getFieldNames($tableName);
        return is_array($fields) && in_array($columnName, $fields, true);
    }

    private function resolvePhotoUrl(array $employe): string
    {
        if (!empty($employe['photo_url'])) {
            return (string) $employe['photo_url'];
        }

        $sexe = strtoupper((string) ($employe['sexe'] ?? 'H'));
        return $sexe === 'F'
            ? 'photo/photo-employes/DefaultIMG_Femme.png'
            : 'photo/photo-employes/DefaultIMG_Homme.png';
    }

    private function resolveContractSexCode(string $sexe): string
    {
        return $sexe === 'F' ? 'F' : 'H';
    }

    private function optimizeEmployeePhoto(string $sourcePath, string $destinationPath, string $sourceExtension): bool
    {
        if (!is_file($sourcePath)) {
            return false;
        }

        if (!function_exists('imagecreatefromjpeg') || !function_exists('imagecreatetruecolor') || !function_exists('imagecopyresampled')) {
            return false;
        }

        $dimensions = @getimagesize($sourcePath);
        if ($dimensions === false) {
            return false;
        }

        [$width, $height] = $dimensions;
        if ($width < 1 || $height < 1) {
            return false;
        }

        $sourceImage = match ($sourceExtension) {
            'jpg', 'jpeg' => @\imagecreatefromjpeg($sourcePath),
            'png' => @\imagecreatefrompng($sourcePath),
            'webp' => function_exists('imagecreatefromwebp') ? @\imagecreatefromwebp($sourcePath) : false,
            default => false,
        };

        if (!$sourceImage) {
            return false;
        }

        $squareSize = min($width, $height);
        $sourceX = (int) floor(($width - $squareSize) / 2);
        $sourceY = (int) floor(($height - $squareSize) / 2);
        $targetSize = 640;

        $targetImage = \imagecreatetruecolor($targetSize, $targetSize);
        \imagealphablending($targetImage, true);
        \imagesavealpha($targetImage, true);
        $transparent = \imagecolorallocatealpha($targetImage, 255, 255, 255, 127);
        \imagefill($targetImage, 0, 0, $transparent);

        \imagecopyresampled(
            $targetImage,
            $sourceImage,
            0,
            0,
            $sourceX,
            $sourceY,
            $targetSize,
            $targetSize,
            $squareSize,
            $squareSize
        );

        $saved = function_exists('imagewebp')
            ? \imagewebp($targetImage, $destinationPath, 82)
            : \imagejpeg($targetImage, $destinationPath, 82);

        \imagedestroy($sourceImage);
        \imagedestroy($targetImage);

        return $saved;
    }

    private function removeEmployeePhotoFile(?string $photoUrl): void
    {
        if (empty($photoUrl) || str_contains($photoUrl, 'DefaultIMG_')) {
            return;
        }

        $absolutePath = FCPATH . str_replace('/', DIRECTORY_SEPARATOR, ltrim($photoUrl, '/'));
        if (is_file($absolutePath)) {
            @unlink($absolutePath);
        }
    }

    private function normalizeMalagasyPhone(string $telephone): ?string
    {
        $digits = preg_replace('/\D+/', '', $telephone);
        if ($digits === null || $digits === '') {
            return null;
        }

        if (!preg_match('/^0(?:32|33|34|37|38)\d{7}$/', $digits)) {
            return null;
        }

        return $digits;
    }

    private function findDuplicateEmployeePhone(string $normalizedPhone): ?array
    {
        $db = db_connect();
        $tables = [
            ['table' => 'profils_professeurs', 'label' => 'un professeur'],
            ['table' => 'profils_secretariat', 'label' => 'un secrétaire'],
            ['table' => 'profils_directeurs', 'label' => 'un directeur'],
            ['table' => 'profils_comptables', 'label' => 'un comptable'],
        ];

        foreach ($tables as $entry) {
            $row = $db->table($entry['table'])
                ->select('user_id, nom, prenom, telephone')
                ->where("regexp_replace(COALESCE(telephone, ''), '[^0-9]', '', 'g') = '$normalizedPhone'", null, false)
                ->get()
                ->getRowArray();

            if ($row) {
                return [
                    'table' => $entry['table'],
                    'label' => trim(($row['prenom'] ?? '') . ' ' . ($row['nom'] ?? '')) ?: $entry['label'],
                ];
            }
        }

        return null;
    }

    private function describeEmployeeInsertError(\Throwable $throwable, string $email, string $fonction): string
    {
        $message = $throwable->getMessage();

        if (str_contains($message, 'users_email_key')) {
            return 'Impossible de créer l\'employé : l\'adresse email « ' . $email . ' » est déjà enregistrée dans la base.';
        }

        if (str_contains($message, 'sexe_check')) {
            return 'Impossible de créer l\'employé : la valeur du sexe choisie n\'est pas acceptée par la base de données pour la fonction « ' . $fonction . ' ». Choisissez H ou F.';
        }

        if (str_contains($message, 'telephone')) {
            return 'Impossible de créer l\'employé : le numéro de téléphone est invalide ou déjà utilisé. Utilisez un numéro malgache au format 032XXXXXXX, 033XXXXXXX, 034XXXXXXX, 037XXXXXXX ou 038XXXXXXX.';
        }

        return 'Impossible de créer l\'employé : une erreur inattendue est survenue pendant l\'enregistrement. Vérifiez les champs saisis puis réessayez.';
    }

}