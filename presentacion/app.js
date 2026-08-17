(() => {
  'use strict';

  const state = {
    index: 0,
    slides: Array.from(document.querySelectorAll('.slide')),
    tocOpen: false,
    lastFocused: null,
    touchStart: null,
  };

  const elements = {
    deck: document.getElementById('deck'),
    current: document.getElementById('current-slide'),
    total: document.getElementById('total-slides'),
    progress: document.getElementById('progress-bar'),
    previous: document.getElementById('prev-button'),
    next: document.getElementById('next-button'),
    toc: document.getElementById('toc'),
    tocClose: document.querySelector('#toc button[data-action="close-toc"]'),
    tocList: document.getElementById('toc-list'),
    announcer: document.getElementById('slide-announcer'),
    hint: document.getElementById('keyboard-hint'),
    glossarySearch: document.getElementById('glossary-search'),
    glossaryGrid: document.getElementById('glossary-grid'),
    glossaryEmpty: document.getElementById('glossary-empty'),
    quizProgress: document.getElementById('quiz-progress'),
    jwtDetail: document.getElementById('jwt-detail'),
  };

  const JWT_DETAILS = {
    header: {
      title: 'Header',
      text: 'Describe el tipo de token y el algoritmo de firma.',
    },
    payload: {
      title: 'Payload',
      text: 'Contiene los claims sub, iat y exp. Puede decodificarse; no es un espacio para secretos.',
    },
    signature: {
      title: 'Signature',
      text: 'Permite verificar integridad y emisor con la clave del backend. No cifra el payload.',
    },
  };

  function pad(value) {
    return String(value).padStart(2, '0');
  }

  function validIndex(value) {
    return Math.max(0, Math.min(value, state.slides.length - 1));
  }

  function indexFromHash() {
    const id = window.location.hash.slice(1);
    const found = state.slides.findIndex((slide) => slide.id === id);
    return found >= 0 ? found : 0;
  }

  function syncHash(slide, historyMode) {
    const nextHash = `#${slide.id}`;
    if (window.location.hash === nextHash) return;

    if (historyMode === 'replace') {
      window.history.replaceState(null, '', nextHash);
    } else if (historyMode === 'push') {
      window.history.pushState(null, '', nextHash);
    }
  }

  function goTo(index, options = {}) {
    const nextIndex = validIndex(index);
    const previousSlide = state.slides[state.index];
    const nextSlide = state.slides[nextIndex];

    if (previousSlide !== nextSlide) {
      previousSlide?.classList.remove('is-active');
      previousSlide?.setAttribute('aria-hidden', 'true');
      nextSlide.classList.add('is-active');
      nextSlide.removeAttribute('aria-hidden');
      nextSlide.scrollTop = 0;
    }

    state.index = nextIndex;
    elements.current.textContent = pad(nextIndex + 1);
    elements.total.textContent = pad(state.slides.length);
    elements.progress.style.width = `${((nextIndex + 1) / state.slides.length) * 100}%`;
    elements.previous.disabled = nextIndex === 0;
    elements.next.disabled = nextIndex === state.slides.length - 1;

    document.querySelectorAll('#toc-list a').forEach((link, itemIndex) => {
      if (itemIndex === nextIndex) link.setAttribute('aria-current', 'true');
      else link.removeAttribute('aria-current');
    });

    const title = nextSlide.dataset.title || nextSlide.querySelector('h1, h2')?.textContent.trim();
    elements.announcer.textContent = `Diapositiva ${nextIndex + 1} de ${state.slides.length}: ${title}`;
    document.title = `${pad(nextIndex + 1)} · ${title} · Car Manager`;

    syncHash(nextSlide, options.historyMode || 'push');

    if (options.focusSlide) {
      nextSlide.focus({ preventScroll: true });
    }
  }

  function next() {
    if (state.index < state.slides.length - 1) goTo(state.index + 1);
  }

  function previous() {
    if (state.index > 0) goTo(state.index - 1);
  }

  function buildToc() {
    const fragment = document.createDocumentFragment();
    state.slides.forEach((slide, index) => {
      const link = document.createElement('a');
      link.href = `#${slide.id}`;
      link.dataset.slideIndex = String(index);
      link.innerHTML = `<span>${pad(index + 1)}</span><strong>${slide.dataset.title}</strong>`;
      fragment.appendChild(link);
    });
    elements.tocList.appendChild(fragment);
  }

  function focusableInsideToc() {
    return Array.from(elements.toc.querySelectorAll('button:not([disabled]), a[href]'));
  }

  function openToc(trigger) {
    if (state.tocOpen) return;
    state.tocOpen = true;
    state.lastFocused = trigger || document.activeElement;
    elements.toc.hidden = false;
    elements.deck.inert = true;
    document.querySelector('.deck-header').inert = true;
    document.querySelector('.deck-controls').inert = true;
    elements.tocClose.focus({ preventScroll: true });
  }

  function closeToc() {
    if (!state.tocOpen) return;
    state.tocOpen = false;
    elements.toc.hidden = true;
    elements.deck.inert = false;
    document.querySelector('.deck-header').inert = false;
    document.querySelector('.deck-controls').inert = false;
    state.lastFocused?.focus?.();
  }

  function dismissHint() {
    elements.hint.hidden = true;
    try {
      window.localStorage.setItem('car-manager.deck-hint-dismissed', 'true');
    } catch {
      // The presentation remains functional when browser storage is unavailable.
    }
  }

  function restoreHintPreference() {
    try {
      elements.hint.hidden = window.localStorage.getItem('car-manager.deck-hint-dismissed') === 'true';
    } catch {
      elements.hint.hidden = false;
    }
  }

  function isInteractiveTarget(target) {
    return target instanceof Element && Boolean(target.closest('button, a, input, textarea, select, [contenteditable="true"]'));
  }

  function handleKeyboard(event) {
    if (event.key === 'Escape') {
      if (state.tocOpen) {
        event.preventDefault();
        closeToc();
      }
      return;
    }

    if (state.tocOpen) {
      if (event.key === 'Tab') {
        const focusable = focusableInsideToc();
        const first = focusable[0];
        const last = focusable[focusable.length - 1];
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault();
          last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      }
      return;
    }

    if (isInteractiveTarget(event.target)) return;

    const actions = {
      ArrowRight: next,
      PageDown: next,
      ' ': next,
      ArrowLeft: previous,
      PageUp: previous,
      Home: () => goTo(0),
      End: () => goTo(state.slides.length - 1),
    };

    const action = actions[event.key];
    if (action) {
      event.preventDefault();
      action();
    }
  }

  function handleActions(event) {
    const actionTarget = event.target.closest('[data-action]');
    if (actionTarget) {
      const actions = {
        next,
        previous,
        'open-toc': () => openToc(actionTarget),
        'close-toc': closeToc,
        'dismiss-hint': dismissHint,
      };
      actions[actionTarget.dataset.action]?.();
      return;
    }

    const tocLink = event.target.closest('[data-slide-index]');
    if (tocLink) {
      event.preventDefault();
      closeToc();
      goTo(Number(tocLink.dataset.slideIndex), { focusSlide: true });
      return;
    }

    const quizButton = event.target.closest('.quiz-item > button');
    if (quizButton) {
      const item = quizButton.closest('.quiz-item');
      const answer = item.querySelector('.quiz-answer');
      const expanded = quizButton.getAttribute('aria-expanded') === 'true';
      quizButton.setAttribute('aria-expanded', String(!expanded));
      answer.hidden = expanded;
      item.classList.toggle('is-open', !expanded);
      updateQuizProgress();
      return;
    }

    const jwtPart = event.target.closest('[data-jwt]');
    if (jwtPart) {
      const key = jwtPart.dataset.jwt;
      const detail = JWT_DETAILS[key];
      document.querySelectorAll('[data-jwt]').forEach((part) => {
        part.setAttribute('aria-pressed', String(part === jwtPart));
      });
      elements.jwtDetail.innerHTML = `<strong>${detail.title}</strong><p>${detail.text}</p>`;
    }
  }

  function updateQuizProgress() {
    const revealed = document.querySelectorAll('.quiz-item > button[aria-expanded="true"]').length;
    elements.quizProgress.textContent = `${revealed} de 5 respuestas reveladas`;
  }

  function normalize(value) {
    return value.toLocaleLowerCase('es').normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
  }

  function filterGlossary() {
    const term = normalize(elements.glossarySearch.value);
    let visible = 0;
    elements.glossaryGrid.querySelectorAll('article').forEach((card) => {
      const haystack = normalize(`${card.dataset.terms} ${card.textContent}`);
      const matches = !term || haystack.includes(term);
      card.hidden = !matches;
      if (matches) visible += 1;
    });
    elements.glossaryEmpty.hidden = visible !== 0;
  }

  function handleTouchStart(event) {
    if (event.touches.length !== 1 || state.tocOpen) return;
    const touch = event.touches[0];
    state.touchStart = { x: touch.clientX, y: touch.clientY, time: Date.now() };
  }

  function handleTouchEnd(event) {
    if (!state.touchStart || event.changedTouches.length !== 1 || state.tocOpen) return;
    const touch = event.changedTouches[0];
    const dx = touch.clientX - state.touchStart.x;
    const dy = touch.clientY - state.touchStart.y;
    const elapsed = Date.now() - state.touchStart.time;
    state.touchStart = null;

    const horizontalIntent = Math.abs(dx) > 58 && Math.abs(dx) > Math.abs(dy) * 1.35;
    if (!horizontalIntent || elapsed > 800) return;
    if (dx < 0) next();
    else previous();
  }

  function handleHistoryNavigation() {
    goTo(indexFromHash(), { historyMode: 'none' });
  }

  function initialize() {
    if (!state.slides.length) return;

    state.slides.forEach((slide, index) => {
      slide.classList.toggle('is-active', index === 0);
      if (index !== 0) slide.setAttribute('aria-hidden', 'true');
    });

    buildToc();
    restoreHintPreference();
    goTo(indexFromHash(), { historyMode: 'replace' });

    document.addEventListener('click', handleActions);
    document.addEventListener('keydown', handleKeyboard);
    window.addEventListener('popstate', handleHistoryNavigation);
    window.addEventListener('hashchange', handleHistoryNavigation);
    elements.deck.addEventListener('touchstart', handleTouchStart, { passive: true });
    elements.deck.addEventListener('touchend', handleTouchEnd, { passive: true });
    elements.glossarySearch.addEventListener('input', filterGlossary);
  }

  initialize();
})();
