SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

INSERT INTO usuario (
  id,
  nome_completo,
  cpf,
  email,
  senha_hash,
  data_nascimento,
  tipo_usuario,
  situacao_conta,
  criado_em,
  atualizado_em
)
SELECT
  '00000000-0000-0000-0000-000000000001'::uuid,
  'Administrador Cuidar+',
  '00000000000',
  'admin@cuidaplus.com.br',
  '$2a$12$YkcYPLG3d8JLtWz5Kg6d3evhydK0.L8CvuHXsrfZ7qa5XAFljJ/32',
  DATE '1990-01-01',
  'ADMIN',
  'ATIVO',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE tipo_usuario = 'ADMIN'
)
ON CONFLICT DO NOTHING;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE tipo_usuario = 'ADMIN') THEN
    RAISE EXCEPTION 'Não foi possível criar o administrador inicial; verifique conflitos de e-mail, CPF ou UUID.';
  END IF;
END $$;
