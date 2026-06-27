

function syncEmployeeFields() {
  const fonction = document.getElementById('employee-fonction');
  const matiereGroup = document.getElementById('employee-matiere-group');
  
  if (fonction && matiereGroup) {
    if (fonction.value === 'professeur') {
      matiereGroup.style.display = 'block';
    } else {
      matiereGroup.style.display = 'none';
    }
  }
}

// Email validation
document.addEventListener('DOMContentLoaded', function() {
  const emailInput = document.getElementById('employee-email');
  const emailFeedback = document.getElementById('employee-email-feedback');
  let emailCheckTimeout;

  if (emailInput) {
    emailInput.addEventListener('input', function() {
      clearTimeout(emailCheckTimeout);
      const email = this.value.trim();
      
      if (email === '') {
        if (emailFeedback) {
          emailFeedback.textContent = '';
          emailFeedback.style.color = '';
        }
        return;
      }

      emailCheckTimeout = setTimeout(() => {
        fetch('/directeur/employes/check-email?email=' + encodeURIComponent(email))
          .then(response => response.json())
          .then(data => {
            if (emailFeedback) {
              if (data.exists) {
                emailFeedback.textContent = data.message;
                emailFeedback.style.color = '#b91c1c';
              } else {
                emailFeedback.textContent = data.message;
                emailFeedback.style.color = '#16a34a';
              }
            }
          })
          .catch(error => {
            console.error('Error checking email:', error);
          });
      }, 500);
    });
  }

  // Phone validation
  const phoneInput = document.getElementById('employee-telephone');
  const phoneFeedback = document.getElementById('employee-telephone-feedback');
  let phoneCheckTimeout;

  if (phoneInput) {
    phoneInput.addEventListener('input', function() {
      clearTimeout(phoneCheckTimeout);
      const telephone = this.value.trim();
      
      if (telephone === '') {
        if (phoneFeedback) {
          phoneFeedback.textContent = '';
          phoneFeedback.style.color = '';
        }
        return;
      }

      phoneCheckTimeout = setTimeout(() => {
        fetch('/directeur/employes/check-phone?telephone=' + encodeURIComponent(telephone))
          .then(response => response.json())
          .then(data => {
            if (phoneFeedback) {
              if (!data.valid) {
                phoneFeedback.textContent = data.message;
                phoneFeedback.style.color = '#b91c1c';
              } else if (data.exists) {
                phoneFeedback.textContent = data.message;
                phoneFeedback.style.color = '#b91c1c';
              } else {
                phoneFeedback.textContent = data.message + ' (' + data.normalized + ')';
                phoneFeedback.style.color = '#16a34a';
              }
            }
          })
          .catch(error => {
            console.error('Error checking phone:', error);
          });
      }, 500);
    });
  }

  // Form submission
  const employeeForm = document.getElementById('employee-form');
  if (employeeForm) {
    employeeForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const formData = new FormData(this);
      
      fetch(this.action, {
        method: 'POST',
        body: formData
      })
      .then(response => {
        if (response.ok) {
          closeModal('modal-add-prof');
          this.reset();
          if (emailFeedback) emailFeedback.textContent = '';
          if (phoneFeedback) phoneFeedback.textContent = '';
          window.location.reload();
        } else {
          return response.json().then(data => {
            throw new Error(data.message || 'Erreur lors de l\'enregistrement');
          });
        }
      })
      .catch(error => {
        const errorDiv = document.getElementById('employee-form-error');
        if (errorDiv) {
          errorDiv.textContent = error.message;
          errorDiv.style.display = 'block';
        }
      });
    });
  }
});
