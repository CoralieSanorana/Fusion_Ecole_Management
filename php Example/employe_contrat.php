<?php
  $pageTitle = 'Contrat employé';
  $activePage = 'dir-employes';
  $activeRole = 'directeur';
  $userName = 'Direction';
  $userRole = 'Directeur';
  $userInitials = 'DR';
?>

<?= view('inc/header', ['pageTitle' => $pageTitle, 'activePage' => $activePage]) ?>

<section class="page-section active" id="dir-employe-contrat">
  <style>
    .contract-cv {
      max-width: 1080px;
      margin: 0 auto;
      background: linear-gradient(180deg, #f8fafc 0%, #ffffff 65%);
      border: 1px solid #e2e8f0;
      border-radius: 18px;
      box-shadow: 0 14px 38px rgba(15, 23, 42, 0.08);
      overflow: hidden;
    }

    .contract-cv-header {
      padding: 28px;
      color: #fff;
      background: linear-gradient(120deg, #0f172a, #1e293b 60%, #334155);
      display: grid;
      grid-template-columns: 160px 1fr;
      gap: 22px;
      align-items: center;
    }

    .contract-photo-wrap {
      width: 150px;
      height: 150px;
      border-radius: 50%;
      border: 5px solid rgba(255, 255, 255, 0.35);
      overflow: hidden;
      background: #fff;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
      cursor: pointer;
      position: relative;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .contract-photo-wrap:hover {
      transform: translateY(-2px) scale(1.01);
      box-shadow: 0 14px 34px rgba(0, 0, 0, 0.3);
    }

    .contract-photo-overlay {
      position: absolute;
      inset: auto 0 0 0;
      padding: 8px;
      background: linear-gradient(180deg, rgba(15, 23, 42, 0) 0%, rgba(15, 23, 42, 0.72) 100%);
      color: #fff;
      font-size: 12px;
      text-align: center;
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: none;
    }

    .contract-photo-wrap:hover .contract-photo-overlay {
      opacity: 1;
    }

    .contract-photo-wrap img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .contract-cv-title h2 {
      margin: 0;
      font-size: 34px;
      letter-spacing: 0.3px;
    }

    .contract-cv-title p {
      margin: 8px 0 0;
      color: #cbd5e1;
      font-size: 15px;
    }

    .contract-badges {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      margin-top: 14px;
    }

    .contract-badge {
      background: rgba(255, 255, 255, 0.14);
      border: 1px solid rgba(255, 255, 255, 0.25);
      color: #e2e8f0;
      font-size: 12px;
      font-weight: 600;
      padding: 7px 11px;
      border-radius: 999px;
      text-transform: uppercase;
      letter-spacing: 0.6px;
    }

    .contract-cv-body {
      display: grid;
      grid-template-columns: 1fr 1.2fr;
      gap: 0;
    }

    .contract-panel {
      padding: 24px 26px;
      border-right: 1px solid #e2e8f0;
    }

    .contract-panel:last-child {
      border-right: none;
    }

    .contract-section {
      margin-bottom: 24px;
    }

    .contract-section h3 {
      margin: 0 0 12px;
      font-size: 14px;
      color: #0f172a;
      letter-spacing: 0.8px;
      text-transform: uppercase;
      border-bottom: 2px solid #e2e8f0;
      padding-bottom: 8px;
    }

    .contract-list {
      display: grid;
      gap: 10px;
    }

    .contract-item {
      display: grid;
      grid-template-columns: 140px 1fr;
      gap: 10px;
      align-items: baseline;
      font-size: 14px;
      color: #334155;
    }

    .contract-item strong {
      color: #0f172a;
      font-weight: 700;
    }

    .upload-box {
      border: 1px dashed #cbd5e1;
      border-radius: 12px;
      padding: 14px;
      background: #f8fafc;
    }

    .upload-box .form-row {
      margin: 0;
      gap: 8px;
      display: grid;
      grid-template-columns: 1fr 120px;
    }

    .photo-actions-sheet {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.58);
      display: none;
      align-items: flex-end;
      justify-content: center;
      padding: 18px;
      z-index: 1200;
    }

    .photo-actions-sheet.is-open {
      display: flex;
    }

    .photo-actions-card {
      width: min(420px, 100%);
      background: #fff;
      border-radius: 22px;
      box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
      overflow: hidden;
    }

    .photo-actions-header {
      padding: 14px 18px;
      font-size: 13px;
      color: #64748b;
      text-align: center;
      border-bottom: 1px solid #e2e8f0;
      background: #f8fafc;
    }

    .photo-actions-list {
      display: grid;
    }

    .photo-action-btn,
    .photo-action-danger {
      width: 100%;
      padding: 16px 18px;
      border: 0;
      background: #fff;
      font-size: 15px;
      font-weight: 600;
      text-align: left;
      cursor: pointer;
      border-top: 1px solid #eef2f7;
    }

    .photo-action-btn:hover {
      background: #f8fafc;
    }

    .photo-action-danger {
      color: #b91c1c;
    }

    .photo-action-danger:hover {
      background: #fef2f2;
    }

    .photo-action-note {
      padding: 0 18px 16px;
      font-size: 12px;
      color: #64748b;
      text-align: center;
    }

    .contract-highlight {
      margin-top: 8px;
      padding: 14px;
      border-radius: 12px;
      background: linear-gradient(120deg, #e2e8f0, #f8fafc);
      border: 1px solid #cbd5e1;
      color: #0f172a;
    }

    .contract-highlight .value {
      font-size: 24px;
      font-weight: 700;
      margin: 6px 0 2px;
    }

    @media (max-width: 900px) {
      .contract-cv-header {
        grid-template-columns: 1fr;
        text-align: center;
      }

      .contract-photo-wrap {
        margin: 0 auto;
      }

      .contract-badges {
        justify-content: center;
      }

      .contract-cv-body {
        grid-template-columns: 1fr;
      }

      .contract-panel {
        border-right: none;
        border-top: 1px solid #e2e8f0;
      }

      .contract-item {
        grid-template-columns: 1fr;
      }
    }
  </style>

  <div class="page-header">
    <div>
      <h2>Contrat employé</h2>
      <p>Fiche professionnelle complète</p>
    </div>
    <a class="btn btn-secondary" href="/directeur/employes">Retour</a>
  </div>

  <article class="contract-cv">
    <header class="contract-cv-header">
      <div class="contract-photo-wrap" id="employee-photo-trigger" role="button" tabindex="0" aria-label="Modifier la photo de l'employé">
        <img src="<?= esc(base_url($employe['photo_url'] ?? (($employe['sexe'] ?? 'H') === 'F' ? 'photo/photo-employes/DefaultIMG_Femme.png' : 'photo/photo-employes/DefaultIMG_Homme.png'))) ?>" alt="Photo employé">
        <div class="contract-photo-overlay">Appuyer pour modifier</div>
      </div>
      <div class="contract-cv-title">
        <h2><?= esc(trim(($employe['prenom'] ?? '') . ' ' . ($employe['nom'] ?? ''))) ?></h2>
        <p>Dossier administratif et contractuel de l'employé</p>
        <div class="contract-badges">
          <span class="contract-badge"><?= esc(ucfirst($employe['fonction'] ?? 'Employé')) ?></span>
          <span class="contract-badge"><?= !empty($employe['is_active']) ? 'Compte actif' : 'Compte inactif' ?></span>
          <span class="contract-badge"><?= esc($employe['type_contrat_libelle'] ?? 'Sans type') ?></span>
        </div>
      </div>
    </header>

    <div class="contract-cv-body">
      <section class="contract-panel">
        <div class="contract-section">
          <h3>Photo Employé</h3>
          <div class="upload-box">
            <p style="margin:0;font-size:13px;color:#475569;">Appuyez sur la photo pour ajouter ou supprimer une image, comme sur Instagram.</p>
          </div>
          <p style="margin-top:10px;font-size:12px;color:#64748b;">La photo est lue depuis le profil de l'employé. Si aucune photo n'est enregistrée, l'image par défaut Homme/Femme s'affiche automatiquement.</p>
        </div>

        <div class="contract-section">
          <h3>Identité</h3>
          <div class="contract-list">
            <div class="contract-item"><strong>Email</strong><span><?= esc($employe['email'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Sexe</strong><span><?= ($employe['sexe'] ?? 'H') === 'F' ? 'Féminin' : 'Masculin' ?></span></div>
            <div class="contract-item"><strong>Rôle</strong><span><?= esc($employe['role_nom'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Matière</strong><span><?= esc($employe['matiere_nom'] ?? '—') ?></span></div>
          </div>
        </div>
      </section>

      <section class="contract-panel">
        <div class="contract-section">
          <h3>Contrat</h3>
          <div class="contract-list">
            <div class="contract-item"><strong>Référence</strong><span><?= esc($employe['reference_contrat'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Type</strong><span><?= esc($employe['type_contrat_libelle'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Début</strong><span><?= esc($employe['date_debut'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Fin</strong><span><?= esc($employe['date_fin'] ?? '—') ?></span></div>
            <div class="contract-item"><strong>Salaire mensuel</strong><span><?= !empty($employe['salaire_mensuel']) ? number_format((float) $employe['salaire_mensuel'], 0, ',', ' ') . ' Ar' : '—' ?></span></div>
            <div class="contract-item"><strong>Heures hebdo</strong><span><?= esc($employe['heures_hebdo'] ?? '—') ?></span></div>
          </div>
          <div class="contract-highlight">
            <div>Durée restante du contrat</div>
            <div class="value"><?= esc($employe['texte_restant'] ?? '—') ?></div>
            <small>Calculé à partir de la date actuelle</small>
          </div>
        </div>

        <div class="contract-section" style="margin-bottom:0;">
          <h3>Résumé administratif</h3>
          <div class="contract-list">
            <div class="contract-item"><strong>Compte utilisateur</strong><span><?= !empty($employe['is_active']) ? 'Actif' : 'Inactif' ?></span></div>
            <div class="contract-item"><strong>Fonction</strong><span><?= esc(ucfirst($employe['fonction'] ?? '—')) ?></span></div>
            <div class="contract-item"><strong>Document</strong><span><?= !empty($employe['document_url']) ? esc($employe['document_url']) : 'Non renseigné' ?></span></div>
          </div>
        </div>
      </section>
    </div>
  </article>
</section>

  <div class="photo-actions-sheet" id="photo-actions-sheet" aria-hidden="true">
    <div class="photo-actions-card" role="dialog" aria-modal="true" aria-labelledby="photo-actions-title">
      <div class="photo-actions-header" id="photo-actions-title">Choisir une action pour la photo</div>
      <div class="photo-actions-list">
        <button class="photo-action-btn" type="button" id="photo-add-btn">Ajouter une nouvelle photo</button>
        <form method="post" action="/directeur/employes/contrat/<?= esc($employe['user_id']) ?>/photo/delete" style="margin:0;">
          <?= csrf_field() ?>
          <button class="photo-action-danger" type="submit">Supprimer la photo actuelle</button>
        </form>
        <button class="photo-action-btn" type="button" id="photo-cancel-btn" style="text-align:center;color:#334155;">Annuler</button>
      </div>
      <div class="photo-action-note">La nouvelle image sera automatiquement compressée pour rester nette et légère.</div>
    </div>
  </div>

  <form id="photo-upload-form" method="post" action="/directeur/employes/contrat/<?= esc($employe['user_id']) ?>/photo" enctype="multipart/form-data" style="display:none;">
    <?= csrf_field() ?>
    <input id="photo-upload-input" class="form-control" type="file" name="photo_employe" accept=".jpg,.jpeg,.png,.webp" required>
  </form>

  <script>
    document.addEventListener('DOMContentLoaded', () => {
      const photoTrigger = document.getElementById('employee-photo-trigger');
      const photoSheet = document.getElementById('photo-actions-sheet');
      const addButton = document.getElementById('photo-add-btn');
      const cancelButton = document.getElementById('photo-cancel-btn');
      const uploadForm = document.getElementById('photo-upload-form');
      const uploadInput = document.getElementById('photo-upload-input');

      function openSheet() {
        if (!photoSheet) return;
        photoSheet.classList.add('is-open');
        photoSheet.setAttribute('aria-hidden', 'false');
      }

      function closeSheet() {
        if (!photoSheet) return;
        photoSheet.classList.remove('is-open');
        photoSheet.setAttribute('aria-hidden', 'true');
      }

      if (photoTrigger) {
        photoTrigger.addEventListener('click', openSheet);
        photoTrigger.addEventListener('keydown', (event) => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openSheet();
          }
        });
      }

      if (cancelButton) {
        cancelButton.addEventListener('click', closeSheet);
      }

      if (photoSheet) {
        photoSheet.addEventListener('click', (event) => {
          if (event.target === photoSheet) {
            closeSheet();
          }
        });
      }

      if (addButton && uploadInput) {
        addButton.addEventListener('click', () => {
          closeSheet();
          uploadInput.click();
        });
      }

      if (uploadInput && uploadForm) {
        uploadInput.addEventListener('change', () => {
          if (uploadInput.files && uploadInput.files.length > 0) {
            uploadForm.submit();
          }
        });
      }
    });
  </script>

<?= view('inc/footer') ?>