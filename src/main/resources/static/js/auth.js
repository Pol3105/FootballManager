// Toggle de visibilidad de contraseña en las páginas de auth.
(function () {
  document.querySelectorAll('[data-pass-toggle]').forEach(function (btn) {
    var input = btn.parentElement.querySelector('[data-pass-input]');
    if (!input) return;
    btn.addEventListener('click', function () {
      var show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      btn.setAttribute('aria-label', show ? 'Ocultar contraseña' : 'Mostrar contraseña');
      // Tacha el ojo cuando la contraseña está visible
      btn.style.opacity = show ? '1' : '0.6';
    });
  });
})();
