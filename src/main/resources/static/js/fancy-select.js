// Fancy Select — mejora progresiva de <select> nativos.
// Mantiene el <select> real como fuente de verdad (binding/envío del form).
// Para refrescar el display tras setear .value por código: dispara
// select.dispatchEvent(new Event('change', { bubbles: true })).
(function () {
  var CHEVRON = '<svg class="fancy-select__chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"></path></svg>';
  var CHECK = '<svg class="fancy-select__check" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"></path></svg>';

  var openWrap = null;

  function closeAll(except) {
    document.querySelectorAll('.fancy-select.is-open').forEach(function (w) {
      if (w !== except) w.classList.remove('is-open');
    });
    if (openWrap && openWrap !== except) openWrap = null;
  }

  function enhance(select) {
    if (select.dataset.fancyDone) return;
    if (select.multiple || select.dataset.noFancy != null) return;
    select.dataset.fancyDone = '1';

    var wrap = document.createElement('div');
    wrap.className = 'fancy-select';
    select.parentNode.insertBefore(wrap, select);
    wrap.appendChild(select);
    select.classList.add('fancy-select__native');

    var trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.className = 'fancy-select__trigger';
    trigger.setAttribute('aria-haspopup', 'listbox');
    trigger.setAttribute('aria-expanded', 'false');
    if (select.disabled) trigger.disabled = true;
    var valueEl = document.createElement('span');
    valueEl.className = 'fancy-select__value';
    trigger.appendChild(valueEl);
    trigger.insertAdjacentHTML('beforeend', CHEVRON);
    wrap.appendChild(trigger);

    var menu = document.createElement('div');
    menu.className = 'fancy-select__menu';
    menu.setAttribute('role', 'listbox');
    wrap.appendChild(menu);

    function buildItems() {
      menu.innerHTML = '';
      Array.prototype.forEach.call(select.options, function (opt) {
        var item = document.createElement('div');
        item.className = 'fancy-select__item';
        item.setAttribute('role', 'option');
        item.dataset.value = opt.value;
        item.innerHTML = '<span>' + escapeHtml(opt.textContent) + '</span>' + CHECK;
        item.addEventListener('click', function (e) {
          e.stopPropagation();
          pick(opt.value);
        });
        menu.appendChild(item);
      });
    }

    function syncDisplay() {
      var opt = select.options[select.selectedIndex];
      valueEl.textContent = opt ? opt.textContent : '';
      valueEl.classList.toggle('is-placeholder', !select.value);
      menu.querySelectorAll('.fancy-select__item').forEach(function (it) {
        var sel = it.dataset.value === select.value;
        it.classList.toggle('is-selected', sel);
        it.setAttribute('aria-selected', sel ? 'true' : 'false');
      });
    }

    function pick(value) {
      if (select.value !== value) {
        select.value = value;
        select.dispatchEvent(new Event('change', { bubbles: true }));
      }
      syncDisplay();
      closeMenu();
      trigger.focus();
    }

    function openMenu() {
      closeAll(wrap);
      wrap.classList.add('is-open');
      trigger.setAttribute('aria-expanded', 'true');
      openWrap = wrap;
    }
    function closeMenu() {
      wrap.classList.remove('is-open');
      trigger.setAttribute('aria-expanded', 'false');
      if (openWrap === wrap) openWrap = null;
    }

    trigger.addEventListener('click', function (e) {
      e.stopPropagation();
      if (trigger.disabled) return;
      wrap.classList.contains('is-open') ? closeMenu() : openMenu();
    });

    trigger.addEventListener('keydown', function (e) {
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        if (!wrap.classList.contains('is-open')) { openMenu(); return; }
      }
      if (e.key === 'Escape') closeMenu();
      // navegación por flechas dentro del menú abierto
      if (wrap.classList.contains('is-open') && (e.key === 'ArrowDown' || e.key === 'ArrowUp')) {
        var idx = select.selectedIndex;
        idx += (e.key === 'ArrowDown') ? 1 : -1;
        if (idx < 0) idx = 0;
        if (idx > select.options.length - 1) idx = select.options.length - 1;
        pickPreview(idx);
      }
    });

    function pickPreview(idx) {
      // resaltar sin cerrar (preview con flechas), confirmando con Enter al re-pulsar
      var opt = select.options[idx];
      if (opt) pick(opt.value);
    }

    // refresca el display cuando el <select> cambia por código (prefill de modales)
    select.addEventListener('change', syncDisplay);

    buildItems();
    syncDisplay();
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  function enhanceAll(root) {
    (root || document).querySelectorAll('select').forEach(enhance);
  }

  document.addEventListener('click', function () { closeAll(null); });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeAll(null);
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () { enhanceAll(); });
  } else {
    enhanceAll();
  }

  // expuesto por si se inyectan selects dinámicamente
  window.enhanceFancySelects = enhanceAll;
})();
