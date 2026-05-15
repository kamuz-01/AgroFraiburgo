(function () {
  function getCookie(name) {
    return document.cookie
      .split('; ')
      .map((part) => part.split('='))
      .find(([key]) => key === name)?.[1];
  }

  window.csrfHeaders = function (headers) {
    const token = getCookie('XSRF-TOKEN');
    return {
      ...(headers || {}),
      ...(token ? { 'X-XSRF-TOKEN': decodeURIComponent(token) } : {})
    };
  };

  if (window.axios) {
    window.axios.defaults.xsrfCookieName = 'XSRF-TOKEN';
    window.axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';
  }

  document.addEventListener('click', async function (event) {
    const link = event.target.closest('a[href="/logout"], a[href="/api/auth/logout"], #sair, .logout-link');
    if (!link) return;

    event.preventDefault();

    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'same-origin',
        headers: window.csrfHeaders()
      });
    } finally {
      window.location.href = '/login.html';
    }
  });
})();
