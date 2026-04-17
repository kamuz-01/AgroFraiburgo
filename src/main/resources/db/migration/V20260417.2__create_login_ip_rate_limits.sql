CREATE TABLE IF NOT EXISTS login_ip_rate_limits (
    id_login_ip_rate_limit BIGINT NOT NULL AUTO_INCREMENT,
    ip_address VARCHAR(45) NOT NULL,
    tentativas_na_janela INT NOT NULL DEFAULT 0,
    janela_inicio DATETIME NULL,
    bloqueado_ate DATETIME NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    versao BIGINT NULL,
    PRIMARY KEY (id_login_ip_rate_limit),
    UNIQUE KEY uk_login_ip_rate_limits_ip_address (ip_address),
    KEY idx_login_ip_rate_limits_bloqueado_ate (bloqueado_ate),
    KEY idx_login_ip_rate_limits_janela_inicio (janela_inicio),
    KEY idx_login_ip_rate_limits_atualizado_em (atualizado_em)
);