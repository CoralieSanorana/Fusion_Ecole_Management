/**
 * StatistiquesEleves.js
 * Module "Statistiques Élèves" — détection décrochage.
 * Appelle /api/directeur/statistiques-eleves (analyse) et
 * /api/directeur/statistiques-eleves/classes (filtre classe).
 */
document.addEventListener('DOMContentLoaded', () => {

  const $ = id => document.getElementById(id);

  // ── Éléments DOM ─────────────────────────────────────────────────
  const btnToggle    = $('btn-toggle-filters');
  const filterPanel  = $('filter-panel');
  const fClasse      = $('f-classe');
  const fPeriodeFin  = $('f-periode-fin');
  const fSeuilBaisse = $('f-seuil-baisse');
  const fSeuilAbsence= $('f-seuil-absence');

  const resultContext   = $('result-context');
  const statEffectif    = $('stat-effectif');
  const statRouge       = $('stat-rouge');
  const statJaune       = $('stat-jaune');
  const statCorrelation = $('stat-correlation');

  const tbodyRouge = $('tbody-rouge');
  const tbodyJaune = $('tbody-jaune');
  const chartsContainer = $('classes-charts-container');

  const chartInstances = {}; // classeId -> instance Chart.js

  // ── Toggle panneau filtres ────────────────────────────────────────
  btnToggle.addEventListener('click', () => {
    const visible = filterPanel.style.display !== 'none';
    filterPanel.style.display = visible ? 'none' : 'block';
    btnToggle.innerHTML = visible
      ? '<i class="fas fa-sliders-h"></i> Filtres'
      : '<i class="fas fa-times"></i> Masquer';
  });

  // ── Listeners filtres ────────────────────────────────────────────
  [fClasse, fPeriodeFin, fSeuilBaisse, fSeuilAbsence].forEach(el => {
    el.addEventListener('change', () => charger());
  });

  // ── Charger la liste des classes pour le select ───────────────────
  async function chargerClasses() {
    try {
      const res = await fetch('/api/directeur/statistiques-eleves/classes');
      const classes = await res.json();
      fClasse.innerHTML = '<option value="">Toutes les classes</option>' +
        classes.map(c => `<option value="${c.id}">${escapeHtml(c.nom)}</option>`).join('');
    } catch (e) {
      console.error('Erreur chargement classes', e);
    }
  }

  // ── Charger la liste des périodes pour le select "période de fin" ──
  async function chargerPeriodes() {
    try {
      const res = await fetch('/api/directeur/statistiques-eleves/periodes');
      const periodes = await res.json();
      // Période la plus récente en premier dans la liste déroulante
      const triees = [...periodes].sort((a, b) => b.ordre - a.ordre);
      fPeriodeFin.innerHTML = '<option value="">Dernière période disponible</option>' +
        triees.map(p => `<option value="${p.id}">${escapeHtml(p.libelle)}</option>`).join('');
    } catch (e) {
      console.error('Erreur chargement périodes', e);
    }
  }

  // ── Charger l'analyse principale ──────────────────────────────────
  async function charger() {
    const params = new URLSearchParams();
    if (fClasse.value)       params.set('classeId', fClasse.value);
    if (fPeriodeFin.value)   params.set('periodeFinId', fPeriodeFin.value);
    if (fSeuilBaisse.value)  params.set('seuilBaisseMoyenne', fSeuilBaisse.value);
    if (fSeuilAbsence.value) params.set('seuilTauxAbsence', fSeuilAbsence.value);

    setLoadingState();

    try {
      const res = await fetch('/api/directeur/statistiques-eleves?' + params.toString());
      const data = await res.json();
      render(data);
    } catch (e) {
      console.error('Erreur chargement statistiques élèves', e);
      resultContext.textContent = 'Erreur de chargement.';
    }
  }

  function setLoadingState() {
    resultContext.textContent = 'Chargement…';
    tbodyRouge.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:1.5rem;color:var(--txt3);">
      <i class="fas fa-spinner fa-spin"></i> Chargement…</td></tr>`;
    tbodyJaune.innerHTML = tbodyRouge.innerHTML;
  }

  // ── Rendu complet ──────────────────────────────────────────────────
  function render(data) {
    // Contexte
    const periodes = (data.periodesAnalysees || []).join(' → ');
    resultContext.textContent = data.anneeLibelle
      ? `Année ${data.anneeLibelle} · Périodes analysées : ${periodes || '—'}`
      : 'Aucune donnée disponible pour ces critères.';

    // Compteurs
    statEffectif.textContent    = data.effectifTotal ?? 0;
    statRouge.textContent       = data.nbAlerteRouge ?? 0;
    statJaune.textContent       = data.nbAlerteJaune ?? 0;
    statCorrelation.textContent = data.correlationGlobale != null
      ? data.correlationGlobale.toFixed(2)
      : '—';

    renderListe(tbodyRouge, data.listeRouge, 6);
    renderListe(tbodyJaune, data.listeJaune, 6);
    renderClasses(data.classes || []);
  }

  function renderListe(tbody, liste, colspan) {
    if (!liste || liste.length === 0) {
      tbody.innerHTML = `<tr><td colspan="${colspan}" style="text-align:center;padding:1.5rem;color:var(--txt3);">
        Aucun élève dans cette liste.</td></tr>`;
      return;
    }
    tbody.innerHTML = liste.map(e => {
      const moyennes = (e.moyennesParPeriode || [])
        .map(v => v != null ? Number(v).toFixed(1) : '—')
        .join(' / ');
      const delta = e.deltaMoyenne != null
        ? `<span style="color:${e.deltaMoyenne > 0 ? 'var(--danger)' : 'var(--success)'};font-weight:600;">
             ${e.deltaMoyenne > 0 ? '−' : '+'}${Math.abs(e.deltaMoyenne).toFixed(1)} pts</span>`
        : '—';
      const absence = e.tauxAbsence != null ? `${Number(e.tauxAbsence).toFixed(1)}%` : '—';
      const motifs = (e.motifsAlerte || []).map(m => `<div style="font-size:0.78rem;color:var(--txt2);">${escapeHtml(m)}</div>`).join('');

      return `<tr>
        <td>
          <strong>${escapeHtml(e.nom)} ${escapeHtml(e.prenom)}</strong>
          <div style="font-size:0.78rem;color:var(--txt3);">${escapeHtml(e.matricule || '')}</div>
        </td>
        <td>${escapeHtml(e.classeNom || '—')}</td>
        <td>${moyennes}</td>
        <td>${delta}</td>
        <td>${absence}</td>
        <td>${motifs}</td>
      </tr>`;
    }).join('');
  }

  // ── Nuages de points par classe (Chart.js scatter) ──────────────────
  function renderClasses(classes) {
    // Détruire les anciens graphiques
    Object.values(chartInstances).forEach(c => c.destroy());
    Object.keys(chartInstances).forEach(k => delete chartInstances[k]);
    chartsContainer.innerHTML = '';

    if (classes.length === 0) {
      chartsContainer.innerHTML = '<p style="color:var(--txt3);">Aucune classe à afficher.</p>';
      return;
    }

    classes.forEach(cls => {
      const blockId = `chart-classe-${cls.classeId}`;
      const corr = cls.correlationAbsenceMoyenne != null ? cls.correlationAbsenceMoyenne.toFixed(2) : '—';

      const block = document.createElement('div');
      block.className = 'card';
      block.innerHTML = `
        <div class="card-header" style="justify-content:space-between;">
          <h3 style="font-size:0.95rem;">${escapeHtml(cls.classeNom)} <span style="color:var(--txt3);font-weight:400;font-size:0.8rem;">(${cls.niveauLibelle || ''})</span></h3>
          <div style="display:flex;gap:0.4rem;align-items:center;font-size:0.78rem;">
            <span class="badge-red" style="padding:2px 8px;border-radius:999px;">${cls.nbAlerteRouge} rouge</span>
            <span class="badge-amber" style="padding:2px 8px;border-radius:999px;">${cls.nbAlerteJaune} jaune</span>
          </div>
        </div>
        <div class="card-body" style="padding:1rem;">
          <p style="font-size:0.78rem;color:var(--txt3);margin-bottom:0.5rem;">
            Effectif : ${cls.effectif} · Corrélation absence/moyenne : <strong>${corr}</strong>
          </p>
          <canvas id="${blockId}" height="220"></canvas>
        </div>`;
      chartsContainer.appendChild(block);

      const points = (cls.points || [])
        .filter(p => p.tauxAbsence != null && p.moyenneRecente != null)
        .map(p => ({
          x: Number(p.tauxAbsence),
          y: Number(p.moyenneRecente),
          nom: `${p.nom} ${p.prenom}`,
          niveau: p.niveauAlerte
        }));

      const colorFor = n => n === 'ROUGE' ? 'rgba(255,59,92,0.85)'
                            : n === 'JAUNE' ? 'rgba(255,184,0,0.85)'
                            : 'rgba(10,77,255,0.65)';

      const ctx = document.getElementById(blockId).getContext('2d');
      const chart = new Chart(ctx, {
        type: 'scatter',
        data: {
          datasets: [{
            label: 'Élèves',
            data: points,
            backgroundColor: points.map(p => colorFor(p.niveau)),
            pointRadius: 5,
            pointHoverRadius: 7
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (item) => {
                  const p = item.raw;
                  return `${p.nom} — Absence ${p.x.toFixed(1)}% · Moyenne ${p.y.toFixed(1)}/20`;
                }
              }
            }
          },
          scales: {
            x: { title: { display: true, text: "Taux d'absence (%)" }, beginAtZero: true },
            y: { title: { display: true, text: 'Moyenne récente (/20)' }, min: 0, max: 20 }
          }
        }
      });
      chartInstances[cls.classeId] = chart;
    });
  }

  // ── Util ─────────────────────────────────────────────────────────
  function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // ── Init ─────────────────────────────────────────────────────────
  chargerClasses();
  chargerPeriodes();
  charger();
});
