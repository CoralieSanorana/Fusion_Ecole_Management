document.addEventListener('DOMContentLoaded', () => {
  const endpoint = '/api/directeur/dashboard';

  const yearSelect = document.getElementById('dashboard-year-select');
  const monthSelect = document.getElementById('dashboard-month-select');
  const refreshBtn = document.getElementById('dashboard-refresh-btn');
  const exportTransactionsBtn = document.getElementById('export-transactions-pdf-btn');
  const exportBilanBtn = document.getElementById('export-bilan-pdf-btn');
  const chartExportBtn = document.getElementById('chart-export-pdf-btn');
  const greeting = document.getElementById('dashboard-greeting');
  const context = document.getElementById('dashboard-context');
  const chartContainer = document.getElementById('dashboard-chart');
  const chartEmpty = document.getElementById('dashboard-chart-empty');
  const transactionsContainer = document.getElementById('dashboard-transactions');
  const transactionsEmpty = document.getElementById('dashboard-transactions-empty');
  const paymentPaidBar = document.getElementById('payment-paid-bar');
  const paymentUnpaidBar = document.getElementById('payment-unpaid-bar');
  const paymentPaidCount = document.getElementById('payment-paid-count');
  const paymentUnpaidCount = document.getElementById('payment-unpaid-count');
  const paymentRateTitle = document.getElementById('payment-rate-title');
  const statRecettes = document.getElementById('stat-recettes');
  const statDepenses = document.getElementById('stat-depenses');
  const statDepensesTrend = document.getElementById('stat-depenses-trend');
  const statElevesPayes = document.getElementById('stat-eleves-payes');
  const statElevesPayesRate = document.getElementById('stat-eleves-payes-rate');
  const statProfesseursActifs = document.getElementById('stat-professeurs-actifs');
  const statTransactionsTotal = document.getElementById('stat-transactions-total');
  const statBeneficeNet = document.getElementById('stat-benefice-net');
  const statResume = document.getElementById('stat-resume');
  const chartTitle = document.getElementById('chart-title');

  const moneyFormatter = new Intl.NumberFormat('fr-FR');
  let lastPayload = null;

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function buildQuery() {
    const params = new URLSearchParams();
    if (yearSelect.value) {
      params.set('anneeScolaireId', yearSelect.value);
    }
    if (monthSelect.value) {
      params.set('mois', monthSelect.value);
    }
    return params.toString();
  }

  function setLoading() {
    context.textContent = 'Chargement des données financières…';
    chartContainer.innerHTML = '';
    chartEmpty.style.display = 'none';
    transactionsContainer.innerHTML = '';
    transactionsEmpty.style.display = 'none';
  }

  function formatMoney(value) {
    return moneyFormatter.format(Number(value || 0));
  }

  function fillSelect(select, items, selectedValue) {
    select.innerHTML = items.map(item => {
      const selected = item.value === selectedValue || String(item.id) === String(selectedValue);
      return `<option value="${escapeHtml(item.value ?? item.id)}" ${selected ? 'selected' : ''}>${escapeHtml(item.label ?? item.libelle)}</option>`;
    }).join('');
  }

  function renderChart(series) {
    chartContainer.innerHTML = '';
    const maxValue = Math.max(1, ...series.flatMap(point => [Number(point.recettes || 0), Number(point.depensesSalairesProfesseurs || 0)]));
    if (!series.length) {
      chartEmpty.style.display = 'block';
      return;
    }

    chartEmpty.style.display = 'none';
    chartContainer.style.display = 'flex';
    chartContainer.style.alignItems = 'flex-end';
    chartContainer.style.gap = '16px';
    chartContainer.style.minHeight = '240px';
    series.forEach(point => {
      const recettesHeight = Math.max(10, Math.round((Number(point.recettes || 0) / maxValue) * 160));
      const depensesHeight = Math.max(10, Math.round((Number(point.depensesSalairesProfesseurs || 0) / maxValue) * 160));
      const card = document.createElement('div');
      card.className = 'chart-bar-wrapper';
      card.style.flex = '1';
      card.style.display = 'flex';
      card.style.flexDirection = 'column';
      card.style.alignItems = 'center';
      card.style.justifyContent = 'flex-end';
      card.style.minWidth = '72px';

      const bars = document.createElement('div');
      bars.style.display = 'flex';
      bars.style.alignItems = 'flex-end';
      bars.style.justifyContent = 'center';
      bars.style.gap = '8px';
      bars.style.height = '180px';
      bars.style.width = '100%';

      const recetteBar = document.createElement('div');
      recetteBar.className = 'chart-bar';
      recetteBar.style.height = `${recettesHeight}px`;
      recetteBar.style.width = '18px';
      recetteBar.style.minHeight = '18px';
      recetteBar.style.background = 'linear-gradient(180deg, #fbbf24, #f59e0b)';
      recetteBar.title = `${point.label} - Recettes ${formatMoney(point.recettes)} Ar`;

      const depenseBar = document.createElement('div');
      depenseBar.className = 'chart-bar alt';
      depenseBar.style.height = `${depensesHeight}px`;
      depenseBar.style.width = '18px';
      depenseBar.style.minHeight = '18px';
      depenseBar.style.background = 'linear-gradient(180deg, #5eead4, #14b8a6)';
      depenseBar.title = `${point.label} - Dépenses ${formatMoney(point.depensesSalairesProfesseurs)} Ar`;

      if (point.selected) {
        recetteBar.style.boxShadow = '0 0 0 3px rgba(245, 158, 11, .16)';
        depenseBar.style.boxShadow = '0 0 0 3px rgba(20, 184, 166, .16)';
      }

      bars.appendChild(recetteBar);
      bars.appendChild(depenseBar);

      const label = document.createElement('span');
      label.style.marginTop = '10px';
      label.style.fontSize = '11px';
      label.style.color = point.selected ? 'var(--txt1)' : 'var(--txt3)';
      label.textContent = point.label;

      const amount = document.createElement('small');
      amount.style.marginTop = '4px';
      amount.style.fontSize = '10px';
      amount.style.color = 'var(--txt3)';
      amount.textContent = `${formatMoney(point.recettes)} / ${formatMoney(point.depensesSalairesProfesseurs)} Ar`;

      card.appendChild(bars);
      card.appendChild(label);
      card.appendChild(amount);
      chartContainer.appendChild(card);
    });
  }

  function renderTransactions(transactions) {
    transactionsContainer.innerHTML = '';
    if (!transactions.length) {
      transactionsEmpty.style.display = 'block';
      return;
    }

    transactionsEmpty.style.display = 'none';
    transactions.forEach(transaction => {
      const item = document.createElement('div');
      item.className = 'timeline-item';
      const isExpense = transaction.transactionType === 'DEPENSE';
      const iconClass = isExpense ? 'fa-receipt' : 'fa-check-circle';
      const dotColor = isExpense ? 'rgba(20, 184, 166, .08)' : 'rgba(37,99,235,.08)';
      const dotText = isExpense ? 'var(--clr-teal)' : 'var(--b)';
      const detailText = transaction.libelleSecondaire || transaction.echeanceLabel || '';
      item.innerHTML = `
        <div class="timeline-dot" style="background:${dotColor};color:${dotText};"><i class="fas ${iconClass}"></i></div>
        <div class="timeline-content" style="display:flex;justify-content:space-between;gap:12px;align-items:flex-start;width:100%;">
          <div>
            <strong>${escapeHtml(transaction.libellePrincipal || transaction.etudiantNom || 'Transaction')}</strong>
            <small>${escapeHtml(detailText || (isExpense ? 'Dépense' : 'Paiement'))} · ${escapeHtml(transaction.dateLibelle || '')}</small>
            <div style="margin-top:4px;font-size:12px;color:var(--txt3);">${escapeHtml(transaction.referenceTransaction || 'Sans référence')} ${isExpense ? '' : '· ' + escapeHtml(transaction.matricule || '')}</div>
          </div>
          <div style="text-align:right;white-space:nowrap;">
            <strong style="font-size:14px;">${formatMoney(transaction.montant)} Ar</strong>
            <div style="font-size:12px;color:var(--txt3);">${escapeHtml(isExpense ? (transaction.modePaiement || 'Dépense') : (transaction.modePaiement || 'Mode inconnu'))}</div>
          </div>
        </div>`;
      transactionsContainer.appendChild(item);
    });
  }

  function renderDonut(kpis) {
    const total = Number(kpis.elevesTotal || 0);
    const paid = Number(kpis.elevesPayes || 0);
    const unpaid = Math.max(0, total - paid);
    const paidRatio = total > 0 ? Math.round((paid / total) * 100) : 0;
    const unpaidRatio = total > 0 ? Math.round((unpaid / total) * 100) : 0;
    const donut = document.getElementById('payment-donut');
    donut.style.background = `conic-gradient(var(--success) 0 ${paidRatio}%, var(--danger) ${paidRatio}% 100%)`;
    paymentPaidBar.style.width = `${paidRatio}%`;
    paymentUnpaidBar.style.width = `${unpaidRatio}%`;
    paymentPaidCount.textContent = String(paid);
    paymentUnpaidCount.textContent = String(unpaid);
    paymentRateTitle.textContent = `Taux de paiement ${lastPayload?.selectedMonthLabel || ''}`.trim();
  }

  function render(payload) {
    lastPayload = payload;
    const anneeOptions = (payload.annees || []).map(year => ({ value: year.id, label: year.libelle }));
    fillSelect(yearSelect, anneeOptions, payload.anneeScolaireId);
    fillSelect(monthSelect, payload.mois || [], payload.selectedMonth);

    greeting.textContent = 'Bonjour, Directeur';
    context.textContent = payload.resume || `${payload.anneeScolaireLibelle || ''} · ${payload.selectedMonthLabel || ''}`.trim();

    const kpis = payload.kpis || {};
    statRecettes.textContent = formatMoney(kpis.recettesTotal);
    statDepenses.textContent = formatMoney(kpis.depensesSalairesProfesseurs);
    statDepensesTrend.innerHTML = `<i class="fas fa-arrow-down"></i> ${kpis.transactionsTotal || 0} transaction(s)`;
    statElevesPayes.textContent = String(kpis.elevesPayes || 0);
    statElevesPayesRate.innerHTML = `<i class="fas fa-arrow-up"></i> ${kpis.elevesTotal || 0} élèves inscrits`;
    statProfesseursActifs.textContent = String(kpis.professeursActifs || 0);
    statTransactionsTotal.innerHTML = `<i class="fas fa-check"></i> ${kpis.transactionsTotal || 0} transaction(s)`;
    statBeneficeNet.textContent = formatMoney(kpis.beneficeNet);
    statResume.innerHTML = `<i class="fas fa-arrow-up"></i> ${payload.selectedMonthLabel || ''}`;

    chartTitle.textContent = `Recettes vs dépenses - ${payload.anneeScolaireLibelle || ''}`.trim();
    renderChart(payload.serieMensuelle || []);
    renderTransactions(payload.transactions || []);
    renderDonut(kpis);
  }

  function buildExportUrl(kind) {
    const query = buildQuery();
    return `/api/directeur/dashboard/export/${kind}.pdf${query ? `?${query}` : ''}`;
  }

  async function loadDashboard() {
    setLoading();
    const query = buildQuery();
    const response = await fetch(query ? `${endpoint}?${query}` : endpoint);
    if (!response.ok) {
      throw new Error(`Dashboard request failed: ${response.status}`);
    }
    const payload = await response.json();
    render(payload);
  }

  yearSelect.addEventListener('change', () => loadDashboard().catch(console.error));
  monthSelect.addEventListener('change', () => loadDashboard().catch(console.error));
  refreshBtn.addEventListener('click', () => loadDashboard().catch(console.error));
  exportTransactionsBtn.addEventListener('click', () => window.open(buildExportUrl('transactions'), '_blank', 'noopener'));
  exportBilanBtn.addEventListener('click', () => window.open(buildExportUrl('bilan'), '_blank', 'noopener'));
  chartExportBtn.addEventListener('click', () => window.open(buildExportUrl('bilan'), '_blank', 'noopener'));

  loadDashboard().catch(error => {
    console.error(error);
    context.textContent = 'Impossible de charger le tableau de bord.';
    chartEmpty.style.display = 'block';
    chartEmpty.textContent = 'Chargement impossible.';
    transactionsEmpty.style.display = 'block';
    transactionsEmpty.textContent = 'Chargement impossible.';
  });
});