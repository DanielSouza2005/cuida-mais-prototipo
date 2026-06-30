-- Improved signup model for responsible, assisted person and caregiver profiles.

alter table if exists users add column if not exists phone varchar(20);
alter table if exists users add column if not exists status varchar(30) not null default 'ACTIVE';

create table if not exists responsible_profiles (
  id uuid primary key,
  user_id uuid not null unique references users(id),
  parentesco varchar(40) not null,
  parentesco_outro varchar(120),
  preferencia_contato varchar(30) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table if not exists caregiver_profiles (
  id uuid primary key,
  user_id uuid not null unique references users(id),
  formacao varchar(40),
  formacao_outro varchar(180),
  experiencia varchar(500),
  biografia varchar(500),
  cep varchar(9),
  rua varchar(180),
  numero varchar(30),
  complemento varchar(120),
  bairro varchar(120),
  cidade varchar(120),
  estado varchar(2),
  ponto_referencia varchar(180),
  horario_inicio time,
  horario_fim time,
  observacao varchar(500),
  modalidade_outro varchar(180),
  servico_outro varchar(180),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table if not exists assisted_persons (
  id uuid primary key,
  responsible_user_id uuid not null references users(id),
  nome varchar(140) not null,
  cpf varchar(11),
  data_nascimento date not null,
  grau_dependencia varchar(30) not null,
  mobilidade varchar(30) not null,
  mobilidade_outro varchar(120),
  alergias_outro varchar(180),
  alergias_detalhes varchar(500),
  restricoes_alimentares_outro varchar(180),
  restricoes_alimentares_detalhes varchar(500),
  medicamentos varchar(500),
  observacoes varchar(500),
  cep varchar(9),
  rua varchar(180),
  numero varchar(30),
  complemento varchar(120),
  bairro varchar(120),
  cidade varchar(120),
  estado varchar(2),
  ponto_referencia varchar(180),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table if not exists emergency_contacts (
  id uuid primary key,
  assisted_person_id uuid not null unique references assisted_persons(id),
  nome varchar(140) not null,
  telefone varchar(20) not null,
  vinculo varchar(120) not null,
  responsible_contact boolean not null default false,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table if not exists assisted_person_allergies (
  assisted_person_id uuid not null references assisted_persons(id),
  alergia varchar(40) not null
);

create table if not exists assisted_person_food_restrictions (
  assisted_person_id uuid not null references assisted_persons(id),
  restricao varchar(40) not null
);

create table if not exists caregiver_modalities (
  caregiver_profile_id uuid not null references caregiver_profiles(id),
  modalidade varchar(40) not null
);

create table if not exists caregiver_services (
  caregiver_profile_id uuid not null references caregiver_profiles(id),
  servico varchar(50) not null
);

create table if not exists caregiver_availability_days (
  caregiver_profile_id uuid not null references caregiver_profiles(id),
  dia_semana varchar(20) not null
);

create table if not exists caregiver_availability_periods (
  caregiver_profile_id uuid not null references caregiver_profiles(id),
  periodo varchar(30) not null
);
