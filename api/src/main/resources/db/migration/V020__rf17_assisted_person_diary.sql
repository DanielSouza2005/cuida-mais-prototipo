ALTER TABLE care_activity_records
  ALTER COLUMN occurrence_id DROP NOT NULL,
  ADD COLUMN source_type VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
  ADD COLUMN entry_date DATE,
  ADD COLUMN timezone VARCHAR(80),
  ADD COLUMN care_type VARCHAR(40),
  ADD COLUMN description VARCHAR(2000),
  ADD COLUMN important BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN created_by_user_id UUID REFERENCES users(id);

UPDATE care_activity_records record
SET entry_date = occurrence.scheduled_date,
    timezone = occurrence.timezone,
    care_type = task.category,
    created_by_user_id = record.caregiver_user_id
FROM task_occurrences occurrence
JOIN care_tasks task ON task.id = occurrence.task_id
WHERE record.occurrence_id = occurrence.id;

ALTER TABLE care_activity_records
  ALTER COLUMN entry_date SET NOT NULL,
  ALTER COLUMN timezone SET NOT NULL,
  ALTER COLUMN care_type SET NOT NULL,
  ALTER COLUMN created_by_user_id SET NOT NULL;

CREATE INDEX idx_care_activity_caregiver_date
  ON care_activity_records(caregiver_user_id, entry_date, occurred_at);

CREATE INDEX idx_care_activity_responsible_date
  ON care_activity_records(responsible_user_id, entry_date, occurred_at);

CREATE INDEX idx_care_activity_contract_date
  ON care_activity_records(contract_id, entry_date, occurred_at);

ALTER TABLE care_occurrence_photos
  ALTER COLUMN occurrence_id DROP NOT NULL,
  ADD COLUMN activity_record_id UUID REFERENCES care_activity_records(id) ON DELETE CASCADE,
  ADD CONSTRAINT ck_care_photo_single_parent CHECK (
    (occurrence_id IS NOT NULL AND activity_record_id IS NULL)
    OR (occurrence_id IS NULL AND activity_record_id IS NOT NULL)
  );

CREATE INDEX idx_care_occurrence_photos_activity
  ON care_occurrence_photos(activity_record_id, created_at);
