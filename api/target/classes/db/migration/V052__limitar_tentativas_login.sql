ALTER TABLE usuario
  ADD COLUMN tentativas_login_invalidas INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN login_bloqueado_ate TIMESTAMP WITH TIME ZONE;

ALTER TABLE usuario
  ADD CONSTRAINT ck_usuario_tentativas_login_invalidas
    CHECK (tentativas_login_invalidas BETWEEN 0 AND 5);
