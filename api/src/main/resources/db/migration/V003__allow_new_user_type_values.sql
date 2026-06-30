do $$
declare
  constraint_record record;
begin
  for constraint_record in
    select ccu.constraint_name
    from information_schema.constraint_column_usage ccu
    join information_schema.table_constraints tc
      on tc.constraint_schema = ccu.constraint_schema
     and tc.constraint_name = ccu.constraint_name
    where ccu.constraint_schema = current_schema()
      and ccu.table_name = 'users'
      and ccu.column_name = 'user_type'
      and tc.constraint_type = 'CHECK'
  loop
    execute format('alter table users drop constraint if exists %I', constraint_record.constraint_name);
  end loop;
end $$;

update users
set user_type = 'RESPONSAVEL'
where user_type = 'FAMILY';

update users
set user_type = 'CUIDADOR'
where user_type = 'CAREGIVER';

alter table users
  add constraint ck_users_user_type
  check (user_type in ('RESPONSAVEL', 'CUIDADOR', 'ADMIN', 'FAMILY', 'CAREGIVER'));
