// Rode este arquivo no Neo4j Browser (Neo4j Desktop) após copiar os CSVs para a pasta 'import' do DB.
// Ex.: file:///users.csv

CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT product_id IF NOT EXISTS FOR (p:Product) REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT producer_id IF NOT EXISTS FOR (pr:Producer) REQUIRE pr.id IS UNIQUE;

// Users
LOAD CSV WITH HEADERS FROM 'file:///users.csv' AS row
WITH row WHERE row.id IS NOT NULL AND row.id <> ''
MERGE (u:User {id: toInteger(row.id)})
SET u.tipo = row.tipo;

// Products
LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS row
WITH row WHERE row.id IS NOT NULL AND row.id <> ''
MERGE (p:Product {id: toInteger(row.id)})
SET p.nome = row.nome;

// Producers (derivado de products.idProdutor)
LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS row
WITH row WHERE row.idProdutor IS NOT NULL AND row.idProdutor <> ''
MERGE (pr:Producer {id: toInteger(row.idProdutor)});

// MADE_BY
LOAD CSV WITH HEADERS FROM 'file:///made_by.csv' AS row
WITH row WHERE row.productId IS NOT NULL AND row.productId <> '' AND row.producerId IS NOT NULL AND row.producerId <> ''
MATCH (p:Product {id: toInteger(row.productId)})
MATCH (pr:Producer {id: toInteger(row.producerId)})
MERGE (p)-[:MADE_BY]->(pr);

// FAVORITED
LOAD CSV WITH HEADERS FROM 'file:///favorites.csv' AS row
WITH row WHERE row.userId IS NOT NULL AND row.userId <> '' AND row.productId IS NOT NULL AND row.productId <> ''
MATCH (u:User {id: toInteger(row.userId)})
MATCH (p:Product {id: toInteger(row.productId)})
MERGE (u)-[f:FAVORITED]->(p)
SET f.at = row.at;

// Sanity check (Neo4j 5: evita erro "Label does not exist" / "Relationship type does not exist")
MATCH (n) WHERE 'User' IN labels(n) RETURN count(n) AS users;
MATCH (n) WHERE 'Product' IN labels(n) RETURN count(n) AS products;
MATCH ()-[r]->() RETURN count(r) AS rels_total;
MATCH ()-[r]->() WHERE type(r) = 'MADE_BY' RETURN count(r) AS made_by;
MATCH ()-[r]->() WHERE type(r) = 'FAVORITED' RETURN count(r) AS favorited;
