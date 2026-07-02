/**
 * EtudiantsFiltre.js
 * Gestion du filtre paginé d'étudiants.
 * Appelle /api/directeur/etudiants/recherche avec tous les critères actifs.
 */
document.addEventListener('DOMContentLoaded', () => {

  let currentPage = 0;
  let debounceTimer = null;

  // ── Éléments DOM ─────────────────────────────────────────────────
  const $ = id => document.getElementById(id);
  const btnToggle  = $('btn-toggle-filters');
  const filterPanel= $('filter-panel');
  const btnReset   = $('btn-reset');
  const pills      = $('filter-pills');
  const tbody      = $('result-tbody');
  const paginInfo  = $('pagination-info');
  const paginCtrl  = $('pagination-controls');

  // ── Filtres ───────────────────────────────────────────────────────
  const inputs = {
    recherche:           $('f-recherche'),
    sexe:                $('f-sexe'),
    region:              $('f-region'),
    niveauId:            $('f-niveau'),
    classeId:            $('f-classe'),
    anneeScolaireId:     $('f-annee'),
    statutInscription:   $('f-statut'),
    dateInscriptionDebut:$('f-insc-debut'),
    dateInscriptionFin:  $('f-insc-fin'),
    dateNaissanceDebut:  $('f-naiss-debut'),
    dateNaissanceFin:    $('f-naiss-fin'),
  };
  const cbArchives = $('f-archives');
  const sortBy     = $('f-sort-by');
  const sortDir    = $('f-sort-dir');
  const pageSize   = $('f-page-size');

  // ── Toggle panneau filtres ────────────────────────────────────────
  btnToggle.addEventListener('click', () => {
    const visible = filterPanel.style.display !== 'none';
    filterPanel.style.display = visible ? 'none' : 'block';
    btnToggle.innerHTML = visible
      ? '<i class="fas fa-sliders-h"></i> Filtres'
      : '<i class="fas fa-times"></i> Masquer';
  });

  // ── Reset ─────────────────────────────────────────────────────────
  btnReset.addEventListener('click', () => {
    Object.values(inputs).forEach(el => { if (el) el.value = ''; });
    cbArchives.checked = false;
    sortBy.value = 'nom';
    sortDir.value = 'asc';
    currentPage = 0;
    rechercher();
  });

  // ── Listeners sur tous les filtres (debounce texte libre) ─────────
  inputs.recherche.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => { currentPage = 0; rechercher(); }, 400);
  });

  [inputs.sexe, inputs.region, inputs.niveauId, inputs.classeId,
   inputs.anneeScolaireId, inputs.statutInscription,
   inputs.dateInscriptionDebut, inputs.dateInscriptionFin,
   inputs.dateNaissanceDebut, inputs.dateNaissanceFin,
   cbArchives, sortBy, sortDir, pageSize
  ].forEach(el => {
    if (!el) return;
    el.addEventListener('change', () => { currentPage = 0; rechercher(); });
  });

  // ── Charger données selects ───────────────────────────────────────
  async function loadFormData() {
    try {
      const res  = await fetch('/api/directeur/etudiants/form-data');
      const data = await res.json();

      populate(inputs.niveauId, data.niveaux, n => ({ v: n.id, l: n.libelle }));
      populate(inputs.classeId, data.classes, c => ({ v: c.id, l: c.nom }));
      populate(inputs.anneeScolaireId, data.annees, a => ({
        v: a.id, l: a.libelle + (a.estActive ? ' ★' : '')
      }));
      populate(inputs.statutInscription, data.statuts.map(s => ({ v: s, l: s })));

      // Pré-sélectionner l'année active
      if (data.anneeActive) {
        inputs.anneeScolaireId.value = data.anneeActive.id;
      }
    } catch (e) {
      console.error('Erreur chargement form-data:', e);
    }
  }

  function populate(select, items, mapper) {
    if (!select) return;
    const first = select.options[0]; // garder "Tous"
    select.innerHTML = '';
    select.appendChild(first);
    items.forEach(item => {
      const m = mapper ? mapper(item) : item;
      const opt = document.createElement('option');
      opt.value = m.v; opt.textContent = m.l;
      select.appendChild(opt);
    });
  }

  // ── Construire les query params ───────────────────────────────────
  function buildParams() {
    const p = new URLSearchParams();
    Object.entries(inputs).forEach(([key, el]) => {
      if (el && el.value) p.set(key, el.value);
    });
    if (cbArchives.checked) p.set('isArchived', '');  // null → tous
    p.set('page',     String(currentPage));
    p.set('pageSize', pageSize.value);
    p.set('sortBy',   sortBy.value);
    p.set('sortDir',  sortDir.value);
    return p;
  }

  // ── Requête principale ────────────────────────────────────────────
  async function rechercher() {
    tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;padding:2rem;color:var(--txt3);">
      <i class="fas fa-spinner fa-spin"></i> Chargement…</td></tr>`;

    try {
      const res  = await fetch('/api/directeur/etudiants/recherche?' + buildParams());
      const data = await res.json();
      renderTable(data);
      renderPagination(data);
      renderStats(data);
      renderPills();
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;color:var(--danger);">
        Erreur de chargement</td></tr>`;
      console.error(e);
    }
  }

  // ── Render tableau ────────────────────────────────────────────────
  function renderTable(data) {
    if (!data.content || data.content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="10" style="text-align:center;padding:2.5rem;color:var(--txt3);">
        <i class="fas fa-search" style="font-size:2rem;opacity:0.3;display:block;margin-bottom:0.5rem;"></i>
        Aucun étudiant correspondant aux critères</td></tr>`;
      return;
    }

    const STATUT_BADGE = {
      active:      'badge-green',
      transfere:   'badge-amber',
      exclu:       'badge-red',
      diplome:     'badge-violet',
      abandonne:   'badge-navy',
    };

    tbody.innerHTML = data.content.map(e => `
      <tr style="cursor:pointer;" onclick="window.location.href='/directeur/profil_etudiant?id=${e.etudiantId}'">
        <td><code style="font-size:0.78rem;">${esc(e.matricule)}</code></td>
        <td>
          <div style="font-weight:600;">${esc(e.nom)} ${esc(e.prenom)}</div>
          <div style="font-size:0.75rem;color:var(--txt3);">${esc(e.email || '—')}</div>
        </td>
        <td style="font-size:0.82rem;">${formatDate(e.dateNaissance)}</td>
        <td>${e.sexe === 'M' ? '♂ M' : e.sexe === 'F' ? '♀ F' : '—'}</td>
        <td>${esc(e.region || '—')}</td>
        <td>${esc(e.classeNom || '—')}</td>
        <td>${esc(e.niveauLibelle || '—')}</td>
        <td><span style="font-size:0.78rem;">${esc(e.anneeLibelle || '—')}</span></td>
        <td>${e.statutInscription
          ? `<span class="badge ${STATUT_BADGE[e.statutInscription] || 'badge-navy'}">${esc(e.statutInscription)}</span>`
          : '—'}</td>
        <td style="font-size:0.82rem;">${formatDate(e.dateInscription)}</td>
      </tr>`
    ).join('');
  }

  // ── Render pagination ─────────────────────────────────────────────
  function renderPagination(data) {
    const { currentPage: cp, totalPages, hasPrevious, hasNext, totalElements, pageSize: ps } = data;
    const from = cp * ps + 1;
    const to   = Math.min((cp + 1) * ps, totalElements);

    paginInfo.textContent = totalElements > 0
      ? `${from}–${to} sur ${totalElements} étudiant${totalElements > 1 ? 's' : ''}`
      : '';

    const btn = (label, page, disabled) => {
      const b = document.createElement('button');
      b.className = 'btn';
      b.style.cssText = `padding:0.4rem 0.8rem;font-size:0.82rem;
        background:${disabled ? 'var(--surf2)' : 'var(--surf3)'};
        color:${disabled ? 'var(--txt3)' : 'var(--txt)'};
        border:1px solid var(--border);`;
      b.innerHTML = label;
      b.disabled = disabled;
      if (!disabled) b.addEventListener('click', () => { currentPage = page; rechercher(); });
      return b;
    };

    paginCtrl.innerHTML = '';
    paginCtrl.appendChild(btn('<i class="fas fa-angle-double-left"></i>', 0, !hasPrevious));
    paginCtrl.appendChild(btn('<i class="fas fa-angle-left"></i>', cp - 1, !hasPrevious));

    // Pages centrales
    const range = pageRange(cp, totalPages);
    range.forEach(p => {
      const b = btn(p === '…' ? '…' : p + 1, p, p === '…' || p === cp);
      if (p === cp) b.style.background = 'var(--g)'; b.style.color = 'var(--ink)';
      paginCtrl.appendChild(b);
    });

    paginCtrl.appendChild(btn('<i class="fas fa-angle-right"></i>', cp + 1, !hasNext));
    paginCtrl.appendChild(btn('<i class="fas fa-angle-double-right"></i>', totalPages - 1, !hasNext));
  }

  function pageRange(current, total) {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    if (current < 4) return [0,1,2,3,4,'…',total-1];
    if (current > total - 5) return [0,'…',total-5,total-4,total-3,total-2,total-1];
    return [0,'…',current-1,current,current+1,'…',total-1];
  }

  // ── Stats ─────────────────────────────────────────────────────────
  function renderStats(data) {
    $('stat-total').textContent  = data.totalElements;
    $('stat-page').textContent   = data.content?.length || 0;
    $('stat-pages').textContent  = data.totalPages;
    $('result-count').textContent = `${data.totalElements} étudiant${data.totalElements !== 1 ? 's' : ''} trouvé${data.totalElements !== 1 ? 's' : ''}`;
  }

  // ── Pills (filtres actifs visuels) ────────────────────────────────
  const PILL_LABELS = {
    recherche: 'Recherche', sexe: 'Sexe', region: 'Région',
    niveauId: 'Niveau', classeId: 'Classe', anneeScolaireId: 'Année',
    statutInscription: 'Statut',
    dateInscriptionDebut: 'Inscrit après', dateInscriptionFin: 'Inscrit avant',
    dateNaissanceDebut: 'Né après', dateNaissanceFin: 'Né avant',
  };

  function renderPills() {
    pills.innerHTML = '';
    Object.entries(inputs).forEach(([key, el]) => {
      if (!el || !el.value) return;
      const pill = document.createElement('span');
      pill.style.cssText = `display:inline-flex;align-items:center;gap:0.3rem;
        padding:0.2rem 0.6rem;border-radius:9999px;font-size:0.75rem;
        background:var(--surf2);border:1px solid var(--border);color:var(--txt);`;
      pill.innerHTML = `<strong>${PILL_LABELS[key] || key}:</strong> ${esc(el.value)}
        <span style="cursor:pointer;color:var(--txt3);margin-left:0.2rem;"
              onclick="document.getElementById('f-${key.replace(/([A-Z])/g,'-$1').toLowerCase()}').value='';
                       rechercher();">✕</span>`;
      pills.appendChild(pill);
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────
  function esc(s) {
    if (!s) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }

  function formatDate(d) {
    if (!d) return '—';
    try { return new Date(d).toLocaleDateString('fr-MG'); } catch { return d; }
  }

  // ── Init ──────────────────────────────────────────────────────────
  loadFormData().then(() => rechercher());
});
