<?php
  $pageTitle = 'Employés';
  $activePage = 'dir-employes';
  $activeRole = 'directeur';
  $userName = 'Direction';
  $userRole = 'Directeur';
  $userInitials = 'DR';
?>

<?= view('inc/header', ['pageTitle' => $pageTitle, 'activePage' => $activePage]) ?>

<section class="page-section active" id="dir-employes">
  <div class="page-header">
    <div>
      <h2>Employés</h2>
      <p>Comptes du personnel, contrats et salaires</p>
    </div>
    <div style="display:flex;gap:var(--sp-sm);flex-wrap:wrap;">
      <button class="btn btn-primary" type="button" onclick="toggleEmployeeForm()">Ajouter employé</button>
      <a class="btn btn-secondary" href="/directeur/setup">Retour setup</a>
    </div>
  </div>

  <div class="card" id="employee-form-panel" style="display:none;margin-bottom:var(--sp-xl);">
    <div class="card-header">
      <h3>Nouvel employé</h3>
    </div>
    <div class="card-body">
      <form method="post" action="/directeur/employes">
        <?= csrf_field() ?>
        <div class="form-row">
          <div class="form-group">
            <label>Fonction</label>
            <select class="form-control" name="fonction" id="employee-fonction" onchange="syncEmployeeFields()" required>
              <option value="">Choisir</option>
              <?php foreach ($fonctions as $code => $label): ?>
                <option value="<?= esc($code) ?>"><?= esc($label) ?></option>
              <?php endforeach; ?>
            </select>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input class="form-control" type="email" name="email" id="employee-email" placeholder="prenom.nom@ecole.local" required>
            <small id="employee-email-feedback" style="display:block;margin-top:6px;font-size:12px;min-height:16px;"></small>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Prénom</label>
            <input class="form-control" type="text" name="prenom" placeholder="Jean" required>
          </div>
          <div class="form-group">
            <label>Nom</label>
            <input class="form-control" type="text" name="nom" placeholder="Rakoto" required>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Sexe</label>
            <select class="form-control" name="sexe">
              <option value="M">Masculin</option>
              <option value="F">Féminin</option>
            </select>
          </div>
          <div class="form-group">
            <label>Téléphone</label>
            <input class="form-control" type="text" name="telephone" id="employee-telephone" placeholder="034 00 000 00" inputmode="tel" autocomplete="tel">
            <small id="employee-telephone-feedback" style="display:block;margin-top:6px;font-size:12px;min-height:16px;"></small>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Mot de passe initial</label>
            <input class="form-control" type="text" name="password" placeholder="ChangeMe123!">
          </div>
          <div class="form-group"></div>
        </div>

        <div class="form-group">
          <label>Adresse</label>
          <input class="form-control" type="text" name="adresse" placeholder="Quartier, ville">
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Type de contrat</label>
            <select class="form-control" name="type_contrat_id" required>
              <option value="">Choisir</option>
              <?php foreach ($typesContrats as $typeContrat): ?>
                <option value="<?= esc($typeContrat['id']) ?>"><?= esc($typeContrat['libelle']) ?></option>
              <?php endforeach; ?>
            </select>
          </div>
          <div class="form-group">
            <label>Salaire mensuel</label>
            <input class="form-control" type="number" min="0" step="0.01" name="salaire_mensuel" placeholder="320000">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Date de début</label>
            <input class="form-control" type="date" name="date_debut">
          </div>
          <div class="form-group">
            <label>Date de fin</label>
            <input class="form-control" type="date" name="date_fin">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group" id="employee-matiere-group" style="display:none;">
            <label>Matière enseignée</label>
            <select class="form-control" name="matiere_id">
              <option value="">Choisir</option>
              <?php foreach ($matieres as $matiere): ?>
                <option value="<?= esc($matiere['id']) ?>"><?= esc($matiere['nom']) ?></option>
              <?php endforeach; ?>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Heures hebdo</label>
            <input class="form-control" type="number" min="0" step="0.5" name="heures_hebdo" placeholder="18">
          </div>
          <div class="form-group">
            <label>Spécialité / note interne</label>
            <input class="form-control" type="text" name="specialite" placeholder="Mathématiques">
          </div>
        </div>

        <button class="btn btn-primary" type="submit">Enregistrer l'employé</button>
        <div id="employee-form-error" style="display:none;margin-top:10px;padding:10px 12px;border-radius:10px;background:#fef2f2;border:1px solid #fecaca;color:#b91c1c;font-size:13px;"></div>
      </form>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <h3>Liste des employés</h3>
    </div>
    <div class="card-body">
      <table style="width:100%;border-collapse:collapse;">
        <thead>
          <tr>
            <th align="left">Nom</th>
            <th align="left">Email</th>
            <th align="left">Fonction</th>
            <th align="left">Matière</th>
            <th align="left">Salaire</th>
            <th align="left">Rôles</th>
            <th align="left">Actif</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <?php foreach ($employes as $employe): ?>
            <tr>
              <td><?= esc(trim(($employe['prenom'] ?? '') . ' ' . ($employe['nom'] ?? ''))) ?: '—' ?></td>
              <td><?= esc($employe['email']) ?></td>
              <td><?= esc($fonctions[$employe['fonction']] ?? ucfirst($employe['fonction'] ?? '—')) ?></td>
              <td><?= esc($employe['matiere_nom'] ?? '—') ?></td>
              <td><?= !empty($employe['salaire_mensuel']) ? number_format((float) $employe['salaire_mensuel'], 0, ',', ' ') . ' Ar' : '—' ?></td>
              <td><?= esc(implode(', ', array_unique($employe['roles'] ?? []))) ?></td>
              <td><?= !empty($employe['is_active']) ? 'Oui' : 'Non' ?></td>
              <td style="white-space:nowrap;">
                <a href="/directeur/employes/contrat/<?= esc($employe['id']) ?>" style="color:#2563eb;margin-right:12px;">Contrat</a>
                <a href="/directeur/employes/toggle/<?= esc($employe['id']) ?>" style="color:#2563eb;">Basculer</a>
              </td>
            </tr>
          <?php endforeach; ?>
        </tbody>
      </table>
    </div>
  </div>
</section>

<script src="/assets/Directeur-js/Employe/Employe.js"></script>

<?= view('inc/modals') ?>
<?= view('inc/footer') ?>