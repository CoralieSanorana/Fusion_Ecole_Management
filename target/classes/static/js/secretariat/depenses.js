document.addEventListener('DOMContentLoaded', () => {
  const prixInput = document.getElementById('depensePrixUnitaire');
  const quantiteInput = document.getElementById('depenseQuantite');
  const totalLive = document.getElementById('depenseTotalLive');
  const anneeSelect = document.getElementById('filtreAnnee');
  const periodeSelect = document.getElementById('filtrePeriode');
  const filtreForm = document.getElementById('filtreDepensesForm');

  const moneyFormatter = new Intl.NumberFormat('fr-FR');

  function updateTotal() {
    const prix = Number(prixInput?.value || 0);
    const quantite = Number(quantiteInput?.value || 0);
    const total = prix * quantite;
    if (totalLive) {
      totalLive.textContent = `${moneyFormatter.format(Math.round(total))} Ar`;
    }
  }

  prixInput?.addEventListener('input', updateTotal);
  quantiteInput?.addEventListener('input', updateTotal);
  updateTotal();

  function updatePeriodsState() {
    const hasAnnee = Boolean(anneeSelect && anneeSelect.value);
    if (periodeSelect) {
      periodeSelect.disabled = !hasAnnee;
    }
  }

  anneeSelect?.addEventListener('change', () => {
    updatePeriodsState();
    if (filtreForm) {
      filtreForm.submit();
    }
  });

  periodeSelect?.addEventListener('change', () => {
    if (filtreForm) {
      filtreForm.submit();
    }
  });

  updatePeriodsState();
});
