-- Lote 2/7: contatos e coleções dos perfis.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '2min';

SELECT cuidaplus_renomear_tabela(nome_antigo, nome_novo)
FROM (VALUES
  ('emergency_contacts', 'contatos_emergencia'),
  ('assisted_person_allergies', 'alergias_pessoas_assistidas'),
  ('assisted_person_food_restrictions', 'restricoes_alimentares_pessoas_assistidas'),
  ('caregiver_modalities', 'modalidades_cuidadores'),
  ('caregiver_services', 'servicos_cuidadores'),
  ('caregiver_availability_days', 'dias_disponibilidade_cuidadores'),
  ('caregiver_availability_periods', 'periodos_disponibilidade_cuidadores'),
  ('caregiver_formations', 'formacoes_cuidadores')
) AS tabelas(nome_antigo, nome_novo);

SELECT cuidaplus_renomear_coluna(tabela, nome_antigo, nome_novo)
FROM (VALUES
  ('contatos_emergencia', 'assisted_person_id', 'pessoa_assistida_id'),
  ('contatos_emergencia', 'responsible_contact', 'contato_responsavel'),
  ('contatos_emergencia', 'created_at', 'criado_em'),
  ('contatos_emergencia', 'updated_at', 'atualizado_em'),
  ('alergias_pessoas_assistidas', 'assisted_person_id', 'pessoa_assistida_id'),
  ('restricoes_alimentares_pessoas_assistidas', 'assisted_person_id', 'pessoa_assistida_id'),
  ('modalidades_cuidadores', 'caregiver_profile_id', 'perfil_cuidador_id'),
  ('servicos_cuidadores', 'caregiver_profile_id', 'perfil_cuidador_id'),
  ('dias_disponibilidade_cuidadores', 'caregiver_profile_id', 'perfil_cuidador_id'),
  ('periodos_disponibilidade_cuidadores', 'caregiver_profile_id', 'perfil_cuidador_id'),
  ('formacoes_cuidadores', 'caregiver_profile_id', 'perfil_cuidador_id')
) AS colunas(tabela, nome_antigo, nome_novo);

