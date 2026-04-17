# AgroFraiburgo 🌱

## Sobre o Projeto 👨🏻‍💻

&nbsp;&nbsp;&nbsp;&nbsp;O aplicativo web AgroFraiburgo é um protótipo de aplicativo para disponibilizar informações sobre as feiras de alimentos produzidos pela agricultura familiar na cidade de Fraiburgo/SC.

&nbsp;&nbsp;&nbsp;&nbsp;Esse aplicativo faz parte da temática do meu Trabalho de Conclusão de Curso (TCC) para a obtenção do diploma como Tecnólogo en Análise e Desenvolvimento de sistemas Pelo Instituto Federal Catarinense - Campus Fraiburgo.

&nbsp;&nbsp;&nbsp;&nbsp;Atualmente foram implementadas algumas das funcionalidades propostas, motivo pelo qual disponibilizo esse repositório, caso alguém tenha interesse em concluir todas as funcionalidades propostas, de forma que possa futuramente ser disponibilizado para o público-alvo.

## Funcionalidades Principais

- 📄 Revisão/moderação da documentação dos produtores
- 👤 Gestão e moderação de usuários
- 🔒 Sistema de autenticação (login e OAuth2)
- 🧾 Monitoramento de requisições e logs de aplicação
- 🧑‍🌾 Perfis de produtores e catálogo de produtos
- 🪪 Página de perfil unificada para todos os tipos de usuário
- 🔑 Recuperação de senha com envio de e-mail
- 🔎 Busca funcional para produtos e produtores
- 🗺️ Página de detalhes da feira com mapa do Google Maps
- ⭐ Avaliação de produtores
- 🔖 Favoritar produtor ("Salvar produtor")
- 📃 Listagem de produtores com paginação real e carregamento incremental
- 🧭 Tratamento amigável para erros do servidor e modo offline
- 🧠 Recomendações personalizadas (híbridas) com grafo no Neo4j
- 📱 Interface responsiva

## Atualizações Recentes

### Monitoramento de requisições e logs

Foi adicionada uma camada de **monitoramento de acesso HTTP** com log dedicado para requisições, além dos logs gerais e de erro da aplicação. O projeto agora registra informações como IP, método, URI, status e tempo de resposta em arquivos próprios dentro de `logs/`.

### Correções de consistência no backend

Foram corrigidas inconsistências que afetavam a experiência de uso e a confiabilidade dos dados, como a contagem de favoritos e avaliações, a persistência de `avaliacoes_recebidas` e o fluxo de navegação do perfil entre os diferentes tipos de usuário.

### Perfil unificado por tipo de usuário

Foi criada uma página de **perfil unificada** para consumidor, produtor e moderador, permitindo visualizar e editar dados básicos de cadastro em um único fluxo.

### Recuperação de senha

Foi implementado o fluxo de **recuperação de senha** com envio de e-mail, token temporário e página de redefinição.

### Busca funcional e listagem aprimorada

A busca da vitrine passou a ser **funcional** para produtos e produtores, e a listagem de produtores ganhou filtros reais, paginação incremental e preservação do termo pesquisado ao carregar mais itens.

### Feira em andamento com mapa

Foi adicionada uma página de **detalhes da feira** que exibe a feira com status `EM_ANDAMENTO`, mostrando o endereço cadastrado e a localização no Google Maps.

### Erros e modo offline

O projeto passou a contar com uma página de erro mais amigável para falhas do servidor, distinguindo cenários como 404, 500 e 503, além de uma tela de fallback para o modo offline.

### Segurança e uploads

O fluxo de upload foi endurecido para validar o conteúdo real dos arquivos enviados, e o sistema de autenticação recebeu ajustes de segurança relacionados ao JWT e ao tratamento de sessões com cookie.

### Recomendações por grafos (Neo4j)

Foi adicionada uma camada de **recomendação híbrida** usando Neo4j como grafo de interações. O app tenta montar recomendações personalizadas via Neo4j e, se o Neo4j estiver desabilitado/indisponível, aplica fallbacks (ex.: melhores produtores avaliados e/ou produtos recentes).

Sinais/relacionamentos no grafo (exemplos):

