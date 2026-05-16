-- Guarda o IP de origem para limitar solicitações repetidas de recuperação de senha.

ALTER TABLE recuperacao_senha_tokens
  ADD COLUMN ip_address VARCHAR(45) NULL;

CREATE INDEX idx_recuperacao_senha_usuario_criado
  ON recuperacao_senha_tokens (id_usuario, criado_em);

CREATE INDEX idx_recuperacao_senha_ip_criado
  ON recuperacao_senha_tokens (ip_address, criado_em);
