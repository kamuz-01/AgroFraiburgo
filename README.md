# AgroFraiburgo 🌱

## Sobre o Projeto 👨🏻‍💻

&nbsp;&nbsp;&nbsp;&nbsp;O AgroFraiburgo é um sistema web de informação e vitrine digital para a agricultura familiar de Fraiburgo/SC. A proposta é simples: centralizar informações sobre produtores locais, seus produtos e as feiras em andamento, tornando mais fácil para qualquer pessoa da cidade descobrir o que está sendo produzido e por quem.

&nbsp;&nbsp;&nbsp;&nbsp;Este projeto integra o Trabalho de Conclusão de Curso (TCC) para obtenção do diploma de Tecnólogo em Análise e Desenvolvimento de Sistemas pelo Instituto Federal Catarinense — Campus Fraiburgo.

&nbsp;&nbsp;&nbsp;&nbsp;O sistema está funcional como protótipo. Algumas funcionalidades planejadas ainda não foram implementadas (veja a seção [O que ainda não está pronto](#o-que-ainda-não-está-pronto)). O repositório está aberto caso alguém queira evoluir o projeto para uso real.

---

## O que o sistema faz

- 🧑‍🌾 **Vitrine de produtores** — perfil público com foto de capa, foto de perfil, localização e lista de produtos
- 📦 **Catálogo de produtos** — cadastro, gestão de estoque e status de disponibilidade pelo próprio produtor
- 🗺️ **Detalhes da feira** — exibe endereço e localização no Google Maps da feira com status `EM_ANDAMENTO`
- 🔎 **Busca funcional** — pesquisa por nome de produto, produtor, cidade ou estado, com filtro por faixa de preço
- ⭐ **Avaliações** — consumidores avaliam produtores com nota e comentário
- 🔖 **Favoritar produtor** — salvar produtor na conta (persistência relacional + grafo Neo4j, quando habilitado)
- 🧠 **Recomendações híbridas** — sugestões de produtos baseadas em favoritos e visualizações via Neo4j; com fallback por produtores mais bem avaliados quando o Neo4j não está disponível
- 📄 **Moderação de produtores** — análise e aprovação de cadastros com download de documentos
- 👥 **Gestão de usuários** — moderadores podem alterar status de contas
- 🪪 **Perfil unificado** — edição de dados cadastrais, foto de perfil e imagem de capa para todos os tipos de usuário
- 🔑 **Recuperação de senha** — fluxo por e-mail com token temporário
- 🔒 **Autenticação robusta** — login local com proteção contra força bruta (bloqueio progressivo 12h → 24h → definitivo), rate limiting por IP, rotação automática de chaves JWT e login via Google e Facebook (OAuth2)
- 🧾 **Logs de acesso HTTP** — arquivo dedicado `api-access.log` com IP, método, URI, status e tempo de resposta
- 📱 **Interface responsiva** — navegação adaptada para mobile, incluindo dock de ações na parte inferior da tela

---

## O que ainda não está pronto

Estas funcionalidades foram planejadas no escopo original mas não foram implementadas nesta versão:

- Contato direto entre consumidor e produtor dentro da plataforma (o sistema exibe e-mail e telefone, mas não há chat ou formulário interno)
- Carrinho de compras, pedidos ou qualquer fluxo de pagamento
- Painel de estatísticas para o moderador
- Exportação de relatórios
- Suporte a múltiplas feiras simultâneas (hoje apenas uma feira `EM_ANDAMENTO` é exibida por vez)

---

## Tecnologias Utilizadas

- Java 21 e Spring Boot 3.x
- Maven 3.x
- Spring Data JPA + MySQL 8.x
- Spring Security + JWT (com rotação automática de chaves)
- OAuth2 (Google e Facebook)
- Neo4j (grafo de recomendações — opcional)
- Spring Data Neo4j
- RabbitMQ (envio assíncrono de e-mails)
- Thymeleaf + Thymeleaf Extras Security
- Flyway (migrations incrementais)
- Logback
- Axios, Bootstrap, HTML5, CSS3, JavaScript
- JUnit 5 + Mockito (testes unitários)

---

## Requisitos

| Dependência | Versão mínima |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.x |
| RabbitMQ | 3.x |
| Neo4j | 5.x (opcional) |

Navegador moderno com suporte a HTML5 para renderização das páginas Thymeleaf.

---

## Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/kamuz-01/AgroFraiburgo.git
cd AgroFraiburgo
```

### 2. Instale as dependências

```bash
mvn install
```

### 3. Importe o banco de dados

Execute o arquivo `agrofraiburgo.sql` no seu MySQL para criar o schema e os dados iniciais:

```bash
mysql -u seu_usuario -p < agrofraiburgo.sql
```

### 4. Configure o arquivo `.env`

Crie um arquivo `.env` na raiz do projeto. O `.env` não deve ser versionado (já está no `.gitignore`).

```properties
# Banco de dados
DB_URL=jdbc:mysql://localhost:3306/agrofraiburgo
Driver_Class_Name=com.mysql.cj.jdbc.Driver
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

# JWT
JWT_SECRET=uma_string_base64_de_pelo_menos_32_bytes
JWT_ACCESS_TOKEN_TTL_SECONDS=604800
JWT_ROTATION_INTERVAL_MS=86400000
JWT_ISSUER=http://localhost:8080

# OAuth2 — Google
GOOGLE_CLIENT_ID=seu_client_id
GOOGLE_CLIENT_SECRET=seu_client_secret

# OAuth2 — Facebook
FACEBOOK_CLIENT_ID=seu_app_id
FACEBOOK_CLIENT_SECRET=seu_app_secret

# RabbitMQ
Rabbitmq_Port=5672
Rabbitmq_Username=guest
Rabbitmq_Password=guest
Rabbitmq_Fila_Bloqueios=fila_bloqueios

# E-mail (SMTP)
Spring_Hostname=smtp.seudominio.com
Spring_Mail_port=587
Spring_Mail_Username=seu@email.com
Spring_Mail_Senha=sua_senha_smtp
Spring_Mail_From=no-reply@seudominio.com
Spring_Mail_SMTP=smtp.seudominio.com

# Neo4j (opcional — recomendações por grafo)
# NEO4J_ENABLED=true
# NEO4J_URI=bolt://localhost:7687
# NEO4J_USERNAME=neo4j
# NEO4J_PASSWORD=sua_senha
# NEO4J_DATABASE=neo4j
```

Se não tiver Neo4j configurado, deixe `NEO4J_ENABLED` como `false` (ou simplesmente comente as variáveis). O sistema funcionará normalmente com fallback por produtores mais bem avaliados.

### 5. Execute o projeto

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## Usuários de exemplo

O dump SQL inclui dados de exemplo para testar cada perfil:

| Login | Tipo | Status |
|---|---|---|
| `pedro` | Consumidor | Ativo |
| `antonia` | Produtor | Ativo |
| `xavier` | Moderador | Ativo |

Todas as senhas seguem o padrão definido no dump. Consulte o arquivo `agrofraiburgo.sql` para os valores exatos (estão com hash bcrypt; para testes locais, crie um usuário novo pelo formulário de cadastro).

---

## Neo4j — Recomendações por grafo (opcional)

O sistema usa Neo4j para montar recomendações personalizadas com base em favoritos e visualizações. Se o Neo4j não estiver habilitado, o fallback são os produtos dos produtores com melhor avaliação.

### Exportar dados do MySQL para o Neo4j

```bash
# A partir do dump SQL (sem precisar do MySQL rodando)
python neo4j/export_from_sql.py
```

Isso gera os arquivos em `neo4j-export/`: `users.csv`, `products.csv`, `made_by.csv`, `favorites.csv` e `import.cypher`.

### Importar no Neo4j Desktop

1. Copie os CSVs para a pasta `import` do seu banco no Neo4j Desktop
2. Abra o Neo4j Browser e execute o conteúdo de `import.cypher`

Documentação detalhada, troubleshooting e queries de validação: **`neo4j/README.md`**.

---

## Logs

A aplicação gera três arquivos em `logs/`:

| Arquivo | Conteúdo |
|---|---|
| `app.log` | Log geral da aplicação |
| `error.log` | Somente erros (nível ERROR) |
| `api-access.log` | Requisições HTTP: IP, método, URI, status e tempo de resposta |

Os arquivos são rotacionados diariamente e mantidos por 30 dias.

---

## Estrutura de Diretórios

```text
AgroFraiburgo/
├── src/
│   ├── main/
│   │   ├── java/         # Código-fonte Java
│   │   └── resources/    # Configurações, templates e arquivos estáticos
│   └── test/             # Testes unitários
├── neo4j/                # Scripts de exportação e documentação do grafo
├── neo4j-export/         # CSVs e Cypher gerados para importar no Neo4j
├── documentos-produtores/# Documentos enviados no cadastro de produtores
├── imagens-usuarios/     # Fotos de perfil e capa dos usuários
├── logs/                 # Logs da aplicação (gerados em runtime)
├── agrofraiburgo.sql     # Dump do banco com schema e dados de exemplo
├── pom.xml
└── README.md
```

---

## Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie sua branch (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Add MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## Changelog

Veja as notas de versão em [CHANGELOG.md](CHANGELOG.md).

---

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

## Contato

- 🧑🏽‍💻 **Karli De Jesus Munoz Manzano**
- 📧 **E-mail**: [karli.manzano@estudantes.ifc.edu.br](mailto:karli.manzano@estudantes.ifc.edu.br)

---

<div align="center">
  Desenvolvido com ❤️ para a agricultura familiar de Fraiburgo

  <strong><em>Todos os direitos reservados © 2026</em></strong>
</div>