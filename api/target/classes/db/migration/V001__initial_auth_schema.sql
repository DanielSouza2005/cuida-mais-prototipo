create table if not exists users (
  id uuid primary key,
  full_name varchar(140) not null,
  cpf varchar(11) not null,
  email varchar(180) not null,
  password_hash varchar(255) not null,
  birth_date date not null,
  user_type varchar(20) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint uk_users_email unique (email),
  constraint uk_users_cpf unique (cpf),
  constraint ck_users_user_type check (user_type in ('RESPONSAVEL', 'CUIDADOR', 'ADMIN', 'FAMILY', 'CAREGIVER'))
);

create table if not exists password_reset_tokens (
  id uuid primary key,
  user_id uuid not null references users(id),
  token_hash varchar(64) not null,
  expires_at timestamp with time zone not null,
  used_at timestamp with time zone,
  created_at timestamp with time zone not null,
  constraint uk_password_reset_tokens_token_hash unique (token_hash)
);

create index if not exists idx_password_reset_tokens_user_id
  on password_reset_tokens(user_id);
