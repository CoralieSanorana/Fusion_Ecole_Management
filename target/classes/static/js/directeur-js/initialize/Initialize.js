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
          dataObj[key] = formData.get(key) === 'on';
        } else if (key === 'niveauId' || key === 'anneeScolaireId' || key === 'capacite' || key === 'capaciteMax' || key === 'ordre') {
          dataObj[key] = parseInt(value);
        } else {
          dataObj[key] = value;
        }
      });

      // Get CSRF token
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

      const headers = { 'Content-Type': 'application/json' };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: headers,
          body: JSON.stringify(dataObj)
        });

        if (response.ok) {
          form.reset();
          await fetchData();
          showMessage('success', `${type.charAt(0).toUpperCase() + type.slice(1)} enregistré avec succès !`);
        } else {
          const errorText = await response.text();
          showMessage('error', `Erreur lors de l'enregistrement: ${errorText}`);
        }
      } catch (error) {
        console.error('Error saving item:', error);
        showMessage('error', `Erreur: ${error.message}`);
      }
    });
  }

  function showMessage(type, message) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.form-message');
    existingMessages.forEach(msg => msg.remove());

    const messageDiv = document.createElement('div');
    messageDiv.className = `form-message`;
    messageDiv.style.cssText = `
      padding: 1rem;
      margin-bottom: 1rem;
      border-radius: 0.5rem;
      ${type === 'success' ? 'background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7;' : 'background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5;'}
    `;
    messageDiv.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i> ${message}`;

    const activeStep = document.querySelector('.step-content.active');
    const cardBody = activeStep?.querySelector('.card-body');
    if (cardBody) {
      cardBody.insertBefore(messageDiv, cardBody.firstChild);
    }

    // Auto-remove after 5 seconds
    setTimeout(() => messageDiv.remove(), 5000);
  }

  // Delete Item
  window.deleteItem = async (type, id) => {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) return;
    
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = {};
    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }
    
    try {
      const response = await fetch(`/api/directeur/initialize/${type}/${id}`, {
        method: 'DELETE',
        headers: headers
      });
      if (response.ok) {
        await fetchData();
        showMessage('success', 'Élément supprimé avec succès !');
      } else {
        const errorText = await response.text();
        showMessage('error', `Erreur lors de la suppression: ${errorText}`);
      }
    } catch (error) {
      console.error('Error deleting item:', error);
      showMessage('error', `Erreur: ${error.message}`);
    }
  };

  // Export to CSV
  const btnExport = document.getElementById('btn-export');
  if (btnExport) {
    btnExport.addEventListener('click', async () => {
      try {
        showMessage('info', 'Génération du fichier CSV en cours...');
        
        const response = await fetch('/api/export-import/export/excel');
        
        if (response.ok) {
          const blob = await response.blob();
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = response.headers.get('Content-Disposition')?.split('filename=')[1]?.replace(/"/g, '') || 'export_ecole.csv';
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
          showMessage('success', 'Export réussi ! Le fichier CSV a été téléchargé.');
        } else {
          showMessage('error', 'Erreur lors de l\'export');
        }
      } catch (error) {
        console.error('Error exporting:', error);
        showMessage('error', `Erreur: ${error.message}`);
      }
    });
  }

  // Import from CSV
  const btnImport = document.getElementById('btn-import');
  if (btnImport) {
    btnImport.addEventListener('click', () => {
      // Create file input
      const fileInput = document.createElement('input');
      fileInput.type = 'file';
      fileInput.accept = '.csv';
      fileInput.style.display = 'none';
      
      fileInput.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        const formData = new FormData();
        formData.append('file', file);
        
        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        showMessage('info', 'Import CSV en cours... Veuillez patienter.');
        
        try {
          const response = await fetch('/api/export-import/import/excel', {
            method: 'POST',
            headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
            body: formData
          });
          
          const result = await response.json();
          
          if (result.success) {
            showMessage('success', result.message);
            await fetchData();
          } else {
            let errorMessage = result.message;
            if (result.errors && result.errors.length > 0) {
              errorMessage += '<br><br>Erreurs détaillées:<br>';
              result.errors.forEach((error, index) => {
                errorMessage += `${index + 1}. Ligne ${error.rowNumber} - ${error.tableName}: ${error.errorMessage}<br>`;
              });
            }
            if (result.warnings && result.warnings.length > 0) {
              errorMessage += '<br>Warnings:<br>';
              result.warnings.forEach((warning, index) => {
                errorMessage += `${index + 1}. ${warning}<br>`;
              });
            }
            showMessage('error', errorMessage);
          }
        } catch (error) {
          console.error('Error importing:', error);
          showMessage('error', `Erreur: ${error.message}`);
        }
        
        document.body.removeChild(fileInput);
      });
      
      document.body.appendChild(fileInput);
      fileInput.click();
    });
  }

  // Initialize
  fetchData();
});
