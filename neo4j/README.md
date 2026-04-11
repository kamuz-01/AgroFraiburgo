# Neo4j (Recomendações)

Este projeto usa o Neo4j como **grafo de recomendações** (ex.: `User` → `FAVORITED` → `Product`).

## 1) Gerar CSVs + Cypher a partir do MySQL

O export roda via profile `neo4j-export` e gera os arquivos em `neo4j-export/`.

Windows (cmd/PowerShell):

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=neo4j-export
```

Alternativa (sem MySQL rodando): gerar a partir do dump `agrofraiburgo.sql` do repositório:

```bash
python neo4j\export_from_sql.py
```

Obs.: se ainda não houver favoritos registrados no MySQL/dump, o arquivo `neo4j-export/favorites.csv` ficará apenas com o header (isso é ok).

Saídas geradas:
- `neo4j-export/users.csv`
- `neo4j-export/products.csv`
- `neo4j-export/made_by.csv`
- `neo4j-export/favorites.csv`
- `neo4j-export/import.cypher`

## 2) Importar no Neo4j Desktop

1. Abra o banco no Neo4j Desktop e confira a pasta **import** do DB.
2. Copie os CSVs para essa pasta.
3. Abra o Neo4j Browser e execute o conteúdo de `import.cypher`.

Dica: rode `import.cypher` no banco certo (o DB que você criou para o projeto) e com o DB **Start** no Desktop.

Validação rápida (Neo4j 5):

```cypher
// Confira em qual DB você está (se necessário, selecione o DB do projeto):
// :use agrofraiburgo

// Dica: tipo de relacionamento é case-sensitive.
// 'favorited' e 'FAVORITED' são tipos diferentes.

CALL db.info() YIELD name RETURN name;

CALL db.labels();
CALL db.relationshipTypes();

MATCH (u:User) RETURN count(u) AS users;
MATCH (p:Product) RETURN count(p) AS products;

// Em Neo4j 5, MATCH com :FAVORITED pode dar erro se o tipo não existir ainda.
// Use este formato para contar sem erro:
MATCH ()-[r]->() WHERE type(r) = 'FAVORITED' RETURN count(r) AS favorited;
MATCH ()-[r]->() WHERE type(r) = 'FAVORITED_PRODUCER' RETURN count(r) AS favorited_producer;
MATCH ()-[r]->() WHERE type(r) = 'MADE_BY' RETURN count(r) AS made_by;
// E para labels, use este formato para evitar "Label does not exist":
MATCH (n) WHERE 'User' IN labels(n) RETURN count(n) AS users_safe;
MATCH (n) WHERE 'Product' IN labels(n) RETURN count(n) AS products_safe;
```

Se você receber um erro como:

`01N51: Relationship type does not exist ... in database '<db>'`

isso geralmente significa uma destas coisas:
- você está no **database errado** (rode `CALL db.info()` e use `:use <db>`), ou
- o tipo existe mas com **outra grafia/capitalização** (ex.: `favorited` ao invés de `FAVORITED`).

Para investigar o que existe de fato no DB atual:

```cypher
MATCH (a)-[r]->(b)
RETURN labels(a) AS de, type(r) AS rel, labels(b) AS para, count(*) AS qtd
ORDER BY qtd DESC;
```

Se `favorited` vier 0:
- garanta que o `favorites.csv` (o atualizado) foi copiado para a pasta **import** do DB;
- rode novamente o `import.cypher` (ou pelo menos o bloco de FAVORITED).

## 3) Habilitar recomendações no app

Defina:
- `NEO4J_ENABLED=true`
- `NEO4J_URI=bolt://localhost:7687` (Neo4j Desktop/local)
	- Para Neo4j Aura (cloud), use o URI seguro que o console do Aura fornece, por exemplo:
		- `NEO4J_URI=neo4j+s://<id>.databases.neo4j.io`
- `NEO4J_USERNAME=neo4j`
- `NEO4J_PASSWORD=...`
- `NEO4J_DATABASE=<nome-do-db>`
	- Neo4j Aura: normalmente `neo4j`
	- Neo4j Desktop: pode ser um nome customizado (ex.: `1293a192`)

Se aparecer no console do app algo como **`ServiceUnavailableException: Unable to connect to localhost:7687 (Connection refused)`**:
- o Neo4j Desktop provavelmente está **parado** (Start) ou o Bolt está em **outra porta**.
- no Desktop, abra o DBMS do projeto e confira em **Connection details** / **Settings** qual é a porta do **Bolt**.
- ajuste o `NEO4J_URI` para a porta correta (ex.: `bolt://localhost:7688`).

Se depois de conectar aparecer erro de **database não encontrado**, o nome do database geralmente é `neo4j` (Community Edition). Nesse caso, teste com `NEO4J_DATABASE=neo4j`.

Obs.: o projeto já carrega automaticamente o arquivo `.env` na raiz (via `dotenv-java` no `main()`), então basta manter essas chaves no `.env`.

Windows (PowerShell, só para a sessão atual):

```powershell
$env:NEO4J_ENABLED="true"
$env:NEO4J_URI="bolt://localhost:7687"
$env:NEO4J_USERNAME="neo4j"
$env:NEO4J_PASSWORD="<SUA_SENHA>"
```

Importante: evite colocar senha em arquivo versionado. Prefira variável de ambiente.

O app vai consultar o Neo4j para montar a seção **Recomendados para você** na vitrine.