- `(:User)-[:FAVORITED]->(:Product)`
- `(:User)-[:VIEWED]->(:Product)`
- `(:User)-[:FAVORITED_PRODUCER]->(:Producer)` (favoritar produtor / "Salvar produtor")
- `(:Product)-[:MADE_BY]->(:Producer)`

Documentação detalhada (configuração, export/import, troubleshooting): **`neo4j/README.md`**.

### Favoritar produtor ("Salvar produtor")

Foi introduzida a funcionalidade de **salvar/favoritar produtores** a partir do perfil do produtor.

- Persistência relacional via tabela `favoritos_produtores` (MySQL)
- Espelhamento do relacionamento no Neo4j (`FAVORITED_PRODUCER`) quando habilitado

## Tecnologias Utilizadas

- Java e Spring Boot
- Maven
- Spring Data JPA
- Base de Dados MySQL
- Neo4j (grafo de recomendações)
- Spring Data Neo4j
- Spring Security
- Thymeleaf e Thymeleaf Extras para Security
- JWT
- Jbcrypt
- RabbitMQ
- Logback
- Spring Dev Tools
- Axios
- HTML5
- CSS3
- Bootstrap
- Javascript

## Requisitos

- Java 21 (recomendado; ver propriedade `java.version` no `pom.xml`)
- Spring Boot 3.x ou superior
- Maven 3.x
- IDE compatível (recomendado VS Code ou Spring Tool Suite)
- Ambiente de banco de dados MySQL configurado, com uma ferramenta de gerenciamento/execução como MySQL Workbench, XAMPP, WAMP ou LAMP.
- Neo4j instalado localmente ou uma conta criada no Neo4j Aura, caso deseje usar as recomendações por grafo.
- RabbitMQ devidamente instalado e em execução no ambiente local.
- Navegador moderno com suporte a Thymeleaf/HTML5, para exibição das páginas de perfil, feira, erro e fallback offline.
- O diretório `logs/` será utilizado pela aplicação para registrar `app.log`, `error.log` e `api-access.log`.

## Instalação

### 1. Clone o repositório

```bash
git clone https://seu-repositorio/AgroFraiburgo.git
cd AgroFraiburgo
```

### 2. Instale as dependências

```bash
mvn install
```

### 3. Configure o arquivo `.env`

Crie um arquivo `.env` na raiz do projeto com suas credenciais:

```properties
DB_URL=sua_url_do_banco
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

# (Opcional) Neo4j — recomendações por grafo
# NEO4J_ENABLED=true
# NEO4J_URI=neo4j+s://<id>.databases.neo4j.io
# NEO4J_USERNAME=<usuario>
# NEO4J_PASSWORD=<senha>
# NEO4J_DATABASE=<database>
```

As páginas públicas principais já incluem os fluxos de perfil, recuperação de senha, busca funcional, listagem de produtores com paginação real, detalhes da feira e tratamento amigável para erros do servidor.

Os logs de monitoramento também passam a ser gerados automaticamente em `logs/`, facilitando a inspeção de erros e do tráfego HTTP.

Obs.: o `.env` está no `.gitignore` e não deve ser versionado.

### 4. Execute o projeto

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## Estrutura de Diretórios

```text
AgroFraiburgo/
├── src/
│   ├── main/
│   │   ├── java/         # Código fonte Java
│   │   └── resources/    # Arquivos de configuração
│   └── test/             # Testes automatizados
├── documentos-produtores/  # Armazenamento de documentos
├── imagens-usuarios/       # Armazenamento de imagens
├── pom.xml               # Configuração Maven
└── README.md             # Este arquivo
```

## Contribuição

Contributions são bem-vindas! Para contribuir com o projeto, siga os passos abaixo:

1. Faça um Fork do projeto
2. Crie sua Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## Changelog

Veja as notas de versão em [CHANGELOG.md](CHANGELOG.md).

## Contato

- 🧑🏽‍💻 **Karli De Jesus Munoz Manzano**
- 📧 **Email**: [karli.manzano@estudantes.ifc.edu.br](mailto:karli.manzano@estudantes.ifc.edu.br)

---

<div align="center">
  Desenvolvido com ❤️ para a agricultura familiar de Fraiburgo

  <strong><em>Todos os direitos reservados © 2026</em></strong>
</div>