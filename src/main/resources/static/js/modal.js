// Abrir/cerrar modales. Disparadores: [data-open-modal="ID"]; cierre por
// [data-close-modal], click en el overlay o tecla Esc.
(function () {
  function open(id) {
    var m = document.getElementById(id);
    if (!m) return;
    m.hidden = false;
    document.body.style.overflow = 'hidden';
    var first = m.querySelector('input, textarea, select');
    if (first) setTimeout(function () { first.focus(); }, 50);
  }
  function close(m) {
    if (!m) return;
    m.hidden = true;
    document.body.style.overflow = '';
  }

  document.querySelectorAll('[data-open-modal]').forEach(function (t) {
    t.addEventListener('click', function (e) {
      var id = t.getAttribute('data-open-modal');
      if (document.getElementById(id)) {  // solo intercepta si el modal existe (admin)
        e.preventDefault();
        open(id);
      }
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(function (overlay) {
    overlay.addEventListener('click', function (e) {
      if (e.target === overlay) close(overlay);
    });
    overlay.querySelectorAll('[data-close-modal]').forEach(function (b) {
      b.addEventListener('click', function () { close(overlay); });
    });
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-overlay:not([hidden])').forEach(close);
    }
  });

  // Cambiar dinámicamente el modal del botón del dock según la página activa
  var plusBtn = document.querySelector('.dock__item--admin');
  if (plusBtn) {
    if (window.location.pathname.startsWith('/teams')) {
      plusBtn.setAttribute('data-open-modal', 'team-modal');
      plusBtn.setAttribute('data-label', 'Nuevo Equipo');
    } else {
      plusBtn.setAttribute('data-open-modal', 'tournament-modal');
      plusBtn.setAttribute('data-label', 'Nuevo Torneo');
    }
  }

  // ── 3D Frosted Glass Card Tilt & Glare Effect ──────────
  document.querySelectorAll('.frosted-glass-card').forEach(function (card) {
    card.addEventListener('mousemove', function (e) {
      var rect = card.getBoundingClientRect();
      var x = e.clientX - rect.left;
      var y = e.clientY - rect.top;
      var centerX = rect.width / 2;
      var centerY = rect.height / 2;

      var rotateY = ((x - centerX) / centerX) * 10;
      var rotateX = ((y - centerY) / centerY) * -10;

      card.style.transform = 'rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg)';
      card.style.setProperty('--mouse-x', x + 'px');
      card.style.setProperty('--mouse-y', y + 'px');
    });

    card.addEventListener('mouseleave', function () {
      card.style.transform = 'rotateX(0deg) rotateY(0deg)';
    });
  });

  // ── Editar Equipo Modal Prefill ────────────────────────
  document.querySelectorAll('.edit-team-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      var modal = document.getElementById('team-modal');
      if (!modal) return;

      var id = btn.getAttribute('data-id');
      var name = btn.getAttribute('data-name');
      var city = btn.getAttribute('data-city');
      var year = btn.getAttribute('data-year');
      var tournamentsStr = btn.getAttribute('data-tournaments') || '';
      var activeTournaments = tournamentsStr.split(',').filter(Boolean);

      // Llenar campos
      document.getElementById('team-id').value = id;
      document.getElementById('team-name').value = name;
      document.getElementById('team-city').value = city;
      document.getElementById('team-year').value = year;

      // Cambiar Título del Modal y texto del Botón
      document.getElementById('team-title').textContent = 'Editar Equipo';
      document.getElementById('team-submit-btn').textContent = 'Guardar cambios';
      document.getElementById('team-form').setAttribute('action', '/admin/team/edit/' + id);

      // Marcar checkboxes de torneos
      modal.querySelectorAll('.tournament-checkbox').forEach(function (cb) {
        cb.checked = activeTournaments.includes(cb.value);
      });

      open('team-modal');
    });
  });

  // Asegurar que al abrir para añadir equipo se limpie el formulario
  if (plusBtn && window.location.pathname.startsWith('/teams')) {
    plusBtn.addEventListener('click', function () {
      document.getElementById('team-id').value = '';
      document.getElementById('team-name').value = '';
      document.getElementById('team-city').value = '';
      document.getElementById('team-year').value = '';
      document.getElementById('team-title').textContent = 'Añadir nuevo equipo';
      document.getElementById('team-submit-btn').textContent = 'Guardar equipo';
      document.getElementById('team-form').setAttribute('action', '/admin/team/new');
      document.querySelectorAll('.tournament-checkbox').forEach(function (cb) {
        cb.checked = false;
      });
    });
  }

  // ── Botón Hold and Release (Hold to Delete) ─────────────
  var holdDuration = 3000; // 3 segundos
  document.querySelectorAll('.btn-delete-team').forEach(function (btn) {
    var holdTimer = null;
    var startTime = null;
    var id = btn.getAttribute('data-id');
    var progressEl = btn.querySelector('.hold-progress');
    var textEl = btn.querySelector('.hold-text');
    var originalText = textEl.textContent;

    function handleStart(e) {
      e.preventDefault();
      startTime = Date.now();
      textEl.textContent = 'Soltar';
      
      progressEl.style.transition = 'width ' + (holdDuration / 1000) + 's linear';
      progressEl.style.width = '100%';

      holdTimer = setTimeout(function () {
        // Ejecutar borrado si se mantiene 3 segundos
        window.location.href = '/admin/team/delete/' + id;
      }, holdDuration);
    }

    function handleEnd() {
      if (holdTimer) {
        clearTimeout(holdTimer);
        holdTimer = null;
      }
      textEl.textContent = originalText;
      progressEl.style.transition = 'width 0.15s ease-out';
      progressEl.style.width = '0%';
    }

    btn.addEventListener('mousedown', handleStart);
    btn.addEventListener('mouseup', handleEnd);
    btn.addEventListener('mouseleave', handleEnd);

    btn.addEventListener('touchstart', handleStart, { passive: false });
    btn.addEventListener('touchend', handleEnd);
    btn.addEventListener('touchcancel', handleEnd);
  });
})();
