-- Tabela de tokens temporários para recuperação de senha.
-- O projeto usa um schema existente, então a migration é incremental.

CREATE TABLE IF NOT EXISTS recuperacao_senha_tokens (
  id_token INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  token VARCHAR(64) NOT NULL,
  expira_em DATETIME NOT NULL,
  usado_em DATETIME NULL,
  criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_token),
  UNIQUE KEY uk_recuperacao_senha_token (token),
  KEY idx_recuperacao_senha_id_usuario (id_usuario),
  CONSTRAINT fk_recuperacao_senha_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuarios (id_usuario) ON DELETE CASCADE
);
