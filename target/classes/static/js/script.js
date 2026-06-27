  /* ============================================================
     ROLE CONFIGURATIONS
  ============================================================ */
  const roles = {
    directeur: {
      name: 'M. Rakoto',
      role: 'Directeur',
      initials: 'DR',
      defaultPage: 'dashboard',
      nav: 'nav-directeur'
    },
    secretariat: {
      name: 'Mme. Rasoa',
      role: 'Secrétaire',
      initials: 'RS',
      defaultPage: 'paiement',
      nav: 'nav-secretariat'
    },
    professeur: {
      name: 'Prof. Rabe',
      role: 'Professeur',
      initials: 'RB',
      defaultPage: 'emploi',
      nav: 'nav-professeur'
    },
    etudiant: {
      name: 'Rakoto Jean',
      role: 'Étudiant',
      initials: 'RJ',
      defaultPage: 'emploi',
      nav: 'nav-etudiant'
    }
  };

  const pageTitles = {
    'dir-dashboard': 'Tableau de bord',
    'dir-finances': 'Finances & Bénéfices',
    'dir-professeurs': 'Corps Professoral',
    'dir-prof-profil': 'Profil Professeur',
    'dir-ecolages': 'Écolages du Mois',
    'dir-edt': 'Emplois du Temps',
    'sec-paiements': 'Ajouter un Paiement',
    'sec-bilan': 'Bilan de Paiement',
    'sec-eleves': 'Liste des Élèves',
    'sec-profils': 'Profil Élève',
    'prof-emploi': 'Emploi du Temps',
    'prof-notes': 'Notes des Élèves',
    'prof-devoirs': 'Devoirs & Leçons',
    'prof-bulletins': 'Bulletins',
    'prof-profil': 'Mon Profil',
    'etu-emploi': 'Mon Emploi du Temps',
    'etu-notes': 'Mes Notes',
    'etu-bulletin': 'Mon Bulletin',
    'etu-devoirs': 'Devoirs & Leçons',
    'actualites': 'Actualités',
    'notifications-page': 'Notifications',
  };

  /* ============================================================
     PAGE PATHS MAPPING - REMOVED (Now using standard navigation)
  ============================================================ */

  /* ============================================================
     SWITCH ROLE
  ============================================================ */
  function switchRole(roleKey) {
    const r = roles[roleKey];
    if (!r) return;

    // Update user info
    document.getElementById('sidebar-user-name').textContent = r.name;
    document.getElementById('sidebar-user-role').textContent = r.role;
    document.getElementById('sidebar-avatar-initials').textContent = r.initials;
    document.getElementById('topbar-user-name').textContent = r.name;
    document.getElementById('topbar-user-role').textContent = r.role;
    document.getElementById('topbar-avatar-initials').textContent = r.initials;

    // Show/hide navs
    ['nav-directeur','nav-secretariat','nav-professeur','nav-etudiant'].forEach(id => {
      document.getElementById(id).style.display = 'none';
    });
    document.getElementById(r.nav).style.display = 'block';

    // Navigate to default page for the role
    window.location.href = '/' + roleKey + '/' + r.defaultPage;

    showToast('🔄 Vue changée : ' + r.role);
  }

  /* ============================================================
     SHOW PAGE - REMOVED (Now using standard navigation)
  ============================================================ */

  /* ============================================================
     MODALS
  ============================================================ */
  function openModal(id) {
    document.getElementById(id).classList.add('open');
  }
  function closeModal(id) {
    document.getElementById(id).classList.remove('open');
  }
  // Close on backdrop click
  document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) backdrop.classList.remove('open');
    });
  });

  /* ============================================================
     NOTIFICATIONS
  ============================================================ */
  function toggleNotif() {
    document.getElementById('notif-dropdown').classList.toggle('open');
  }
  document.addEventListener('click', (e) => {
    const notifPanel = document.querySelector('.notif-panel');
    const dropdown = document.getElementById('notif-dropdown');
    if (!e.target.closest('.topbar-btn') || !dropdown.contains(e.target)) {
      if (!e.target.closest('.topbar-btn')) {
        dropdown.classList.remove('open');
      }
    }
  });

  /* ============================================================
     TOAST
  ============================================================ */
  function showToast(message, duration = 3000) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.style.position = 'fixed';
      container.style.bottom = '20px';
      container.style.right = '20px';
      container.style.zIndex = '9999';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.style.background = 'var(--clr-nav)';
    toast.style.color = 'white';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '8px';
    toast.style.marginTop = '10px';
    toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    toast.innerHTML = message;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(20px)';
      toast.style.transition = 'all .3s ease';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  /* ============================================================
     POLL SELECTION
  ============================================================ */
  function selectPoll(el) {
    const parent = el.parentElement;
    parent.querySelectorAll('.poll-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
  }

  /* ============================================================
     FILTER PILLS (interactive)
  ============================================================ */
  document.querySelectorAll('.filter-pills').forEach(group => {
    group.querySelectorAll('.pill').forEach(pill => {
      pill.addEventListener('click', () => {
        group.querySelectorAll('.pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
      });
    });
  });

  /* ============================================================
     TABS
  ============================================================ */
  document.querySelectorAll('.tabs').forEach(tabs => {
    tabs.querySelectorAll('.tab').forEach(tab => {
      tab.addEventListener('click', () => {
        tabs.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
      });
    });
  });

  /* ============================================================
     KEYBOARD: ESC closes modal
  ============================================================ */
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-backdrop.open').forEach(m => m.classList.remove('open'));
      document.getElementById('notif-dropdown').classList.remove('open');
    }
  });

  /* ============================================================
     THEME TOGGLE
  ============================================================ */
  const themeToggle = document.getElementById('themeToggle');
  let isDark = false;
  themeToggle.addEventListener('click', () => {
    isDark = !isDark;
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
    themeToggle.innerHTML = isDark ? '<i class="fas fa-sun"></i>' : '<i class="fas fa-moon"></i>';
    showToast(isDark ? '🌙 Thème sombre activé' : '☀️ Thème clair activé');
  });

  /* ============================================================
     PARTICLE CANVAS
  ============================================================ */
  (function initParticles() {
    const canvas = document.getElementById('particle-canvas');
    const ctx = canvas.getContext('2d');
    let W, H, particles = [], mouse = { x: -999, y: -999 };

    function resize() {
      W = canvas.width  = window.innerWidth;
      H = canvas.height = window.innerHeight;
    }
    resize();
    window.addEventListener('resize', resize);
    window.addEventListener('mousemove', e => { mouse.x = e.clientX; mouse.y = e.clientY; });

    const COLORS = ['#00FF85', '#0A4DFF', '#00D46E', '#4D7AFF', '#00FF8599'];

    function Particle() {
      this.reset();
    }
    Particle.prototype.reset = function() {
      this.x  = Math.random() * W;
      this.y  = Math.random() * H;
      this.r  = Math.random() * 2.2 + .4;
      this.vx = (Math.random() - .5) * .35;
      this.vy = (Math.random() - .5) * .35;
      this.color = COLORS[Math.floor(Math.random() * COLORS.length)];
      this.life  = 1;
    };

    for (let i = 0; i < 80; i++) particles.push(new Particle());

    function draw() {
      ctx.clearRect(0, 0, W, H);

      // Draw connections
      for (let i = 0; i < particles.length; i++) {
        const a = particles[i];
        for (let j = i + 1; j < particles.length; j++) {
          const b = particles[j];
          const dx = a.x - b.x, dy = a.y - b.y;
          const dist = Math.sqrt(dx*dx + dy*dy);
          if (dist < 120) {
            ctx.beginPath();
            ctx.strokeStyle = `rgba(0,255,133,${.12 * (1 - dist/120)})`;
            ctx.lineWidth = .5;
            ctx.moveTo(a.x, a.y);
            ctx.lineTo(b.x, b.y);
            ctx.stroke();
          }
        }

        // Mouse interaction
        const mx = a.x - mouse.x, my = a.y - mouse.y;
        const md = Math.sqrt(mx*mx + my*my);
        if (md < 100) {
          a.vx += mx / md * .04;
          a.vy += my / md * .04;
        }

        a.vx *= .98; a.vy *= .98;
        a.x += a.vx; a.y += a.vy;

        if (a.x < 0 || a.x > W) a.vx *= -1;
        if (a.y < 0 || a.y > H) a.vy *= -1;

        ctx.beginPath();
        ctx.arc(a.x, a.y, a.r, 0, Math.PI*2);
        ctx.fillStyle = a.color;
        ctx.fill();
      }

      requestAnimationFrame(draw);
    }
    draw();
  })();

  /* Init */
  window.addEventListener('DOMContentLoaded', () => {
    // Set active nav item based on current URL
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(item => {
      const href = item.getAttribute('href');
      if (href && currentPath.includes(href)) {
        item.classList.add('active');
      }
    });
    
    // Set role selector based on current path (without redirecting)
    const roleSelect = document.getElementById('roleSelect');
    if (roleSelect) {
      if (currentPath.includes('/directeur')) {
        roleSelect.value = 'directeur';
      } else if (currentPath.includes('/secretariat')) {
        roleSelect.value = 'secretariat';
      } else if (currentPath.includes('/professeur')) {
        roleSelect.value = 'professeur';
      } else if (currentPath.includes('/etudiant')) {
        roleSelect.value = 'etudiant';
      }
    }
    
    setTimeout(() => showToast('👋 Bienvenue sur LycéePro !'), 800);
  });