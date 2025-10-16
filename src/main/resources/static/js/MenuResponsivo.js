document.addEventListener('DOMContentLoaded', function () {
  const btnMobile = document.getElementById('btn-mobile');

  function toggleMenu(event) {
    const nav = document.getElementById('nav');
    nav.classList.toggle('active');
    const active = nav.classList.contains('active');
    event.currentTarget.setAttribute('aria-expanded', active);
    event.currentTarget.setAttribute('aria-label', active ? 'Fechar Menu' : 'Abrir Menu');
  }

  btnMobile.addEventListener('click', toggleMenu);
});

window.addEventListener('pageshow', () => {
  setTimeout(() => {
    const nav = document.getElementById('nav');
    const btnMobile = document.getElementById('btn-mobile');

    if (nav) {
      nav.classList.remove('active');
    }
    if (btnMobile) {
      btnMobile.setAttribute('aria-expanded', 'false');
      btnMobile.setAttribute('aria-label', 'Abrir Menu');
    }
  }, 0); // 0 milissegundos = 0 segundos, executa o mais rápido possível após o evento
});