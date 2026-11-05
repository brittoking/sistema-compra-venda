-- Execute no mesmo schema configurado no JDBC, antes de criar/alterar usuários.
-- Não apaga registros. Permite armazenar senhas novas com hash PBKDF2.
ALTER TABLE usuario ALTER COLUMN usu_senha TYPE varchar(255);

-- A unicidade também evita logins duplicados por gravações simultâneas.
-- Se já houver duplicados, este comando falhará sem apagar usuários.
-- Identifique-os, ajuste os logins e execute novamente.
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_login ON usuario (usu_login);
