// Efecto magnify del dock estilo macOS (port vanilla del componente framer).
// Escala cada item según la distancia horizontal al cursor.
(function () {
  var dock = document.querySelector('[data-dock]');
  if (!dock) return;

  var items = Array.prototype.slice.call(dock.querySelectorAll('.dock__item'));
  if (!items.length) return;

  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduce) return;

  var MAX = 1.55;   // escala máxima (item bajo el cursor)
  var RANGE = 130;  // radio de influencia en px

  function update(mouseX) {
    items.forEach(function (item) {
      var rect = item.getBoundingClientRect();
      var center = rect.left + rect.width / 2;
      var dist = Math.abs(mouseX - center);
      var scale = 1;
      if (dist < RANGE) {
        scale = 1 + (MAX - 1) * (1 - dist / RANGE);
      }
      item.style.transform = 'scale(' + scale.toFixed(3) + ')';
    });
  }

  dock.addEventListener('mousemove', function (e) { update(e.clientX); });
  dock.addEventListener('mouseleave', function () {
    items.forEach(function (item) { item.style.transform = 'scale(1)'; });
  });
})();
