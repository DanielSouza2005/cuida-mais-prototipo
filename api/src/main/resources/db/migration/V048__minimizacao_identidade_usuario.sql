SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

-- Usuários existentes já passaram pelo cadastro vigente. A confirmação é
-- preenchida somente para compatibilizar a base; novos cadastros precisam
-- enviar true e são validados pela API.
ALTER TABLE usuario
  ADD COLUMN maior_de_idade_confirmado boolean;

UPDATE usuario
SET maior_de_idade_confirmado = true
WHERE maior_de_idade_confirmado IS NULL;

ALTER TABLE usuario
  ALTER COLUMN maior_de_idade_confirmado SET NOT NULL,
  ALTER COLUMN telefone DROP NOT NULL;

ALTER TABLE responsavel
  ALTER COLUMN preferencia_contato DROP NOT NULL;

-- A remoção das colunas elimina também constraints e índices dependentes.
ALTER TABLE usuario
  DROP COLUMN cpf,
  DROP COLUMN data_nascimento;

ALTER TABLE pessoa_assistida
  DROP COLUMN cpf;
