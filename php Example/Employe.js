document.addEventListener('DOMContentLoaded', () => {
  const formPanel = document.getElementById('employee-form-panel');
  const emailInput = document.getElementById('employee-email');
  const emailFeedback = document.getElementById('employee-email-feedback');
  const phoneInput = document.getElementById('employee-telephone');
  const phoneFeedback = document.getElementById('employee-telephone-feedback');
  const formError = document.getElementById('employee-form-error');
  const form = formPanel ? formPanel.querySelector('form') : null;
  const fonction = document.getElementById('employee-fonction');
  const matiereGroup = document.getElementById('employee-matiere-group');
  let debounceTimer = null;
  let emailIsDuplicate = false;
  let phoneIsDuplicate = false;
  let phoneIsValid = true;

  function toggleEmployeeForm() {
    if (!formPanel) {
      return;
    }

    formPanel.style.display = formPanel.style.display === 'none' ? 'block' : 'none';
  }

  function syncEmployeeFields() {
    const isProfesseur = fonction && fonction.value === 'professeur';

    if (matiereGroup) {
      matiereGroup.style.display = isProfesseur ? 'block' : 'none';
    }
  }

  function showFeedback(message, isError) {
    if (!emailFeedback) {
      return;
    }

    emailFeedback.textContent = message || '';
    emailFeedback.style.color = isError ? '#b91c1c' : '#15803d';
    emailFeedback.style.fontWeight = isError ? '600' : '500';
  }

  function showPhoneFeedback(message, isError) {
    if (!phoneFeedback) {
      return;
    }

    phoneFeedback.textContent = message || '';
    phoneFeedback.style.color = isError ? '#b91c1c' : '#15803d';
    phoneFeedback.style.fontWeight = isError ? '600' : '500';
  }

  function normalizePhone(value) {
    return (value || '').replace(/\D+/g, '');
  }

  function isValidMalagasyPhone(value) {
    return /^0(?:32|33|34|37|38)\d{7}$/.test(normalizePhone(value));
  }

  function showFormError(message) {
    if (!formError) {
      return;
    }

    if (!message) {
      formError.style.display = 'none';
      formError.textContent = '';
      return;
    }

    formError.textContent = message;
    formError.style.display = 'block';
  }

  async function checkEmailAvailability(email) {
    if (!email) {
      emailIsDuplicate = false;
      showFeedback('', false);
      showFormError('');
      return;
    }

    try {
      const response = await fetch(`/directeur/employes/check-email?email=${encodeURIComponent(email)}`, {
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
        },
      });

      const data = await response.json();
      emailIsDuplicate = Boolean(data.exists);
      showFeedback(data.message || '', emailIsDuplicate);

      if (emailIsDuplicate) {
        showFormError(data.message || 'Cette adresse email est déjà utilisée.');
      } else {
        showFormError('');
      }
    } catch (error) {
      emailIsDuplicate = false;
      showFeedback('Impossible de vérifier cet email pour le moment. Réessayez dans quelques secondes.', true);
    }
  }

  async function checkPhoneAvailability(telephone) {
    const rawValue = (telephone || '').trim();

    if (!rawValue) {
      phoneIsDuplicate = false;
      phoneIsValid = true;
      showPhoneFeedback('', false);
      showFormError('');
      return;
    }

    if (!isValidMalagasyPhone(rawValue)) {
      phoneIsDuplicate = false;
      phoneIsValid = false;
      showPhoneFeedback('Numéro invalide: utilisez un numéro malgache commençant par 032, 033, 034, 037 ou 038 avec 10 chiffres.', true);
      showFormError('');
      return;
    }

    try {
      const response = await fetch(`/directeur/employes/check-phone?telephone=${encodeURIComponent(rawValue)}`, {
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
        },
      });

      const data = await response.json();
      phoneIsValid = Boolean(data.valid);
      phoneIsDuplicate = Boolean(data.exists);
      showPhoneFeedback(data.message || '', phoneIsDuplicate || !phoneIsValid);

      if (phoneIsDuplicate) {
        showFormError(data.message || 'Ce numéro est déjà utilisé.');
      } else if (!phoneIsValid) {
        showFormError(data.message || 'Numéro invalide.');
      } else {
        showFormError('');
      }
    } catch (error) {
      phoneIsDuplicate = false;
      phoneIsValid = true;
      showPhoneFeedback('Impossible de vérifier le numéro pour le moment. Réessayez dans quelques secondes.', true);
    }
  }

  if (fonction) {
    fonction.addEventListener('change', syncEmployeeFields);
  }

  if (emailInput) {
    emailInput.addEventListener('input', () => {
      const value = emailInput.value.trim();
      clearTimeout(debounceTimer);
      debounceTimer = window.setTimeout(() => {
        checkEmailAvailability(value);
      }, 350);
    });

    emailInput.addEventListener('blur', () => {
      clearTimeout(debounceTimer);
      checkEmailAvailability(emailInput.value.trim());
    });
  }

  if (phoneInput) {
    phoneInput.addEventListener('input', () => {
      const value = phoneInput.value.trim();
      clearTimeout(debounceTimer);
      debounceTimer = window.setTimeout(() => {
        checkPhoneAvailability(value);
      }, 350);
    });

    phoneInput.addEventListener('blur', () => {
      clearTimeout(debounceTimer);
      checkPhoneAvailability(phoneInput.value.trim());
    });
  }

  if (form) {
    form.addEventListener('submit', async (event) => {
      showFormError('');
      const email = emailInput ? emailInput.value.trim() : '';
      const telephone = phoneInput ? phoneInput.value.trim() : '';

      if (!email) {
        showFormError('L\'adresse email est obligatoire pour créer un employé.');
        event.preventDefault();
        return;
      }

      if (emailIsDuplicate) {
        showFormError('Impossible d\'enregistrer : cette adresse email est déjà utilisée par un autre compte.');
        event.preventDefault();
        return;
      }

      if (telephone && !isValidMalagasyPhone(telephone)) {
        showFormError('Impossible d\'enregistrer : le numéro doit être malgache et commencer par 032, 033, 034, 037 ou 038.');
        event.preventDefault();
        return;
      }

      if (phoneIsDuplicate) {
        showFormError('Impossible d\'enregistrer : ce numéro de téléphone existe déjà dans la base.');
        event.preventDefault();
        return;
      }

      if (emailInput) {
        try {
          const response = await fetch(`/directeur/employes/check-email?email=${encodeURIComponent(email)}`, {
            headers: {
              'X-Requested-With': 'XMLHttpRequest',
            },
          });
          const data = await response.json();

          if (data.exists) {
            emailIsDuplicate = true;
            showFeedback(data.message || 'Cette adresse email est déjà utilisée.', true);
            showFormError(data.message || 'Cette adresse email est déjà utilisée.');
            event.preventDefault();
          }
        } catch (error) {
          showFormError('Impossible de vérifier l\'email avant l\'enregistrement. Réessayez ou rechargez la page.');
          event.preventDefault();
        }
      }

      if (phoneInput && telephone) {
        try {
          const response = await fetch(`/directeur/employes/check-phone?telephone=${encodeURIComponent(telephone)}`, {
            headers: {
              'X-Requested-With': 'XMLHttpRequest',
            },
          });
          const data = await response.json();

          if (!data.valid) {
            phoneIsValid = false;
            showPhoneFeedback(data.message || 'Numéro invalide.', true);
            showFormError(data.message || 'Numéro invalide.');
            event.preventDefault();
            return;
          }

          if (data.exists) {
            phoneIsDuplicate = true;
            showPhoneFeedback(data.message || 'Ce numéro est déjà utilisé.', true);
            showFormError(data.message || 'Ce numéro est déjà utilisé.');
            event.preventDefault();
          }
        } catch (error) {
          showFormError('Impossible de vérifier le numéro avant l\'enregistrement. Réessayez ou rechargez la page.');
          event.preventDefault();
        }
      }
    });
  }

  window.toggleEmployeeForm = toggleEmployeeForm;
  window.syncEmployeeFields = syncEmployeeFields;

  syncEmployeeFields();
});