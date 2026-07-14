// Initialize.js
document.addEventListener('DOMContentLoaded', () => {
  let data = {
    etablissements: [],
    anneesScolaires: [],
    niveaux: [],
    salles: [],
    classes: [],
    matieres: [],
    professeurs: [],
    coefficients: [],
    affectations: []
  };

  // NOTE : la navigation entre étapes (carrousel, onglets actifs/visités) est
  // gérée exclusivement par le script inline en bas de initialize.html, qui
  // expose window.goToStep(stepName). Ce fichier ne fait plus doublon ici —
  // les bannières de prérequis appellent goToStep(...) pour rediriger l'utilisateur.

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
      <div class="list-item">
        <div class="li-main">
          <strong>${item.nom}</strong>
          <small>${item.adresse || ''}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('etablissement', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('annee', data.anneesScolaires, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.libelle}</strong>
          <small>${item.dateDebut} - ${item.dateFin}</small>
        </div>
        <div class="li-actions">
          ${item.estActive ? '<span class="badge-active">Active</span>' : ''}
          <button class="btn btn-sm btn-danger" onclick="deleteItem('annee-scolaire', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('niveau', data.niveaux, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.libelle}</strong>
          <small>Ordre: ${item.ordre}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('niveau', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('salle', data.salles, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.nom}</strong>
          <small>Capacité : ${item.capacite} &middot; Type : ${item.type}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('salle', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('classe', data.classes, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.nom}</strong>
          <small>Capacité max : ${item.capaciteMax}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('classe', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('matiere', data.matieres, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.nom}</strong>
          <small>${item.code || ''}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('matiere', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    renderList('coefficient', data.coefficients, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.matiereNom || 'Matière inconnue'} - ${item.niveauNom || 'Niveau inconnu'}</strong>
          <small>Coefficient : ${item.valeur}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-warning" onclick="editCoefficient(${item.id})">
            <i class="fas fa-edit"></i>
          </button>
          <button class="btn btn-sm btn-danger" onclick="deleteItem('coefficient', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);

    // Initial filter for coefficients
    filterCoefficients();

    // Initial filter for affectations
    filterAffectations();

    // Check prerequisites and show/hide forms accordingly
    checkPrerequisites();
  }

  // Check prerequisites and show/hide forms
  // Construit une bannière "prérequis manquant(s)" cohérente et bien espacée
  function missingPrereqBanner(title, message, links, extraNote) {
    const linksHtml = (links || [])
      .map(l => `<a href="#" onclick="goToStep('${l.step}'); return false;">Aller à ${l.label}</a>`)
      .join('');
    return `
      <div class="prereq-banner prereq-banner--missing">
        <i class="fas fa-exclamation-triangle prereq-icon"></i>
        <div>
          <span class="prereq-title">${title}</span>
          <p>${message}</p>
          ${extraNote ? `<p style="margin-top:0.5rem;">${extraNote}</p>` : ''}
          ${linksHtml ? `<div class="prereq-links">${linksHtml}</div>` : ''}
        </div>
      </div>
    `;
  }

  function checkPrerequisites() {
    const hasEtablissement = data.etablissements && data.etablissements.length > 0;
    const hasAnneeActive = data.anneesScolaires && data.anneesScolaires.some(a => a.estActive);
    const hasNiveaux = data.niveaux && data.niveaux.length > 0;
    const hasSalles = data.salles && data.salles.length > 0;
    const hasMatieres = data.matieres && data.matieres.length > 0;
    const hasClasses = data.classes && data.classes.length > 0;
    const hasProfesseurs = data.professeurs && data.professeurs.length > 0;

    // Annee Scolaire - requires Etablissement
    const formAnnee = document.getElementById('form-annee');
    const prereqAnnee = document.getElementById('prereq-annee');
    if (formAnnee && prereqAnnee) {
      if (!hasEtablissement) {
        formAnnee.style.display = 'none';
        prereqAnnee.style.display = 'block';
        prereqAnnee.innerHTML = missingPrereqBanner(
          'Prérequis manquant',
          "Vous devez d'abord créer un établissement.",
          [{ step: 'etablissement', label: 'Établissement' }]
        );
      } else {
        formAnnee.style.display = 'grid';
        prereqAnnee.style.display = 'none';
      }
    }

    // Niveaux - requires Etablissement
    const formNiveau = document.getElementById('form-niveau');
    const prereqNiveau = document.getElementById('prereq-niveau');
    if (formNiveau && prereqNiveau) {
      if (!hasEtablissement) {
        formNiveau.style.display = 'none';
        prereqNiveau.style.display = 'block';
        prereqNiveau.innerHTML = missingPrereqBanner(
          'Prérequis manquant',
          "Vous devez d'abord créer un établissement.",
          [{ step: 'etablissement', label: 'Établissement' }]
        );
      } else {
        formNiveau.style.display = 'grid';
        prereqNiveau.style.display = 'none';
      }
    }

    // Salles - requires Etablissement
    const formSalle = document.getElementById('form-salle');
    const prereqSalle = document.getElementById('prereq-salle');
    if (formSalle && prereqSalle) {
      if (!hasEtablissement) {
        formSalle.style.display = 'none';
        prereqSalle.style.display = 'block';
        prereqSalle.innerHTML = missingPrereqBanner(
          'Prérequis manquant',
          "Vous devez d'abord créer un établissement.",
          [{ step: 'etablissement', label: 'Établissement' }]
        );
      } else {
        formSalle.style.display = 'grid';
        prereqSalle.style.display = 'none';
      }
    }

    // Matières - requires Etablissement
    // (correctif : le formulaire était affiché EN MEME TEMPS que la bannière
    // d'erreur quand l'établissement manquait, d'où le chevauchement visuel)
    const formMatiere = document.getElementById('form-matiere');
    const prereqMatiere = document.getElementById('prereq-matiere');
    if (formMatiere && prereqMatiere) {
      if (!hasEtablissement) {
        formMatiere.style.display = 'none';
        prereqMatiere.style.display = 'block';
        prereqMatiere.innerHTML = missingPrereqBanner(
          'Prérequis manquant',
          "Vous devez d'abord créer un établissement.",
          [{ step: 'etablissement', label: 'Établissement' }]
        );
      } else {
        formMatiere.style.display = 'grid';
        prereqMatiere.style.display = 'none';
      }
    }

    // Classes - requires Annee Active, Niveaux, Salles
    const formClasse = document.getElementById('form-classe');
    const prereqClasse = document.getElementById('prereq-classe');
    if (formClasse && prereqClasse) {
      const missingPrereqs = [];
      const links = [];
      if (!hasAnneeActive) { missingPrereqs.push('une année scolaire active'); links.push({ step: 'annee', label: 'Année Scolaire' }); }
      if (!hasNiveaux) { missingPrereqs.push('des niveaux'); links.push({ step: 'niveau', label: 'Niveaux' }); }
      if (!hasSalles) { missingPrereqs.push('des salles'); links.push({ step: 'salle', label: 'Salles' }); }

      if (missingPrereqs.length > 0) {
        formClasse.style.display = 'none';
        prereqClasse.style.display = 'block';
        prereqClasse.innerHTML = missingPrereqBanner(
          'Prérequis manquants',
          `Vous devez d'abord créer ${missingPrereqs.join(', ')}.`,
          links
        );
      } else {
        formClasse.style.display = 'grid';
        prereqClasse.style.display = 'none';
      }
    }

    // Coefficients - requires Matières, Niveaux
    const formCoefficient = document.getElementById('form-coefficient');
    const prereqCoefficient = document.getElementById('prereq-coefficient');
    if (formCoefficient && prereqCoefficient) {
      const missingPrereqs = [];
      const links = [];
      if (!hasMatieres) { missingPrereqs.push('des matières'); links.push({ step: 'matiere', label: 'Matières' }); }
      if (!hasNiveaux) { missingPrereqs.push('des niveaux'); links.push({ step: 'niveau', label: 'Niveaux' }); }

      if (missingPrereqs.length > 0) {
        formCoefficient.style.display = 'none';
        prereqCoefficient.style.display = 'block';
        prereqCoefficient.innerHTML = missingPrereqBanner(
          'Prérequis manquants',
          `Vous devez d'abord créer ${missingPrereqs.join(', ')}.`,
          links
        );
      } else {
        formCoefficient.style.display = 'grid';
        prereqCoefficient.style.display = 'none';
      }
    }

    // Affectations - requires Professeurs, Matières, Classes, Annee Scolaire
    const formAffectation = document.getElementById('form-affectation');
    const prereqAffectation = document.getElementById('prereq-affectation');
    if (formAffectation && prereqAffectation) {
      const missingPrereqs = [];
      const links = [];
      if (!hasProfesseurs) missingPrereqs.push('des professeurs');
      if (!hasMatieres) { missingPrereqs.push('des matières'); links.push({ step: 'matiere', label: 'Matières' }); }
      if (!hasClasses) { missingPrereqs.push('des classes'); links.push({ step: 'classe', label: 'Classes' }); }
      if (!hasAnneeActive) { missingPrereqs.push('une année scolaire active'); links.push({ step: 'annee', label: 'Année Scolaire' }); }

      if (missingPrereqs.length > 0) {
        formAffectation.style.display = 'none';
        prereqAffectation.style.display = 'block';
        prereqAffectation.innerHTML = missingPrereqBanner(
          'Prérequis manquants',
          `Vous devez d'abord créer ${missingPrereqs.join(', ')}.`,
          links,
          !hasProfesseurs ? "Veuillez contacter l'administration pour créer des professeurs." : null
        );
      } else {
        formAffectation.style.display = 'grid';
        prereqAffectation.style.display = 'none';
      }
    }
  }

  function renderList(type, items, templateFn) {
    const container = document.getElementById(`list-${type}`);
    if (!container) return;
    container.innerHTML = items.length
      ? `<div class="list-items">${items.map(templateFn).join('')}</div>`
      : '<p class="list-empty">Aucun élément pour le moment</p>';
  }

  // Filter coefficients by matière and niveau
  function filterCoefficients() {
    const selectMatiereCoeff = document.getElementById('select-matiere-coeff');
    const selectNiveauCoeff = document.getElementById('select-niveau-coeff');
    
    const selectedMatiereId = selectMatiereCoeff ? selectMatiereCoeff.value : '';
    const selectedNiveauId = selectNiveauCoeff ? selectNiveauCoeff.value : '';
    
    let filteredCoefficients = data.coefficients;
    
    // Filter by matière if selected
    if (selectedMatiereId) {
      filteredCoefficients = filteredCoefficients.filter(c => c.matiereId == selectedMatiereId);
    }
    
    // Filter by niveau if selected
    if (selectedNiveauId) {
      filteredCoefficients = filteredCoefficients.filter(c => c.niveauId == selectedNiveauId);
    }
    
    renderList('coefficient', filteredCoefficients, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.matiereNom || 'Matière inconnue'} - ${item.niveauNom || 'Niveau inconnu'}</strong>
          <small>Coefficient : ${item.valeur}</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-warning" onclick="editCoefficient(${item.id})">
            <i class="fas fa-edit"></i>
          </button>
          <button class="btn btn-sm btn-danger" onclick="deleteItem('coefficient', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);
  }

  // Filter affectations by classe et année
  function filterAffectations() {
    const selectClasse = document.getElementById('select-classe');
    const selectAnneeAffect = document.getElementById('select-annee-affect');
    
    const selectedClasseId = selectClasse ? selectClasse.value : '';
    const selectedAnneeId = selectAnneeAffect ? selectAnneeAffect.value : '';
    
    let filteredAffectations = data.affectations;
    
    // Filter by classe if selected
    if (selectedClasseId) {
      filteredAffectations = filteredAffectations.filter(a => a.classeId == selectedClasseId);
    }
    
    // Filter by année if selected
    if (selectedAnneeId) {
      filteredAffectations = filteredAffectations.filter(a => a.anneeScolaireId == selectedAnneeId);
    }
    
    renderList('affectation', filteredAffectations, item => `
      <div class="list-item">
        <div class="li-main">
          <strong>${item.professeurNom || 'Professeur inconnu'} - ${item.matiereNom || 'Matière inconnue'}</strong>
          <small>${item.classeNom || 'Classe inconnue'} &middot; ${item.anneeScolaireNom || 'Année inconnue'} &middot; ${item.heuresHebdo}h/semaine</small>
        </div>
        <div class="li-actions">
          <button class="btn btn-sm btn-danger" onclick="deleteItem('affectation', ${item.id})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>
    `);
  }

  // Edit coefficient
  window.editCoefficient = async (id) => {
    const coefficient = data.coefficients.find(c => c.id === id);
    if (!coefficient) return;

    const selectMatiereCoeff = document.getElementById('select-matiere-coeff');
    const selectNiveauCoeff = document.getElementById('select-niveau-coeff');
    const inputValeur = document.querySelector('#form-coefficient input[name="valeur"]');

    if (selectMatiereCoeff) selectMatiereCoeff.value = coefficient.matiereId;
    if (selectNiveauCoeff) selectNiveauCoeff.value = coefficient.niveauId;
    if (inputValeur) inputValeur.value = coefficient.valeur;

    // Change form to update mode
    const form = document.getElementById('form-coefficient');
    form.dataset.editId = id;
    
    showMessage('info', 'Modification du coefficient en cours. Modifiez les valeurs et cliquez sur Enregistrer.');
  };

  // Populate Selects
  function populateSelects() {
    const selectNiveau = document.getElementById('select-niveau');
    const selectAnnee = document.getElementById('select-annee');
    const selectMatiereCoeff = document.getElementById('select-matiere-coeff');
    const selectNiveauCoeff = document.getElementById('select-niveau-coeff');
    const selectProfesseur = document.getElementById('select-professeur');
    const selectMatiereAffect = document.getElementById('select-matiere-affect');
    const selectClasse = document.getElementById('select-classe');
    const selectAnneeAffect = document.getElementById('select-annee-affect');
    const selectSalleClasse = document.getElementById('select-salle-classe');
    const selectEtablissementAnnee = document.getElementById('select-etablissement-annee');
    const selectEtablissementNiveau = document.getElementById('select-etablissement-niveau');
    const selectEtablissementSalle = document.getElementById('select-etablissement-salle');
    const selectEtablissementMatiere = document.getElementById('select-etablissement-matiere');

    // Populate établissement selects
    if (selectEtablissementAnnee) {
      selectEtablissementAnnee.innerHTML = data.etablissements.map(e => `<option value="${e.id}">${e.nom}</option>`).join('');
      makeSearchable(selectEtablissementAnnee);
    }

    if (selectEtablissementNiveau) {
      selectEtablissementNiveau.innerHTML = data.etablissements.map(e => `<option value="${e.id}">${e.nom}</option>`).join('');
      makeSearchable(selectEtablissementNiveau);
    }

    if (selectEtablissementSalle) {
      selectEtablissementSalle.innerHTML = data.etablissements.map(e => `<option value="${e.id}">${e.nom}</option>`).join('');
      makeSearchable(selectEtablissementSalle);
    }

    if (selectEtablissementMatiere) {
      selectEtablissementMatiere.innerHTML = data.etablissements.map(e => `<option value="${e.id}">${e.nom}</option>`).join('');
      makeSearchable(selectEtablissementMatiere);
    }

    if (selectNiveau) {
      selectNiveau.innerHTML = data.niveaux.map(n => `<option value="${n.id}">${n.libelle}</option>`).join('');
      makeSearchable(selectNiveau);
    }

    if (selectAnnee) {
      selectAnnee.innerHTML = data.anneesScolaires.map(a => `<option value="${a.id}">${a.libelle}</option>`).join('');
      makeSearchable(selectAnnee);
    }

    if (selectSalleClasse) {
      // Get salle IDs that are already used by existing classes
      const usedSalleIds = data.classes.map(c => c.salleId);
      // Filter out salles that are already used
      const availableSalles = data.salles.filter(s => !usedSalleIds.includes(s.id));

      const formClasse = document.getElementById('form-classe');
      const prereqClasse = document.getElementById('prereq-classe');
      // Nettoyage d'un éventuel ancien message (versions précédentes de la page)
      const legacyMsg = document.getElementById('no-salle-message');
      if (legacyMsg) legacyMsg.remove();

      if (data.salles.length > 0 && availableSalles.length === 0) {
        // Des salles existent mais sont toutes déjà associées à une classe :
        // on masque complètement le formulaire (plus de champs à moitié inutilisables),
        // on affiche uniquement le message, la liste des classes déjà créées reste visible en dessous.
        selectSalleClasse.innerHTML = '<option value="">Aucune salle disponible</option>';
        selectSalleClasse.disabled = true;
        if (formClasse) formClasse.style.display = 'none';
        if (prereqClasse) {
          prereqClasse.style.display = 'block';
          prereqClasse.innerHTML = missingPrereqBanner(
            'Aucune salle disponible',
            'Toutes les salles ont déjà une classe associée. Veuillez créer de nouvelles salles avant de créer des classes.',
            [{ step: 'salle', label: 'Salles' }]
          );
        }
      } else {
        selectSalleClasse.innerHTML = availableSalles.map(s => `<option value="${s.id}" data-capacite="${s.capacite}">${s.nom} (Capacité: ${s.capacite})</option>`).join('');
        selectSalleClasse.disabled = false;
        // NOTE : on ne force pas le réaffichage de formClasse/prereqClasse ici.
        // checkPrerequisites() a déjà déterminé le bon état (niveaux/année active/
        // salles manquants ou non) avant que ce bloc ne s'exécute ; on ne le
        // réécrit que si le problème était spécifiquement "plus de salle libre".
      }
      makeSearchable(selectSalleClasse);
      
      // Add event listener to auto-fill capacity when salle is selected
      selectSalleClasse.addEventListener('change', function() {
        const selectedOption = this.options[this.selectedIndex];
        const capacite = selectedOption.getAttribute('data-capacite');
        const capaciteInput = document.querySelector('#form-classe input[name="capaciteMax"]');
        if (capacite && capaciteInput) {
          capaciteInput.value = capacite;
        }
      });
    }

    if (selectMatiereCoeff) {
      selectMatiereCoeff.innerHTML = '<option value="">Toutes les matières</option>' + data.matieres.map(m => `<option value="${m.id}">${m.nom}</option>`).join('');
      // Add event listener to filter coefficients when matière changes
      selectMatiereCoeff.addEventListener('change', () => {
        filterCoefficients();
      });
    }

    if (selectNiveauCoeff) {
      selectNiveauCoeff.innerHTML = '<option value="">Tous les niveaux</option>' + data.niveaux.map(n => `<option value="${n.id}">${n.libelle}</option>`).join('');
      // Add event listener to filter coefficients when niveau changes
      selectNiveauCoeff.addEventListener('change', () => {
        filterCoefficients();
      });
    }

    if (selectProfesseur) {
      selectProfesseur.innerHTML = data.professeurs.map(p => `<option value="${p.id}">${p.nom} ${p.prenom}</option>`).join('');
    }

    if (selectMatiereAffect) {
      selectMatiereAffect.innerHTML = data.matieres.map(m => `<option value="${m.id}">${m.nom}</option>`).join('');
    }

    if (selectClasse) {
      selectClasse.innerHTML = '<option value="">Toutes les classes</option>' + data.classes.map(c => `<option value="${c.id}">${c.nom}</option>`).join('');
      // Add event listener to filter affectations when classe changes
      selectClasse.addEventListener('change', () => {
        filterAffectations();
      });
    }

    if (selectAnneeAffect) {
      selectAnneeAffect.innerHTML = '<option value="">Toutes les années</option>' + data.anneesScolaires.map(a => `<option value="${a.id}">${a.libelle}</option>`).join('');
      // Add event listener to filter affectations when année changes
      selectAnneeAffect.addEventListener('change', () => {
        filterAffectations();
      });
    }
  }

  // Make a select element searchable
  function makeSearchable(selectElement) {
    if (!selectElement || selectElement.classList.contains('searchable-initialized')) return;
    
    selectElement.classList.add('searchable-initialized');
    
    const wrapper = document.createElement('div');
    wrapper.style.position = 'relative';
    wrapper.style.display = 'inline-block';
    wrapper.style.width = '100%';
    
    const searchInput = document.createElement('input');
    searchInput.type = 'text';
    searchInput.placeholder = 'Rechercher...';
    searchInput.style.cssText = `
      width: 100%;
      padding: 0.5rem;
      margin-bottom: 0.5rem;
      border: 1px solid var(--border);
      border-radius: 0.25rem;
      font-size: 0.9rem;
    `;
    
    selectElement.parentNode.insertBefore(wrapper, selectElement);
    wrapper.appendChild(searchInput);
    wrapper.appendChild(selectElement);
    
    // Store original options
    const originalOptions = Array.from(selectElement.options);
    
    searchInput.addEventListener('input', (e) => {
      const searchTerm = e.target.value.toLowerCase();
      
      // Filter options
      Array.from(selectElement.options).forEach(option => {
        const text = option.text.toLowerCase();
        if (text.includes(searchTerm)) {
          option.style.display = '';
        } else {
          option.style.display = 'none';
        }
      });
      
      // If search is empty, reset to first option
      if (searchTerm === '') {
        selectElement.selectedIndex = 0;
      }
    });
    
    // Trigger change event on select when search input changes
    searchInput.addEventListener('change', () => {
      selectElement.dispatchEvent(new Event('change'));
    });
  }

  // Form Handlers
  setupForm('etablissement', '/api/directeur/initialize/etablissement');
  setupForm('annee', '/api/directeur/initialize/annee-scolaire');
  setupForm('niveau', '/api/directeur/initialize/niveau');
  setupForm('salle', '/api/directeur/initialize/salle');
  setupForm('classe', '/api/directeur/initialize/classe');
  setupForm('matiere', '/api/directeur/initialize/matiere');
  setupForm('coefficient', '/api/directeur/initialize/coefficient', true);
  setupForm('affectation', '/api/directeur/initialize/affectation', false, true);

  function setupForm(type, url, supportEdit = false, supportFilter = false) {
    const form = document.getElementById(`form-${type}`);
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const dataObj = {};
      formData.forEach((value, key) => {
        if (key === 'estActive' || key === 'isActive') {
          dataObj[key] = formData.get(key) === 'on';
        } else if (key === 'niveauId' || key === 'anneeScolaireId' || key === 'capacite' || key === 'capaciteMax' || key === 'ordre' || 
                   key === 'matiereId' || key === 'professeurId' || key === 'classeId' || key === 'salleId' || key === 'etablissementId') {
          dataObj[key] = parseInt(value);
        } else if (key === 'valeur' || key === 'heuresHebdo') {
          dataObj[key] = parseFloat(value);
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

      // Check if in edit mode
      const editId = form.dataset.editId;
      let requestUrl = url;
      let method = 'POST';

      if (supportEdit && editId) {
        requestUrl = `${url}/${editId}`;
        method = 'PUT';
      }

      try {
        const response = await fetch(requestUrl, {
          method: method,
          headers: headers,
          body: JSON.stringify(dataObj)
        });

        if (response.ok) {
          form.reset();
          delete form.dataset.editId;
          await fetchData();
          if (supportEdit && editId) {
            showMessage('success', `${type.charAt(0).toUpperCase() + type.slice(1)} modifié avec succès !`);
          } else {
            showMessage('success', `${type.charAt(0).toUpperCase() + type.slice(1)} enregistré avec succès !`);
          }
          // Re-apply filter if coefficient or affectation
          if (type === 'coefficient') {
            filterCoefficients();
          }
          if (supportFilter && type === 'affectation') {
            filterAffectations();
          }
          // Re-check prerequisites after successful save
          checkPrerequisites();
        } else {
          const errorText = await response.text();
          // Check if error is a prerequisite error and provide navigation
          if (errorText.includes('Vous devez d\'abord créer un établissement')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('etablissement'); return false;">Aller à Établissement</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer et activer une année scolaire')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('annee'); return false;">Aller à Année Scolaire</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer des niveaux')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('niveau'); return false;">Aller à Niveaux</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer des salles')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('salle'); return false;">Aller à Salles</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer des matières')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('matiere'); return false;">Aller à Matières</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer des classes')) {
            showMessage('error', `${errorText} <a href="#" onclick="goToStep('classe'); return false;">Aller à Classes</a>`);
          } else if (errorText.includes('Vous devez d\'abord créer des professeurs')) {
            showMessage('error', `${errorText} Veuillez contacter l'administration pour créer des professeurs.`);
          } else {
            showMessage('error', `Erreur lors de l'enregistrement: ${errorText}`);
          }
        }
      } catch (error) {
        console.error('Error saving item:', error);
        showMessage('error', `Erreur: ${error.message}`);
      }
    });
  }

  // type: 'success' | 'error' | 'info' | 'default'
  // message: texte principal (peut contenir du HTML simple, ex: un lien)
  // details: optionnel — tableau de { heading: string, items: string[] } pour
  //          afficher des sous-listes bien espacées (ex: erreurs d'import CSV)
  function showMessage(type, message, details) {
    // On repart d'une alerte propre à chaque fois (évite l'empilement d'anciens messages)
    const existingMessages = document.querySelectorAll('.alert');
    existingMessages.forEach(msg => msg.remove());

    const config = {
      success: { icon: 'check-circle', cls: 'alert-success', title: 'Succès' },
      error:   { icon: 'exclamation-circle', cls: 'alert-error', title: 'Erreur' },
      info:    { icon: 'info-circle', cls: 'alert-info', title: 'Information' },
      default: { icon: 'bell', cls: 'alert-default', title: null }
    }[type] || { icon: 'bell', cls: 'alert-default', title: null };

    const messageDiv = document.createElement('div');
    messageDiv.className = `alert ${config.cls}`;

    let detailsHtml = '';
    if (details && details.length) {
      detailsHtml = details.map(section => `
        <span class="alert-detail-heading">${section.heading} (${section.items.length})</span>
        <ul class="alert-detail-list">
          ${section.items.map(item => `<li>${item}</li>`).join('')}
        </ul>
      `).join('');
    }

    messageDiv.innerHTML = `
      <i class="fas fa-${config.icon} alert-icon"></i>
      <div class="alert-body">
        ${config.title ? `<span class="alert-title">${config.title}</span>` : ''}
        <div>${message}</div>
        ${detailsHtml}
      </div>
      <button type="button" class="alert-close" aria-label="Fermer" onclick="this.parentElement.remove()">
        <i class="fas fa-times"></i>
      </button>
    `;

    // Try to use message container first, fallback to active step card body
    const messageContainer = document.getElementById('message-container');
    if (messageContainer) {
      messageContainer.appendChild(messageDiv);
    } else {
      const activeStep = document.querySelector('.step-content.active');
      const cardBody = activeStep?.querySelector('.card-body');
      if (cardBody) {
        cardBody.insertBefore(messageDiv, cardBody.firstChild);
      }
    }

    // Auto-remove after 6 secondes pour succès/info, 12 secondes pour les erreurs
    // (plus long pour laisser le temps de lire une liste d'erreurs détaillées)
    const timeout = type === 'error' ? 12000 : 6000;
    setTimeout(() => messageDiv.remove(), timeout);
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
            const details = [];
            if (result.errors && result.errors.length > 0) {
              details.push({
                heading: 'Erreurs détaillées',
                items: result.errors.map(error => `Ligne ${error.rowNumber} — ${error.tableName} : ${error.errorMessage}`)
              });
            }
            if (result.warnings && result.warnings.length > 0) {
              details.push({
                heading: 'Avertissements',
                items: result.warnings
              });
            }
            showMessage('error', result.message, details);
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