// Mapa de puntos animado para el panel del registro.
// Port vanilla del componente React "DotMap" (sin framer-motion), con la
// paleta marrón del tema. Dibuja una silueta de "mundo" en puntos + rutas
// animadas que viajan entre nodos.
(function () {
  var canvas = document.querySelector('[data-dotmap]');
  if (!canvas) return;
  var ctx = canvas.getContext('2d');
  if (!ctx) return;

  var DOT = 'rgba(100, 74, 64, ';        // marrón primary
  var MOVE = '#8a6a55';                    // marrón claro (punto móvil)
  var GLOW = 'rgba(138, 106, 85, 0.4)';

  var routes = [
    { start: { x: 100, y: 150, delay: 0 },   end: { x: 200, y: 80, delay: 2 } },
    { start: { x: 200, y: 80,  delay: 2 },   end: { x: 260, y: 120, delay: 4 } },
    { start: { x: 50,  y: 50,  delay: 1 },   end: { x: 150, y: 180, delay: 3 } },
    { start: { x: 280, y: 60,  delay: 0.5 }, end: { x: 180, y: 180, delay: 2.5 } }
  ];

  var dims = { width: 0, height: 0 };
  var dots = [];

  function generateDots(w, h) {
    var out = [];
    var gap = 12;
    for (var x = 0; x < w; x += gap) {
      for (var y = 0; y < h; y += gap) {
        var inShape =
          ((x < w * 0.25 && x > w * 0.05) && (y < h * 0.4 && y > h * 0.1)) ||   // N. América
          ((x < w * 0.25 && x > w * 0.15) && (y < h * 0.8 && y > h * 0.4)) ||   // S. América
          ((x < w * 0.45 && x > w * 0.3)  && (y < h * 0.35 && y > h * 0.15)) || // Europa
          ((x < w * 0.5  && x > w * 0.35) && (y < h * 0.65 && y > h * 0.35)) || // África
          ((x < w * 0.7  && x > w * 0.45) && (y < h * 0.5 && y > h * 0.1)) ||   // Asia
          ((x < w * 0.8  && x > w * 0.65) && (y < h * 0.8 && y > h * 0.6));     // Oceanía
        if (inShape && Math.random() > 0.3) {
          out.push({ x: x, y: y, opacity: Math.random() * 0.5 + 0.2 });
        }
      }
    }
    return out;
  }

  function resize() {
    var parent = canvas.parentElement;
    if (!parent) return;
    var w = parent.clientWidth;
    var h = parent.clientHeight;
    dims.width = w; dims.height = h;
    canvas.width = w; canvas.height = h;
    dots = generateDots(w, h);
  }

  var start = Date.now();

  function frame() {
    var w = dims.width, h = dims.height;
    ctx.clearRect(0, 0, w, h);

    // puntos de fondo
    for (var i = 0; i < dots.length; i++) {
      var d = dots[i];
      ctx.beginPath();
      ctx.arc(d.x, d.y, 1, 0, Math.PI * 2);
      ctx.fillStyle = DOT + d.opacity + ')';
      ctx.fill();
    }

    // rutas animadas
    var t = (Date.now() - start) / 1000;
    for (var r = 0; r < routes.length; r++) {
      var route = routes[r];
      var elapsed = t - route.start.delay;
      if (elapsed <= 0) continue;
      var progress = Math.min(elapsed / 3, 1);
      var x = route.start.x + (route.end.x - route.start.x) * progress;
      var y = route.start.y + (route.end.y - route.start.y) * progress;

      ctx.beginPath();
      ctx.moveTo(route.start.x, route.start.y);
      ctx.lineTo(x, y);
      ctx.strokeStyle = DOT + '0.9)';
      ctx.lineWidth = 1.5;
      ctx.stroke();

      ctx.beginPath();
      ctx.arc(route.start.x, route.start.y, 3, 0, Math.PI * 2);
      ctx.fillStyle = DOT + '0.9)';
      ctx.fill();

      ctx.beginPath();
      ctx.arc(x, y, 6, 0, Math.PI * 2);
      ctx.fillStyle = GLOW;
      ctx.fill();
      ctx.beginPath();
      ctx.arc(x, y, 3, 0, Math.PI * 2);
      ctx.fillStyle = MOVE;
      ctx.fill();

      if (progress === 1) {
        ctx.beginPath();
        ctx.arc(route.end.x, route.end.y, 3, 0, Math.PI * 2);
        ctx.fillStyle = DOT + '0.9)';
        ctx.fill();
      }
    }

    if (t > 15) start = Date.now(); // reinicia el ciclo
    requestAnimationFrame(frame);
  }

  resize();
  if (window.ResizeObserver && canvas.parentElement) {
    new ResizeObserver(resize).observe(canvas.parentElement);
  } else {
    window.addEventListener('resize', resize);
  }
  requestAnimationFrame(frame);
})();
