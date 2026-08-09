CREATE TABLE user_push_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  expo_push_token VARCHAR(255) NOT NULL,
  platform VARCHAR(20) NOT NULL,
  device_id VARCHAR(180),
  app_version VARCHAR(40),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  last_used_at TIMESTAMP WITH TIME ZONE,
  disabled_at TIMESTAMP WITH TIME ZONE,
  CONSTRAINT fk_user_push_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uk_user_push_tokens_expo_token UNIQUE (expo_push_token)
);

CREATE INDEX idx_user_push_tokens_active_user ON user_push_tokens (user_id) WHERE active = TRUE;
