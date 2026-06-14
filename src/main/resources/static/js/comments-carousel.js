// Carrusel de comentarios estilo testimonial: una tarjeta a la vez,
// flechas + puntos, auto-rotación cada 6s (pausa al hover).
(function () {
  var AUTO_MS = 6000;

  document.querySelectorAll('[data-comments-carousel]').forEach(function (root) {
    var slides = Array.prototype.slice.call(root.querySelectorAll('.cc-slide'));
    var dots = Array.prototype.slice.call(root.querySelectorAll('.cc-dot'));
    if (slides.length === 0) return;

    var index = 0;
    var timer = null;

    function show(i) {
      index = (i + slides.length) % slides.length;
      slides.forEach(function (s, k) { s.classList.toggle('is-active', k === index); });
      dots.forEach(function (d, k) { d.classList.toggle('is-active', k === index); });
    }
    function next() { show(index + 1); }
    function prev() { show(index - 1); }

    function start() {
      if (slides.length <= 1) return;
      stop();
      timer = setInterval(next, AUTO_MS);
    }
    function stop() { if (timer) { clearInterval(timer); timer = null; } }

    var prevBtn = root.querySelector('[data-cc-prev]');
    var nextBtn = root.querySelector('[data-cc-next]');
    if (prevBtn) prevBtn.addEventListener('click', function () { prev(); start(); });
    if (nextBtn) nextBtn.addEventListener('click', function () { next(); start(); });
    dots.forEach(function (d) {
      d.addEventListener('click', function () { show(Number(d.getAttribute('data-dot'))); start(); });
    });

    root.addEventListener('mouseenter', stop);
    root.addEventListener('mouseleave', start);

    show(0);
    start();
  });
})();
