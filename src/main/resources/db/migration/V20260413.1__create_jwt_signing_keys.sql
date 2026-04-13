-- Tabela de chaves JWT versionadas para rotação automática com coexistência.
-- O projeto usa um schema existente, então a migration é incremental.

CREATE TABLE IF NOT EXISTS jwt_signing_keys (
  id_chave BIGINT NOT NULL AUTO_INCREMENT,
  key_version INT NOT NULL,
  secret_base64 VARCHAR(512) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL,
  PRIMARY KEY (id_chave),
  UNIQUE KEY uk_jwt_signing_keys_version (key_version),
  KEY idx_jwt_signing_keys_active (active),
  KEY idx_jwt_signing_keys_expires_at (expires_at)
);