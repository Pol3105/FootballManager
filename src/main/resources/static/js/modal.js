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

  // Setea el valor de un <select> y notifica al fancy-select para refrescar el display
  function setSelect(id, value) {
    var el = document.getElementById(id);
    if (!el) return;
    el.value = value == null ? '' : value;
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  // Setea el returnUrl oculto del modal de jugador (a dónde volver tras guardar)
  function setReturnUrl(url) {
    var el = document.getElementById('player-return-url');
    if (el) el.value = url || '';
  }

  document.querySelectorAll('[data-open-modal]').forEach(function (t) {
    t.addEventListener('click', function (e) {
      var id = t.getAttribute('data-open-modal');
      var modal = document.getElementById(id);
      if (modal) {  // intercepta y abre de forma fluida el modal si está cargado
        e.preventDefault();
        e.stopPropagation();
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
    } else if (window.location.pathname.startsWith('/referees')) {
      plusBtn.setAttribute('data-open-modal', 'referee-modal');
      plusBtn.setAttribute('data-label', 'Nuevo Árbitro');
    } else if (window.location.pathname.startsWith('/players')) {
      plusBtn.setAttribute('data-open-modal', 'player-modal');
      plusBtn.setAttribute('href', '#');
      plusBtn.setAttribute('data-label', 'Nuevo Jugador');
    } else if (window.location.pathname.startsWith('/team/')) {
      plusBtn.setAttribute('data-open-modal', 'player-modal');
      plusBtn.setAttribute('data-label', 'Fichar Jugador');
    } else if (window.location.pathname.startsWith('/tournament/')) {
      plusBtn.setAttribute('data-open-modal', 'match-modal');
      plusBtn.setAttribute('href', '#');
      plusBtn.setAttribute('data-label', 'Programar Partido');
    } else if (window.location.pathname.startsWith('/match/')) {
      plusBtn.setAttribute('data-open-modal', 'comment-modal');
      plusBtn.setAttribute('href', '#');
      plusBtn.setAttribute('data-label', 'Nuevo Comentario');
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

  // ── Editar Árbitro Modal Prefill ────────────────────────
  document.querySelectorAll('.edit-referee-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      var modal = document.getElementById('referee-modal');
      if (!modal) return;

      var id = btn.getAttribute('data-id');
      var name = btn.getAttribute('data-name');
      var surname = btn.getAttribute('data-surname');
      var code = btn.getAttribute('data-code');

      // Llenar campos
      document.getElementById('referee-id').value = id;
      document.getElementById('referee-name').value = name;
      document.getElementById('referee-surname').value = surname;
      document.getElementById('referee-code').value = code;

      // Cambiar Título del Modal y texto del Botón
      document.getElementById('referee-title').textContent = 'Editar Árbitro';
      document.getElementById('referee-submit-btn').textContent = 'Guardar cambios';
      document.getElementById('referee-form').setAttribute('action', '/admin/referee/save');

      open('referee-modal');
    });
  });

  // ── Editar Jugador Modal Prefill ────────────────────────
  document.querySelectorAll('.edit-player-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      var modal = document.getElementById('player-modal');
      if (!modal) return;

      var id = btn.getAttribute('data-id');
      var name = btn.getAttribute('data-name');
      var surname = btn.getAttribute('data-surname');
      var birth = btn.getAttribute('data-birth');
      var height = btn.getAttribute('data-height');
      var position = btn.getAttribute('data-position');
      var team = btn.getAttribute('data-team');

      // Llenar campos
      document.getElementById('player-id').value = id;
      document.getElementById('player-name').value = name;
      document.getElementById('player-surname').value = surname;
      document.getElementById('player-birth').value = birth;
      document.getElementById('player-height').value = height;
      setSelect('player-position', position);
      setSelect('player-team', team);

      // Volver a la página actual tras guardar (equipo o /players)
      setReturnUrl(window.location.pathname + window.location.search);

      // Cambiar Título del Modal y texto del Botón
      document.getElementById('player-title').textContent = 'Editar Jugador';
      document.getElementById('player-submit-btn').textContent = 'Guardar cambios';
      document.getElementById('player-form').setAttribute('action', '/admin/player/save');

      open('player-modal');
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

  // Asegurar que al abrir para añadir árbitro se limpie el formulario
  if (plusBtn && window.location.pathname.startsWith('/referees')) {
    plusBtn.addEventListener('click', function () {
      document.getElementById('referee-id').value = '';
      document.getElementById('referee-name').value = '';
      document.getElementById('referee-surname').value = '';
      document.getElementById('referee-code').value = '';
      document.getElementById('referee-title').textContent = 'Añadir nuevo árbitro';
      document.getElementById('referee-submit-btn').textContent = 'Guardar árbitro';
      document.getElementById('referee-form').setAttribute('action', '/admin/referee/save');
    });
  }

  // Asegurar que al abrir para añadir jugador se limpie el formulario
  if (plusBtn && window.location.pathname.startsWith('/players')) {
    plusBtn.addEventListener('click', function () {
      document.getElementById('player-id').value = '';
      document.getElementById('player-name').value = '';
      document.getElementById('player-surname').value = '';
      document.getElementById('player-birth').value = '';
      document.getElementById('player-height').value = '';
      setSelect('player-position', '');
      setSelect('player-team', '');
      setReturnUrl('');  // jugador genérico → volver a /players (default del backend)
      document.getElementById('player-title').textContent = 'Añadir nuevo jugador';
      document.getElementById('player-submit-btn').textContent = 'Guardar jugador';
      document.getElementById('player-form').setAttribute('action', '/admin/player/save');
    });
  }

  // Asegurar que al abrir para añadir jugador desde la vista de equipo se limpie y pre-seleccione
  if (plusBtn && window.location.pathname.startsWith('/team/')) {
    plusBtn.addEventListener('click', function () {
      document.getElementById('player-id').value = '';
      document.getElementById('player-name').value = '';
      document.getElementById('player-surname').value = '';
      document.getElementById('player-birth').value = '';
      document.getElementById('player-height').value = '';
      setSelect('player-position', '');
      if (typeof CURRENT_TEAM_ID !== 'undefined' && CURRENT_TEAM_ID !== null) {
        setSelect('player-team', CURRENT_TEAM_ID);
      } else {
        setSelect('player-team', '');
      }
      // Volver a la página del equipo tras fichar, no a /players
      setReturnUrl(window.location.pathname + window.location.search);
      document.getElementById('player-title').textContent = 'Fichar Jugador';
      document.getElementById('player-submit-btn').textContent = 'Guardar jugador';
      document.getElementById('player-form').setAttribute('action', '/admin/player/save');
    });
  }

  // Limpiar el formulario del modal de partido al abrir (crear nuevo)
  if (plusBtn && window.location.pathname.startsWith('/tournament/')) {
    plusBtn.addEventListener('click', function () {
      var f = document.getElementById('match-form');
      if (!f) return;
      var ids = ['match-home-score', 'match-away-score', 'match-location', 'match-date'];
      ids.forEach(function (id) { var el = document.getElementById(id); if (el) el.value = ''; });
      // selects: dejar primera opción
      ['match-home-team', 'match-away-team', 'match-referee'].forEach(function (id) {
        var el = document.getElementById(id);
        if (el && el.options.length) setSelect(id, el.options[0].value);
      });
      setSelect('match-status', 'SCHEDULED');
    });
  }

  // ── Botón Hold and Release (Hold to Delete) ─────────────
  var holdDuration = 3000; // 3 segundos
  
  // Equipos
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

    btn.addEventListener('click', function (e) {
      e.stopPropagation();
    });
  });

  // Árbitros
  document.querySelectorAll('.btn-delete-referee').forEach(function (btn) {
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
        window.location.href = '/admin/referee/delete/' + id;
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

    btn.addEventListener('click', function (e) {
      e.stopPropagation();
    });
  });

  // Jugadores
  document.querySelectorAll('.btn-delete-player').forEach(function (btn) {
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
        window.location.href = '/admin/player/delete/' + id;
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

    btn.addEventListener('click', function (e) {
      e.stopPropagation();
    });
  });

  // Partidos
  document.querySelectorAll('.btn-delete-match').forEach(function (btn) {
    var holdTimer = null;
    var id = btn.getAttribute('data-id');
    var progressEl = btn.querySelector('.hold-progress');
    var textEl = btn.querySelector('.hold-text');
    var originalText = textEl.textContent;

    function handleStart(e) {
      e.preventDefault();
      textEl.textContent = 'Soltar';
      progressEl.style.transition = 'width ' + (holdDuration / 1000) + 's linear';
      progressEl.style.width = '100%';
      holdTimer = setTimeout(function () {
        window.location.href = '/admin/match/delete/' + id;
      }, holdDuration);
    }
    function handleEnd() {
      if (holdTimer) { clearTimeout(holdTimer); holdTimer = null; }
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
    btn.addEventListener('click', function (e) { e.stopPropagation(); });
  });

  // Evitar propagación del botón editar al contenedor
  document.querySelectorAll('.edit-team-btn, .edit-referee-btn, .edit-player-btn, .edit-match-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      // Cerrar todos los dropdowns al hacer clic en editar
      document.querySelectorAll('.team-dropdown-menu').forEach(function (el) { el.style.display = 'none'; });
    });
  });

  // Toggle de Dropdown Menu
  document.querySelectorAll('.team-dropdown-trigger').forEach(function (trigger) {
    trigger.addEventListener('click', function (e) {
      e.stopPropagation();
      var menu = trigger.nextElementSibling;
      var isClosed = menu.style.display === 'none';
      
      // Cerrar otros menús abiertos
      document.querySelectorAll('.team-dropdown-menu').forEach(function (el) { el.style.display = 'none'; });
      
      menu.style.display = isClosed ? 'flex' : 'none';
    });
  });

  // Cerrar menús al hacer click fuera
  document.addEventListener('click', function () {
    document.querySelectorAll('.team-dropdown-menu').forEach(function (el) {
      el.style.display = 'none';
    });
  });

  // Evento click en la tarjeta para navegar
  document.querySelectorAll('.team-clickable-card').forEach(function (card) {
    card.addEventListener('click', function () {
      var url = card.getAttribute('data-href');
      if (url) {
        window.location.href = url;
      }
    });
  });
})();
