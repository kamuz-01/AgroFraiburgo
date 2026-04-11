-- Cria a tabela de favoritos de produtores (consumidor/moderador -> produtor)
-- Observação: este projeto usa um schema MySQL já existente (via dump), então esta migração é incremental.

CREATE TABLE IF NOT EXISTS favoritos_produtores (
  id_usuario INT NOT NULL,
  id_produtor INT NOT NULL,
  data_favorito DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario, id_produtor),
  KEY idx_favoritos_produtores_id_produtor (id_produtor),
  CONSTRAINT fk_favoritos_produtores_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_favoritos_produtores_produtor FOREIGN KEY (id_produtor)
    REFERENCES produtores (id_produtor) ON DELETE CASCADE
);
