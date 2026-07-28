ALTER TABLE care_tasks
  ADD COLUMN requires_completion_photo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE care_routine_items
  ADD COLUMN requires_completion_photo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE service_request_care_items_snapshot
  ADD COLUMN requires_completion_photo BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE task_occurrences
  ADD COLUMN auto_marked_not_done BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN status_updated_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE care_task_audit
  ALTER COLUMN actor_user_id DROP NOT NULL;

CREATE TABLE care_occurrence_photos (
  id UUID PRIMARY KEY,
  occurrence_id UUID NOT NULL REFERENCES task_occurrences(id) ON DELETE CASCADE,
  uploaded_by_user_id UUID NOT NULL REFERENCES users(id),
  file_name VARCHAR(80) NOT NULL UNIQUE,
  original_file_name VARCHAR(255),
  content_type VARCHAR(30) NOT NULL,
  file_size BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_care_occurrence_photos_occurrence
  ON care_occurrence_photos(occurrence_id, created_at);
