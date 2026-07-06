alter table if exists caregiver_profiles
  add column if not exists tempo_experiencia varchar(30);

create table if not exists caregiver_formations (
  caregiver_profile_id uuid not null references caregiver_profiles(id),
  formacao varchar(40) not null,
  constraint uk_caregiver_formations unique (caregiver_profile_id, formacao)
);

insert into caregiver_formations (caregiver_profile_id, formacao)
select id, formacao
from caregiver_profiles
where formacao is not null
on conflict do nothing;
