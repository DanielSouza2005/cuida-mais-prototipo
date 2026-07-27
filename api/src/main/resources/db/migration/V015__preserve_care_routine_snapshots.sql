DO $$
DECLARE
  existing_constraint_name TEXT;
BEGIN
  SELECT constraint_row.conname
    INTO existing_constraint_name
  FROM pg_constraint constraint_row
  JOIN pg_attribute column_row
    ON column_row.attrelid = constraint_row.conrelid
   AND column_row.attnum = ANY (constraint_row.conkey)
  WHERE constraint_row.conrelid = 'service_request_care_items_snapshot'::regclass
    AND constraint_row.contype = 'f'
    AND column_row.attname = 'original_care_routine_item_id'
  LIMIT 1;

  IF existing_constraint_name IS NOT NULL THEN
    EXECUTE format(
      'ALTER TABLE service_request_care_items_snapshot DROP CONSTRAINT %I',
      existing_constraint_name
    );
  END IF;
END $$;

ALTER TABLE service_request_care_items_snapshot
  ADD CONSTRAINT fk_request_care_snapshot_original_item
  FOREIGN KEY (original_care_routine_item_id)
  REFERENCES care_routine_items(id)
  ON DELETE SET NULL;

