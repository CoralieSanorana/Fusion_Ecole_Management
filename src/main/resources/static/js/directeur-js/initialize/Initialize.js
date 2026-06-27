// Initialize.js
document.addEventListener('DOMContentLoaded', () => {
  let data = {
    etablissements: [],
    anneesScolaires: [],
    niveaux: [],
    salles: [],
    classes: [],
    matieres: []
  };

  // Step Navigation
  const stepBtns = document.querySelectorAll('.step-btn');
  const stepContents = document.querySelectorAll('.step-content');

  stepBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const step = btn.dataset.step;
      activateStep(step);
    });
  });

  function activateStep(step) {
    stepBtns.forEach(btn => {
      btn.classList.remove('active');
      if (btn.dataset.step === step) {
        btn.classList.add('active');
        btn.style.borderColor = 'var(--clr-primary)';
        btn.style.boxShadow = '0 4px 6px -1px rgba(0,0,0,0.1)';
      } else {
        btn.style.borderColor = 'var(--border)';
        btn.style.boxShadow = 'none';
      }
    });

    stepContents.forEach(content => {
      content.classList.remove('active');
      if (content.id === `step-${step}`) {
        content.classList.add('active');
      }
    });
  }

  // Fetch Initial Data
  async function fetchData() {
    try {
      const response = await fetch('/api/directeur/initialize/data');
      data = await response.json();
      renderLists();
      populateSelects();
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  }

  // Render Lists
  function renderLists() {
    renderList('etablissement', data.etablissements, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.nom}</strong>
          <small style="display:block;color:var(--txt2);">${item.adresse || ''}</small>
        </div>
        <button class="btn btn-sm btn-danger" onclick="deleteItem('etablissement', ${item.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `);

    renderList('annee', data.anneesScolaires, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.libelle}</strong>
          <small style="display:block;color:var(--txt2);">${item.dateDebut} - ${item.dateFin}</small>
        </div>
        <div style="display:flex;align-items:center;gap:1rem;">
          ${item.estActive ? '<span style="background:var(--clr-success);color:white;padding:0.25rem 0.75rem;border-radius:9999px;font-size:0.75rem;">Active</span>' : ''}
          <button class="btn btn-sm btn-danger" onclick="deleteItem('annee-scolaire', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('niveau', data.niveaux, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.libelle}</strong>
          <small style="display:block;color:var(--txt2);">Ordre: ${item.ordre}</small>
        </div>
        <button class="btn btn-sm btn-danger" onclick="deleteItem('niveau', ${item.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `);

    renderList('salle', data.salles, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.nom}</strong>
          <small style="display:block;color:var(--txt2);">Capacité: ${item.capacite} | Type: ${item.type}</small>
        </div>
        <button class="btn btn-sm btn-danger" onclick="deleteItem('salle', ${item.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `);

    renderList('classe', data.classes, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.nom}</strong>
          <small style="display:block;color:var(--txt2);">Capacité max: ${item.capaciteMax}</small>
        </div>
        <button class="btn btn-sm btn-danger" onclick="deleteItem('classe', ${item.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `);

    renderList('matiere', data.matieres, item => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:0.5rem;margin-bottom:0.5rem;">
        <div>
          <strong>${item.nom}</strong>
          <small style="display:block;color:var(--txt2);">${item.code || ''}</small>
        </div>
        <button class="btn btn-sm btn-danger" onclick="deleteItem('matiere', ${item.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `);
  }

  function renderList(type, items, templateFn) {
    const container = document.getElementById(`list-${type}`);
    if (!container) return;
    container.innerHTML = items.length ? items.map(templateFn).join('') : '<p style="color:var(--txt2);text-align:center;padding:2rem;">Aucun élément pour le moment</p>';
  }

  // Populate Selects
  function populateSelects() {
    const selectNiveau = document.getElementById('select-niveau');
    const selectAnnee = document.getElementById('select-annee');

    if (selectNiveau) {
      selectNiveau.innerHTML = data.niveaux.map(n => `<option value="${n.id}">${n.libelle}</option>`).join('');
    }

    if (selectAnnee) {
      selectAnnee.innerHTML = data.anneesScolaires.map(a => `<option value="${a.id}">${a.libelle}</option>`).join('');
    }
  }

  // Form Handlers
  setupForm('etablissement', '/api/directeur/initialize/etablissement');
  setupForm('annee', '/api/directeur/initialize/annee-scolaire');
  setupForm('niveau', '/api/directeur/initialize/niveau');
  setupForm('salle', '/api/directeur/initialize/salle');
  setupForm('classe', '/api/directeur/initialize/classe');
  setupForm('matiere', '/api/directeur/initialize/matiere');

  function setupForm(type, url) {
    const form = document.getElementById(`form-${type}`);
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const dataObj = {};
      formData.forEach((value, key) => {
        if (key === 'estActive' || key === 'isActive') {
          dataObj[key] = true;
        } else if (key === 'niveauId' || key === 'anneeScolaireId' || key === 'capacite' || key === 'capaciteMax' || key === 'ordre') {
          dataObj[key] = parseInt(value);
        } else {
          dataObj[key] = value;
        }
      });

      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(dataObj)
        });

        if (response.ok) {
          form.reset();
          await fetchData();
        }
      } catch (error) {
        console.error('Error saving item:', error);
      }
    });
  }

  // Delete Item
  window.deleteItem = async (type, id) => {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) return;
    try {
      const response = await fetch(`/api/directeur/initialize/${type}/${id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        await fetchData();
      }
    } catch (error) {
      console.error('Error deleting item:', error);
    }
  };

  // Initialize
  fetchData();
});
