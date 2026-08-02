CREATE TABLE user_notification_preferences (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  notification_type VARCHAR(64) NOT NULL,
  enabled BOOLEAN NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT uk_user_notification_preferences UNIQUE (user_id, notification_type)
);

CREATE INDEX idx_user_notification_preferences_user
  ON user_notification_preferences(user_id);
