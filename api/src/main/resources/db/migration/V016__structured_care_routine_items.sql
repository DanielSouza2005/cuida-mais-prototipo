ALTER TABLE care_routine_items
  ADD COLUMN category VARCHAR(40),
  ADD COLUMN custom_category VARCHAR(120),
  ADD COLUMN priority VARCHAR(20),
  ADD COLUMN recurrence_type VARCHAR(40),
  ADD COLUMN scheduled_time TIME,
  ADD COLUMN interval_days INTEGER,
  ADD COLUMN reminder_enabled BOOLEAN,
  ADD COLUMN reminder_minutes_before INTEGER,
  ADD COLUMN notes VARCHAR(2000),
  ADD COLUMN medication_name VARCHAR(180),
  ADD COLUMN medication_dosage VARCHAR(80),
  ADD COLUMN medication_unit VARCHAR(30),
  ADD COLUMN medication_custom_unit VARCHAR(80),
  ADD COLUMN medication_administration_route VARCHAR(30),
  ADD COLUMN medication_custom_route VARCHAR(120),
  ADD COLUMN medication_instructions VARCHAR(1000);

CREATE TABLE care_routine_item_weekdays (
  care_routine_item_id UUID NOT NULL REFERENCES care_routine_items(id) ON DELETE CASCADE,
  weekday VARCHAR(20) NOT NULL,
  PRIMARY KEY (care_routine_item_id, weekday)
);

ALTER TABLE service_request_care_items_snapshot
  ADD COLUMN category VARCHAR(40),
  ADD COLUMN custom_category VARCHAR(120),
  ADD COLUMN priority VARCHAR(20),
  ADD COLUMN recurrence_type VARCHAR(40),
  ADD COLUMN scheduled_time TIME,
  ADD COLUMN interval_days INTEGER,
  ADD COLUMN reminder_enabled BOOLEAN,
  ADD COLUMN reminder_minutes_before INTEGER,
  ADD COLUMN notes VARCHAR(2000),
  ADD COLUMN medication_name VARCHAR(180),
  ADD COLUMN medication_dosage VARCHAR(80),
  ADD COLUMN medication_unit VARCHAR(30),
  ADD COLUMN medication_custom_unit VARCHAR(80),
  ADD COLUMN medication_administration_route VARCHAR(30),
  ADD COLUMN medication_custom_route VARCHAR(120),
  ADD COLUMN medication_instructions VARCHAR(1000);

CREATE TABLE service_request_care_snapshot_weekdays (
  snapshot_item_id UUID NOT NULL REFERENCES service_request_care_items_snapshot(id) ON DELETE CASCADE,
  weekday VARCHAR(20) NOT NULL,
  PRIMARY KEY (snapshot_item_id, weekday)
);
