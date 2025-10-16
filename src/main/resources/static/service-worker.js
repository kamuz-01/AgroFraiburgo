// Alterar o nome do cache para forçar atualização
const CACHE_NAME = 'agrofraiburgo-cache-v6'; // incrementado para v6

const URLS_TO_CACHE = [
  '/',
  '/pagina_inicial.html',
  '/criar_conta.html',
  '/login.html',
  '/produtos.html',
  '/sobre.html',
  '/offline.html',
  '/manifest.json',
  '/imagens/logo-192x192.png',
  '/imagens/logo-512x512.png',
  '/imagens/favicon.png',
  '/css/estilos-login.css',
  '/css/estilos-criar_conta.css',
  '/css/estilos-pagina_inicial.css',
  '/js/validacoes-criar_conta.js',
  '/js/MenuResponsivo.js',
  '/js/script.js'
];

// Rotas que exigem autenticação (suporte a .html e sem .html)
const AUTHENTICATED_PATHS = [
  '/home_consumidor',
  '/home_consumidor.html',
  '/home_produtor',
  '/home_produtor.html',
  '/home_moderador'
];

// Instala o service worker e adiciona arquivos no cache
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
		return Promise.all(
		  URLS_TO_CACHE.map(url =>
		    fetch(url, { credentials: 'omit' }).then(response => {
		      if (response.ok) {
		        return cache.put(url, response);
		      }
		      console.error('Falha cachear:', url, response.status);
		      return Promise.resolve();
		    }).catch(err => {
		      console.error('Erro no fetch:', url, err);
		      return Promise.resolve();
		    })
		  )
		);
    })
  );
  self.skipWaiting();
});

// Ativa o service worker e remove caches antigos
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames
          .filter(name => name !== CACHE_NAME)
          .map(name => caches.delete(name))
      );
    })
  );
  self.clients.claim();
});

// Intercepta requisições e serve do cache quando possível
self.addEventListener('fetch', event => {
  const { request } = event;
  const url = new URL(request.url);

  if (request.method !== 'GET') {
    event.respondWith(fetch(request));
    return;
  }

  if (AUTHENTICATED_PATHS.includes(url.pathname)) {
    // Estratégia network-first com fallback para cache
    event.respondWith(
      fetch(request, { credentials: 'include' })
        .then(networkResponse => {
          // Se resposta ok, salva no cache para uso offline depois
          if (networkResponse && networkResponse.status === 200) {
            const clone = networkResponse.clone();
            caches.open(CACHE_NAME).then(cache => cache.put(request, clone));
          }
          return networkResponse;
        })
        .catch(() => {
          // Offline: tenta a versão salva no cache
          return caches.match(request)
            .then(cachedResponse => cachedResponse || caches.match('/offline.html'));
        })
    );
  } else {
    // Estratégia cache-first para páginas públicas
    event.respondWith(
      caches.match(request)
        .then(cachedResponse => {
          return cachedResponse || fetch(request, { credentials: 'omit' })
            .then(networkResponse => {
              if (networkResponse && networkResponse.status === 200) {
                return caches.open(CACHE_NAME).then(cache => {
                  cache.put(request, networkResponse.clone());
                  return networkResponse;
                });
              }
              return networkResponse;
            });
        })
        .catch(() => caches.match('/offline.html'))
    );
  }
});