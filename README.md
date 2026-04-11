# AgroFraiburgo 🌱

## Sobre o Projeto 👨🏻‍💻

&nbsp;&nbsp;&nbsp;&nbsp;O aplicativo web AgroFraiburgo é um protótipo de aplicativo para disponibilizar informações sobre as feiras de alimentos produzidos pela agricultura familiar na cidade de Fraiburgo/SC.

&nbsp;&nbsp;&nbsp;&nbsp;Esse aplicativo faz parte da temática do meu Trabalho de Conclusão de Curso (TCC) para a obtenção do diploma como Tecnólogo en Análise e Desenvolvimento de sistemas Pelo Instituto Federal Catarinense - Campus Fraiburgo.

&nbsp;&nbsp;&nbsp;&nbsp;Atualmente foram implementadas algumas das funcionalidades propostas, motivo pelo qual disponibilizo esse repositório, caso alguém tenha interesse em concluir todas as funcionalidades propostas, de forma que possa futuramente ser disponibilizado para o público-alvo.

## Funcionalidades Principais

- 📄 Revisão/moderação da documentação dos produtores
- 👤 Gestão e moderação de usuários
- 🔒 Sistema de autenticação (login e OAuth2)
- 🧑‍🌾 Perfis de produtores e catálogo de produtos
- ⭐ Avaliação de produtores
- 🔖 Favoritar produtor ("Salvar produtor")
- 🧠 Recomendações personalizadas (híbridas) com grafo no Neo4j
- 📱 Interface responsiva

## Novidades (Abr/2026)

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

- Java Spring Boot
- Maven
- Base de Dados MySQL
- Neo4j (grafo de recomendações)
- Spring Data Neo4j
- Spring Security
- Jbcrypt
- RabbitMQ
- Thymeleaf
- Spring Dev Tools.
- Axios
- HMTL5
- CSS3
- Bootstrap
- Javascript

## Requisitos

- Java 21 (recomendado; ver propriedade `java.version` no `pom.xml`)
- SpringBoot 3.x ou superior
- Maven 3.x
- IDE compatível (recomendado VS Code ou Spring Tool Suite)
- **Ambiente de banco de dados MySQL configurado, sendo necessário ter uma das seguintes ferramentas de gerenciamento e execução do MySQL**:
- MySQL Workbench
- XAMPP
- WAMP ou
- LAMP
- NEO4J instalado localmente ou uma conta criada no NEO4J Aura.
- RabbitMQ devidamente instalado e em execução no ambiente local.

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

## Contato

- 🧑🏽‍💻 **Karli De Jesus Munoz Manzano**
- 📧 **Email**: [karli.manzano@estudantes.ifc.edu.br](mailto:karli.manzano@estudantes.ifc.edu.br)

---

<div align="center">
  Desenvolvido com ❤️ para a agricultura familiar de Fraiburgo

  <strong><em>Todos os direitos reservados © 2026</em></strong>
</div>