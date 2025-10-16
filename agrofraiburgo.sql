-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 10/10/2025 às 15:08
-- Versão do servidor: 8.4.6
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `agrofraiburgo`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `avaliacoes`
--

CREATE TABLE `avaliacoes` (
  `id_avaliacao` int NOT NULL,
  `id_consumidor` int DEFAULT NULL,
  `id_produtor` int DEFAULT NULL,
  `nota` int NOT NULL,
  `comentario` text COLLATE utf8mb4_general_ci,
  `data_avaliacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura para tabela `consumidores`
--

CREATE TABLE `consumidores` (
  `id_consumidor` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `consumidores`
--

INSERT INTO `consumidores` (`id_consumidor`) VALUES
(6),
(7),
(11),
(12),
(13),
(19);

-- --------------------------------------------------------

--
-- Estrutura para tabela `documentos_produtor`
--

CREATE TABLE `documentos_produtor` (
  `id_documento` int NOT NULL,
  `id_produtor` int NOT NULL,
  `documento_identidade` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `comprovante_residencia` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `declaracao_pronaf` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `certificado_producao_organica` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `codigo_rastreabilidade` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `numero_inscricao_estadual` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `alvara_sanitario` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `data_envio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `documentos_produtor`
--

INSERT INTO `documentos_produtor` (`id_documento`, `id_produtor`, `documento_identidade`, `comprovante_residencia`, `declaracao_pronaf`, `certificado_producao_organica`, `codigo_rastreabilidade`, `numero_inscricao_estadual`, `alvara_sanitario`, `data_envio`) VALUES
(2, 23, '/documentos-produtores/23/identidade_1757600850706.pdf', '/documentos-produtores/23/residencia_1757600850708.pdf', '/documentos-produtores/23/pronaf_1757600850710.pdf', '/documentos-produtores/23/organico_1757600850713.pdf', '/documentos-produtores/23/rastreabilidade_1757600850715.pdf', '/documentos-produtores/23/inscricao_1757600850717.pdf', '/documentos-produtores/23/alvara_1757600850719.pdf', '2025-09-11 11:27:31'),
(3, 26, '/documentos-produtores/26/identidade_1759931339226.pdf', '/documentos-produtores/26/residencia_1759931339231.pdf', '/documentos-produtores/26/pronaf_1759931339233.pdf', '/documentos-produtores/26/organico_1759931339236.pdf', '/documentos-produtores/26/rastreabilidade_1759931339239.pdf', '/documentos-produtores/26/inscricao_1759931339241.pdf', '/documentos-produtores/26/alvara_1759931339244.pdf', '2025-10-08 10:48:59');

-- --------------------------------------------------------

--
-- Estrutura para tabela `feira`
--

CREATE TABLE `feira` (
  `id_feira` int NOT NULL,
  `id_moderador` int NOT NULL,
  `nome_local` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `logradouro` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `numero` int NOT NULL,
  `bairro` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `complemento` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status_feira` enum('EM_ANDAMENTO','SUSPENSA','ENCERRADO') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `feira`
--

INSERT INTO `feira` (`id_feira`, `id_moderador`, `nome_local`, `logradouro`, `numero`, `bairro`, `complemento`, `status_feira`) VALUES
(1, 24, 'Feira do Sabor', 'Rua Marly', 45, 'Centro', 'Outro', 'EM_ANDAMENTO');

-- --------------------------------------------------------

--
-- Estrutura para tabela `moderadores`
--

CREATE TABLE `moderadores` (
  `id_moderador` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `moderadores`
--

INSERT INTO `moderadores` (`id_moderador`) VALUES
(24),
(25);

-- --------------------------------------------------------

--
-- Estrutura para tabela `produtores`
--

CREATE TABLE `produtores` (
  `id_produtor` int NOT NULL,
  `avaliacoes_recebidas` int DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `produtores`
--

INSERT INTO `produtores` (`id_produtor`, `avaliacoes_recebidas`) VALUES
(23, 0),
(26, 0);

-- --------------------------------------------------------

--
-- Estrutura para tabela `produtos`
--

CREATE TABLE `produtos` (
  `id_produto` int NOT NULL,
  `id_produtor` int NOT NULL,
  `nome_produto` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `descricao` text COLLATE utf8mb4_general_ci,
  `preco` decimal(10,2) NOT NULL,
  `unidade_medida` varchar(50) COLLATE utf8mb4_general_ci DEFAULT 'kg',
  `quantidade_estoque` decimal(10,2) NOT NULL,
  `status_produto` enum('COM_ESTOQUE','SEM_ESTOQUE') COLLATE utf8mb4_general_ci DEFAULT 'COM_ESTOQUE',
  `imagem_produto` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `data_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `data_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ;

--
-- Despejando dados para a tabela `produtos`
--

INSERT INTO `produtos` (`id_produto`, `id_produtor`, `nome_produto`, `descricao`, `preco`, `unidade_medida`, `quantidade_estoque`, `status_produto`, `imagem_produto`, `data_criacao`, `data_atualizacao`) VALUES
(3, 23, 'Pêssego', 'Fresco, natural, do seu jeito', 6.00, 'kg', 250.00, 'COM_ESTOQUE', '/imagens-usuarios/23/produtos/1759864944571_nisonco-pr-and-seo-H4QpChoce7I-u.png', '2025-10-07 16:22:25', '2025-10-07 16:22:25'),
(4, 23, 'Uva de mesa', 'As melhores uvas do mundo', 10.00, 'kg', 250.00, 'COM_ESTOQUE', '/imagens-usuarios/23/produtos/1759869628668_tipos-de-uvas-mais-vendidas-no-v.png', '2025-10-07 17:40:29', '2025-10-07 17:40:29');

--
-- Acionadores `produtos`
--
DELIMITER $$
CREATE TRIGGER `trg_produtos_status_update` BEFORE UPDATE ON `produtos` FOR EACH ROW BEGIN
  IF NEW.quantidade_estoque > 0 THEN
    SET NEW.status_produto = 'COM_ESTOQUE';
  ELSE
    SET NEW.status_produto = 'SEM_ESTOQUE';
  END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura para tabela `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int NOT NULL,
  `nome_usuario` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sobrenome_usuario` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cpf_usuario` varchar(11) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `data_nascimento` date DEFAULT NULL,
  `sexo` varchar(17) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telefone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(160) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nome_login` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `senha` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tipo_usuario` enum('CONSUMIDOR','PRODUTOR','MODERADOR') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CONSUMIDOR',
  `oauth_provider` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `oauth_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_conta` enum('ATIVO','PENDENTE','REJEITADO','BLOQUEADO') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ATIVO',
  `imagem_perfil` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '/imagens-usuarios/defaults/perfil.png',
  `imagem_capa` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '/imagens-usuarios/defaults/capa.webp',
  `cidade` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `estado` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nome_usuario`, `sobrenome_usuario`, `cpf_usuario`, `data_nascimento`, `sexo`, `telefone`, `email`, `nome_login`, `senha`, `tipo_usuario`, `oauth_provider`, `oauth_id`, `criado_em`, `atualizado_em`, `status_conta`, `imagem_perfil`, `imagem_capa`, `cidade`, `estado`) VALUES
(6, 'Jesús', 'Muñoz', NULL, NULL, NULL, NULL, 'jesusvzlanz@gmail.com', NULL, NULL, 'CONSUMIDOR', 'google', '118299944397952644508', '2025-08-28 11:11:47', '2025-10-03 23:29:09', 'ATIVO', '/imagens-usuarios/6/imagem-perfil/perfil.png', '/imagens-usuarios/defaults/imagem-capa/capa.webp', NULL, NULL),
(7, 'Jesus', 'Muñoz', NULL, NULL, NULL, NULL, 'carlos-raul19630@hotmail.com', NULL, NULL, 'CONSUMIDOR', 'facebook', '10230259903385789', '2025-08-28 11:12:20', '2025-10-03 23:31:33', 'ATIVO', '/imagens-usuarios/7/imagem-perfil/perfil.png', '/imagens-usuarios/defaults/imagem-capa/capa.webp', NULL, NULL),
(11, 'Pedro', 'Pérez', '04904699068', '2007-01-01', 'Masculino', '11111111111', 'pedro@mail.com', 'pedro', '$2a$10$HGMfv5DRiKAEHVPHBgW9Xeayst/97qR0NJ4aqn4uqbMNFgCPEt0Sq', 'CONSUMIDOR', NULL, NULL, '2025-09-09 16:30:51', '2025-09-09 16:30:52', 'ATIVO', '/imagens-usuarios/11/imagem-perfil/perfil_1757446251436.png', '/imagens-usuarios/11/imagem-capa/capa_1757446251443.png', 'Fraiburgo', 'SC'),
(12, 'Jose', 'Torres', '24654075038', '2007-01-30', 'Masculino', '22222222222', 'jose@email.com', 'jose', '$2a$10$DwQhT8t7xu4OW5852Xua7OAtn1XShtocbUDnPP9ScYtxBcBzxkeOu', 'CONSUMIDOR', NULL, NULL, '2025-09-09 17:06:36', '2025-10-03 23:34:03', 'ATIVO', '/imagens-usuarios/12/imagem-perfil/perfil_1757448396543.png', '/imagens-usuarios/12/imagem-capa/capa_1757448396548.png', 'Fraiburgo', 'SC'),
(13, 'Juan', 'Diaz', '73466525098', '1998-07-21', 'Masculino', '33333333333', 'juan@mail.com', 'juan', '$2a$10$2tSHFVPPl6S/yExmfWe2Ue9wB/fzcDFNkWdQfsZTM.CnbbeXeKLUG', 'CONSUMIDOR', NULL, NULL, '2025-09-09 17:48:49', '2025-09-09 17:48:50', 'ATIVO', '/imagens-usuarios/13/imagem-perfil/perfil_1757450929622.png', '/imagens-usuarios/13/imagem-capa/capa_1757450929627.png', 'Fraiburgo', 'SC'),
(19, 'Marta', 'Colomina', '60260312002', '2007-01-01', 'Feminino', '55555555555', 'marta@email.com', 'marta', '$2a$10$ybsm1hnC1XyKvv7uR5eXDO1C8rJVvuR4UlIejz/8dVbtVm3JQgn9W', 'CONSUMIDOR', NULL, NULL, '2025-09-10 17:28:00', '2025-10-03 23:36:36', 'ATIVO', '/imagens-usuarios/19/imagem-perfil/perfil_1757536080088.png', '/imagens-usuarios/19/imagem-capa/capa_1757536080109.png', 'Fraiburgo', 'SC'),
(23, 'Antonia', 'Palacios', '36277330020', '2007-01-01', 'Feminino', '66666666666', 'antonia@email.com', 'antonia', '$2a$10$PF4s6TNND3tBr6gRM2GPb.6UkEniFU/cOqkaPajM2hWD7z97mkqRe', 'PRODUTOR', NULL, NULL, '2025-09-11 11:27:30', '2025-10-06 11:00:17', 'ATIVO', '/imagens-usuarios/23/imagem-perfil/perfil_1757600850694.png', '/imagens-usuarios/23/imagem-capa/capa_1757600850703.png', 'Fraiburgo', 'SC'),
(24, 'Xavier', 'Machado', '03714918000', '2000-12-25', 'Masculino', '10101010101', 'xavier@email.com', 'xavier', '$2a$10$Y4x66GlSF65cb6Rbt8O92OMcfCG/QsuBC7GHV4kHVNfen7BoRfgRi', 'MODERADOR', NULL, NULL, '2025-09-24 16:50:30', '2025-09-24 16:50:30', 'ATIVO', '/imagens-usuarios/24/imagem-perfil/perfil_foto.png', '/imagens-usuarios/24/imagem-capa/capa_imagem.png', 'Fraiburgo', 'SC'),
(25, 'Damian', 'Ferreira', '47018602041', '2005-09-03', 'Masculino', '34567567456', 'damian@email.com', 'damian', '$2a$10$vjQtQeHcMXaD7w8wJCn9sOiFZFtfcy0Tee0Cy8rCnG/Fni6r8xIYe', 'MODERADOR', NULL, NULL, '2025-10-03 18:30:28', '2025-10-03 18:30:28', 'ATIVO', '/imagens-usuarios/25/imagem-perfil/perfil_1759527028287.png', '/imagens-usuarios/25/imagem-capa/capa_1759527028293.png', 'Fraiburgo', 'SC'),
(26, 'Petra', 'Morales', '71795012048', '1990-11-14', 'Feminino', '56784567894', 'petra@mail.com', 'petra', '$2a$10$Kgb2qU1OXtymq4mt3bRv3e5y6ixZfPcJ5WTlDB9C9qKg1FrZHNi36', 'PRODUTOR', NULL, NULL, '2025-10-08 10:48:59', '2025-10-09 14:38:43', 'PENDENTE', '/imagens-usuarios/26/imagem-perfil/perfil_1759931339212.png', '/imagens-usuarios/26/imagem-capa/capa_1759931339223.png', 'Fraiburgo', 'SC');

--
-- Acionadores `usuarios`
--
DELIMITER $$
CREATE TRIGGER `trg_after_insert_usuario_consumidor` AFTER INSERT ON `usuarios` FOR EACH ROW BEGIN
    IF NEW.tipo_usuario = 'CONSUMIDOR' THEN
        INSERT INTO consumidores (id_consumidor) VALUES (NEW.id_usuario);
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_after_insert_usuario_moderador` AFTER INSERT ON `usuarios` FOR EACH ROW BEGIN
    IF NEW.tipo_usuario = 'MODERADOR' THEN
        INSERT INTO moderadores (id_moderador) VALUES (NEW.id_usuario);
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_after_insert_usuario_produtor` AFTER INSERT ON `usuarios` FOR EACH ROW BEGIN
    IF NEW.tipo_usuario = 'PRODUTOR' THEN
        INSERT INTO produtores (id_produtor) VALUES (NEW.id_usuario);
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_usuarios_bi` BEFORE INSERT ON `usuarios` FOR EACH ROW BEGIN
  IF NOT (
      (NEW.email IS NOT NULL AND NEW.senha IS NOT NULL)
      OR
      (NEW.oauth_provider IS NOT NULL AND NEW.oauth_id IS NOT NULL)
    )
  THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Informe email+senha (conta local) OU oauth_provider+oauth_id (conta OAuth).';
  END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_usuarios_bu` BEFORE UPDATE ON `usuarios` FOR EACH ROW BEGIN
  IF NOT (
      (NEW.email IS NOT NULL AND NEW.senha IS NOT NULL)
      OR
      (NEW.oauth_provider IS NOT NULL AND NEW.oauth_id IS NOT NULL)
    )
  THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Informe email+senha (conta local) OU oauth_provider+oauth_id (conta OAuth).';
  END IF;
END
$$
DELIMITER ;

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `avaliacoes`
--
ALTER TABLE `avaliacoes`
  ADD PRIMARY KEY (`id_avaliacao`),
  ADD KEY `id_consumidor` (`id_consumidor`),
  ADD KEY `idx_avaliacoes_produtor` (`id_produtor`);

--
-- Índices de tabela `consumidores`
--
ALTER TABLE `consumidores`
  ADD PRIMARY KEY (`id_consumidor`);

--
-- Índices de tabela `documentos_produtor`
--
ALTER TABLE `documentos_produtor`
  ADD PRIMARY KEY (`id_documento`),
  ADD KEY `idx_docs_produtor` (`id_produtor`);

--
-- Índices de tabela `feira`
--
ALTER TABLE `feira`
  ADD PRIMARY KEY (`id_feira`),
  ADD KEY `fk_feira_moderador` (`id_moderador`);

--
-- Índices de tabela `moderadores`
--
ALTER TABLE `moderadores`
  ADD PRIMARY KEY (`id_moderador`);

--
-- Índices de tabela `produtores`
--
ALTER TABLE `produtores`
  ADD PRIMARY KEY (`id_produtor`);

--
-- Índices de tabela `produtos`
--
ALTER TABLE `produtos`
  ADD PRIMARY KEY (`id_produto`),
  ADD KEY `id_produtor` (`id_produtor`);

--
-- Índices de tabela `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `cpf_usuario` (`cpf_usuario`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `nome_login` (`nome_login`),
  ADD UNIQUE KEY `ux_oauth` (`oauth_provider`,`oauth_id`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `avaliacoes`
--
ALTER TABLE `avaliacoes`
  MODIFY `id_avaliacao` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `documentos_produtor`
--
ALTER TABLE `documentos_produtor`
  MODIFY `id_documento` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de tabela `feira`
--
ALTER TABLE `feira`
  MODIFY `id_feira` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de tabela `produtos`
--
ALTER TABLE `produtos`
  MODIFY `id_produto` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `avaliacoes`
--
ALTER TABLE `avaliacoes`
  ADD CONSTRAINT `avaliacoes_ibfk_1` FOREIGN KEY (`id_consumidor`) REFERENCES `consumidores` (`id_consumidor`) ON DELETE SET NULL,
  ADD CONSTRAINT `avaliacoes_ibfk_2` FOREIGN KEY (`id_produtor`) REFERENCES `produtores` (`id_produtor`) ON DELETE SET NULL;

--
-- Restrições para tabelas `consumidores`
--
ALTER TABLE `consumidores`
  ADD CONSTRAINT `consumidores_ibfk_1` FOREIGN KEY (`id_consumidor`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Restrições para tabelas `documentos_produtor`
--
ALTER TABLE `documentos_produtor`
  ADD CONSTRAINT `documentos_produtor_ibfk_1` FOREIGN KEY (`id_produtor`) REFERENCES `produtores` (`id_produtor`) ON DELETE CASCADE;

--
-- Restrições para tabelas `feira`
--
ALTER TABLE `feira`
  ADD CONSTRAINT `fk_feira_moderador` FOREIGN KEY (`id_moderador`) REFERENCES `moderadores` (`id_moderador`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Restrições para tabelas `moderadores`
--
ALTER TABLE `moderadores`
  ADD CONSTRAINT `moderadores_ibfk_1` FOREIGN KEY (`id_moderador`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Restrições para tabelas `produtores`
--
ALTER TABLE `produtores`
  ADD CONSTRAINT `produtores_ibfk_1` FOREIGN KEY (`id_produtor`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Restrições para tabelas `produtos`
--
ALTER TABLE `produtos`
  ADD CONSTRAINT `produtos_ibfk_1` FOREIGN KEY (`id_produtor`) REFERENCES `produtores` (`id_produtor`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
