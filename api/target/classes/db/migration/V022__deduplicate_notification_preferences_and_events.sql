CREATE TEMP TABLE notification_preference_winners ON COMMIT DROP AS
SELECT DISTINCT ON (user_id, canonical_type)
  id,
  user_id,
  canonical_type
FROM (
  SELECT
    id,
    user_id,
    CASE notification_type
      WHEN 'TASK_OCCURRENCE_COMPLETED' THEN 'CARE_OCCURRENCE_COMPLETED'
      WHEN 'TASK_OCCURRENCE_NOT_COMPLETED' THEN 'CARE_OCCURRENCE_NOT_DONE'
      WHEN 'CARE_TASK_REMINDER' THEN 'CARE_OCCURRENCE_REMINDER'
      WHEN 'CARE_TASK_OVERDUE' THEN 'CARE_OCCURRENCE_OVERDUE'
      WHEN 'CARE_TASK_NOT_DONE' THEN 'CARE_OCCURRENCE_NOT_DONE'
      WHEN 'CARE_TASK_RESPONSIBLE_ALERT' THEN 'CARE_OCCURRENCE_RESPONSIBLE_ALERT'
      ELSE notification_type
    END AS canonical_type,
    updated_at,
    created_at
  FROM user_notification_preferences
  WHERE notification_type IN (
    'TASK_OCCURRENCE_COMPLETED', 'TASK_OCCURRENCE_NOT_COMPLETED',
    'CARE_TASK_REMINDER', 'CARE_TASK_OVERDUE', 'CARE_TASK_NOT_DONE', 'CARE_TASK_RESPONSIBLE_ALERT',
    'CARE_OCCURRENCE_COMPLETED', 'CARE_OCCURRENCE_NOT_DONE',
    'CARE_OCCURRENCE_REMINDER', 'CARE_OCCURRENCE_OVERDUE', 'CARE_OCCURRENCE_RESPONSIBLE_ALERT'
  )
) mapped
ORDER BY user_id, canonical_type, updated_at DESC, created_at DESC, id DESC;

DELETE FROM user_notification_preferences preference
USING notification_preference_winners winner
WHERE preference.user_id = winner.user_id
  AND CASE preference.notification_type
    WHEN 'TASK_OCCURRENCE_COMPLETED' THEN 'CARE_OCCURRENCE_COMPLETED'
    WHEN 'TASK_OCCURRENCE_NOT_COMPLETED' THEN 'CARE_OCCURRENCE_NOT_DONE'
    WHEN 'CARE_TASK_REMINDER' THEN 'CARE_OCCURRENCE_REMINDER'
    WHEN 'CARE_TASK_OVERDUE' THEN 'CARE_OCCURRENCE_OVERDUE'
    WHEN 'CARE_TASK_NOT_DONE' THEN 'CARE_OCCURRENCE_NOT_DONE'
    WHEN 'CARE_TASK_RESPONSIBLE_ALERT' THEN 'CARE_OCCURRENCE_RESPONSIBLE_ALERT'
    ELSE preference.notification_type
  END = winner.canonical_type
  AND preference.id <> winner.id;

UPDATE user_notification_preferences preference
SET notification_type = winner.canonical_type
FROM notification_preference_winners winner
WHERE preference.id = winner.id
  AND preference.notification_type <> winner.canonical_type;

ALTER TABLE notifications
  ADD COLUMN deduplication_key VARCHAR(220);

CREATE UNIQUE INDEX ux_notifications_deduplication_key
  ON notifications(deduplication_key)
  WHERE deduplication_key IS NOT NULL;
