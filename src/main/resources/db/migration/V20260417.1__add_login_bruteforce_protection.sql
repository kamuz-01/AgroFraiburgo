ALTER TABLE usuarios
    ADD COLUMN login_bloqueio_etapa VARCHAR(32) NOT NULL DEFAULT 'LIVRE',
    ADD COLUMN login_falhas_consecutivas INT NOT NULL DEFAULT 0,
    ADD COLUMN login_bloqueado_ate DATETIME NULL;