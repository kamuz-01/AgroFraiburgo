# Changelog

Todas as mudanças relevantes deste projeto serão registradas aqui.

## [v1.0.0-beta.1] - 2026-04-16

### Adicionado

- Monitoramento dedicado de requisições HTTP com log próprio para acesso.
- Página de perfil unificada para consumidor, produtor e moderador.
- Fluxo de recuperação de senha com envio de e-mail e redefinição por token.
- Busca funcional para produtos e produtores.
- Listagem de produtores com paginação real e carregamento incremental.
- Página de detalhes da feira com status `EM_ANDAMENTO` e integração com Google Maps.
- Tratamento amigável para erros do servidor e fallback offline.
- Recomendações híbridas com Neo4j e fallbacks quando o grafo não está disponível.
- Funcionalidade de favoritar produtor.

### Melhorado

- Validação de upload mais rígida para arquivos enviados.
- Ajustes de segurança no fluxo de autenticação e sessão.
- Correções de consistência em favoritos, avaliações e navegação entre perfis.

## Próximas versões beta

Use este modelo para as próximas releases:

### [vX.Y.Z-beta.N] - YYYY-MM-DD

### Adicionado
- 

### Alterado
- 

### Corrigido
- 

### Observações
- 

## Boas práticas de versão

- Use tags anotadas, por exemplo: `v1.0.0-beta.1`.
- Publique a tag antes de criar a release no GitHub.
- Marque a release como `pre-release` enquanto a versão ainda estiver em beta.