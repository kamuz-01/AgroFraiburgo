# AgroFraiburgo 🌱

## Sobre o Projeto

O AgroFraiburgo é um protótipo de aplicativo para disponibilizar informações sobre as feiras de alimentos produzidos pela agricultura familiar na cidade de Fraiburgo/SC.

## Funcionalidades Principais

- 📄 Revisão da documentação dos produtores
- 👤 Gestão de usuários
- 🔒 Sistema de autenticação seguro
- 📱 Interface responsiva e amigável

## Tecnologias Utilizadas

- Java Spring Boot
- Maven
- Base de Dados MySQL
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

- Java 11 ou superior
- SpringBoot 3.x ou superior
- Maven 3.x
- IDE compatível (recomendado VS Code ou Spring Tool Suite)

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
```

### 4. Execute o projeto

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## Estrutura de Diretórios

```
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

Karli De Jesus Munoz Manzano

📧 mailto:karli.manzano@estudantes.ifc.edu.br

---

*Desenvolvido com ❤️ para a agricultura familiar de Fraiburgo

*Todos os direitos reservados © 2025
