ALTER TABLE care_routine_items
  ADD COLUMN reminder_at_scheduled_time BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN overdue_reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN overdue_after_minutes INTEGER,
  ADD COLUMN repeat_while_pending BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN repeat_interval_minutes INTEGER,
  ADD COLUMN important BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN notify_responsible_if_important BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE service_request_care_items_snapshot
  ADD COLUMN reminder_at_scheduled_time BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN overdue_reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN overdue_after_minutes INTEGER,
  ADD COLUMN repeat_while_pending BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN repeat_interval_minutes INTEGER,
  ADD COLUMN important BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN notify_responsible_if_important BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE care_tasks
  ADD COLUMN source_snapshot_item_id UUID REFERENCES service_request_care_items_snapshot(id),
  ADD COLUMN reminder_at_scheduled_time BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN overdue_reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN overdue_after_minutes INTEGER,
  ADD COLUMN repeat_while_pending BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN repeat_interval_minutes INTEGER,
  ADD COLUMN important BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN notify_responsible_if_important BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uk_care_tasks_source_snapshot ON care_tasks(source_snapshot_item_id) WHERE source_snapshot_item_id IS NOT NULL;

CREATE TABLE care_task_reminders (
  id UUID PRIMARY KEY,
  occurrence_id UUID NOT NULL REFERENCES task_occurrences(id),
  recipient_user_id UUID NOT NULL REFERENCES users(id),
  reminder_type VARCHAR(40) NOT NULL,
  scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
  sent_at TIMESTAMP WITH TIME ZONE,
  canceled_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(20) NOT NULL,
  deduplication_key VARCHAR(220) NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_task_reminders_due ON care_task_reminders(status, scheduled_at);
CREATE INDEX idx_task_reminders_occurrence ON care_task_reminders(occurrence_id, status);

