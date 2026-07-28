ALTER TABLE care_tasks
  ADD COLUMN duplicate_of_task_id UUID REFERENCES care_tasks(id);

CREATE INDEX idx_care_tasks_duplicate_of ON care_tasks(duplicate_of_task_id);

-- Vincula séries legadas sem snapshot à série provisionada equivalente do mesmo contrato.
WITH legacy_duplicates AS (
  SELECT legacy.id AS duplicate_id, canonical.id AS canonical_id
  FROM care_tasks legacy
  JOIN LATERAL (
    SELECT candidate.id
    FROM care_tasks candidate
    WHERE candidate.contract_id = legacy.contract_id
      AND candidate.source_snapshot_item_id IS NOT NULL
      AND candidate.title = legacy.title
      AND candidate.category = legacy.category
      AND COALESCE(candidate.custom_category, '') = COALESCE(legacy.custom_category, '')
      AND candidate.recurrence_type = legacy.recurrence_type
      AND candidate.scheduled_time = legacy.scheduled_time
    ORDER BY candidate.created_at, candidate.id
    LIMIT 1
  ) canonical ON TRUE
  WHERE legacy.source_snapshot_item_id IS NULL
    AND legacy.duplicate_of_task_id IS NULL
)
UPDATE care_tasks task
SET duplicate_of_task_id = duplicate.canonical_id,
    status = 'FINALIZADA',
    updated_at = CURRENT_TIMESTAMP
FROM legacy_duplicates duplicate
WHERE task.id = duplicate.duplicate_id;

UPDATE care_task_reminders reminder
SET status = 'CANCELED', canceled_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
WHERE reminder.status = 'SCHEDULED'
  AND reminder.occurrence_id IN (
    SELECT occurrence.id
    FROM task_occurrences occurrence
    JOIN care_tasks task ON task.id = occurrence.task_id
    WHERE task.duplicate_of_task_id IS NOT NULL
  );

UPDATE task_occurrences occurrence
SET status = 'CANCELADA', canceled_at = CURRENT_TIMESTAMP,
    status_updated_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
FROM care_tasks task
WHERE occurrence.task_id = task.id
  AND task.duplicate_of_task_id IS NOT NULL
  AND occurrence.status IN ('PENDENTE', 'ATRASADA');

-- A chave original já impede duas ocorrências para o mesmo item/data/horário.
-- Este índice explicita também o contrato na chave de negócio sem quebrar dados antigos.
CREATE UNIQUE INDEX uk_occurrence_contract_task_date_time
  ON task_occurrences(contract_id, task_id, scheduled_date, scheduled_time);
