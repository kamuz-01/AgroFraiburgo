import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL_PATH = ROOT / "agrofraiburgo.sql"
OUT_DIR = ROOT / "neo4j-export"


def read_sql_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")


def parse_insert_blocks(sql: str, table: str):
    # Captura INSERT INTO `table` (`col1`,...) VALUES (...),(...);
    pattern = re.compile(
        rf"INSERT\s+INTO\s+`{re.escape(table)}`\s*\((?P<cols>[^)]*)\)\s*VALUES\s*(?P<values>.*?);",
        re.IGNORECASE | re.DOTALL,
    )
    for m in pattern.finditer(sql):
        cols_raw = m.group("cols")
        cols = [c.strip().strip('`') for c in cols_raw.split(',')]
        values_raw = m.group("values").strip()
        yield cols, values_raw


def split_tuples(values_raw: str):
    # Retorna lista de strings "..." para cada tupla, sem os parênteses externos
    tuples = []
    i = 0
    n = len(values_raw)
    while i < n:
        while i < n and values_raw[i].isspace():
            i += 1
        if i >= n:
            break
        if values_raw[i] != '(':
            i += 1
            continue
        i += 1
        start = i
        depth = 1
        in_str = False
        esc = False
        while i < n:
            ch = values_raw[i]
            if in_str:
                if esc:
                    esc = False
                elif ch == '\\':
                    esc = True
                elif ch == "'":
                    in_str = False
            else:
                if ch == "'":
                    in_str = True
                elif ch == '(':
                    depth += 1
                elif ch == ')':
                    depth -= 1
                    if depth == 0:
                        tuples.append(values_raw[start:i])
                        i += 1
                        break
            i += 1
        # Avança até o próximo '(' (pulando vírgulas)
        while i < n and values_raw[i] not in '(': 
            i += 1
    return tuples


def split_fields(tuple_raw: str):
    fields = []
    cur = []
    in_str = False
    esc = False
    i = 0
    n = len(tuple_raw)
    while i < n:
        ch = tuple_raw[i]
        if in_str:
            if esc:
                cur.append(ch)
                esc = False
            elif ch == '\\':
                esc = True
            elif ch == "'":
                in_str = False
            else:
                cur.append(ch)
        else:
            if ch == "'":
                in_str = True
            elif ch == ',':
                fields.append(''.join(cur).strip())
                cur = []
            else:
                cur.append(ch)
        i += 1
    fields.append(''.join(cur).strip())

    # Normaliza NULL
    norm = []
    for f in fields:
        if f.upper() == 'NULL':
            norm.append(None)
        else:
            norm.append(f)
    return norm


def csv_safe(s: str) -> str:
    if s is None:
        return ""
    s = str(s)
    s = s.replace('\r', ' ').replace('\n', ' ').strip()
    # CSV simples sem aspas: remover vírgula
    return s.replace(',', ' ')


def export_users(rows):
    # users.csv: id,tipo
    out = ["id,tipo\n"]
    for r in rows:
        tipo = (r.get('tipo_usuario') or '').strip()
        status = (r.get('status_conta') or 'ATIVO').strip()
        if tipo not in ('CONSUMIDOR', 'MODERADOR'):
            continue
        if status and status != 'ATIVO':
            continue
        out.append(f"{r['id_usuario']},{tipo}\n")
    return ''.join(out)


def export_products(rows):
    # products.csv: id,nome,idProdutor
    out = ["id,nome,idProdutor\n"]
    for r in rows:
        out.append(
            f"{r['id_produto']},{csv_safe(r.get('nome_produto'))},{csv_safe(r.get('id_produtor'))}\n"
        )
    return ''.join(out)


def export_made_by(product_rows):
    out = ["productId,producerId\n"]
    for r in product_rows:
        pid = r.get('id_produto')
        pr = r.get('id_produtor')
        if pid is None or pr is None or str(pr).strip() == "":
            continue
        out.append(f"{pid},{pr}\n")
    return ''.join(out)


def export_favorites_empty():
    return "userId,productId,at\n"


def import_cypher_text():
    return """// Rode este arquivo no Neo4j Browser (Neo4j Desktop) após copiar os CSVs para a pasta 'import' do DB.
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

// Sanity check (Neo4j 5: evita erro \"Label does not exist\" / \"Relationship type does not exist\")
MATCH (n) WHERE 'User' IN labels(n) RETURN count(n) AS users;
MATCH (n) WHERE 'Product' IN labels(n) RETURN count(n) AS products;
MATCH ()-[r]->() RETURN count(r) AS rels_total;
MATCH ()-[r]->() WHERE type(r) = 'MADE_BY' RETURN count(r) AS made_by;
MATCH ()-[r]->() WHERE type(r) = 'FAVORITED' RETURN count(r) AS favorited;
"""


def main():
    sql = read_sql_text(SQL_PATH)

    # Usuarios
    usuario_rows = []
    for cols, values_raw in parse_insert_blocks(sql, 'usuarios'):
        tuples = split_tuples(values_raw)
        for t in tuples:
            fields = split_fields(t)
            row = {}
            for c, v in zip(cols, fields):
                row[c] = v
            usuario_rows.append(row)

    # Produtos
    produto_rows = []
    for cols, values_raw in parse_insert_blocks(sql, 'produtos'):
        tuples = split_tuples(values_raw)
        for t in tuples:
            fields = split_fields(t)
            row = {}
            for c, v in zip(cols, fields):
                row[c] = v
            produto_rows.append(row)

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    (OUT_DIR / 'users.csv').write_text(export_users(usuario_rows), encoding='utf-8')
    (OUT_DIR / 'products.csv').write_text(export_products(produto_rows), encoding='utf-8')
    (OUT_DIR / 'made_by.csv').write_text(export_made_by(produto_rows), encoding='utf-8')
    # No dump antigo não existe favoritos; gera header vazio.
    (OUT_DIR / 'favorites.csv').write_text(export_favorites_empty(), encoding='utf-8')
    (OUT_DIR / 'import.cypher').write_text(import_cypher_text(), encoding='utf-8')

    print('OK')
    print(str(OUT_DIR))


if __name__ == '__main__':
    main()
