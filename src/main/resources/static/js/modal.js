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
})();
