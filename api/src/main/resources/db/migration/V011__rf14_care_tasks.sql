CREATE TABLE care_tasks (
  id UUID PRIMARY KEY,
  title VARCHAR(140) NOT NULL,
  description VARCHAR(2000),
  category VARCHAR(40) NOT NULL,
  custom_category VARCHAR(120),
  priority VARCHAR(20) NOT NULL,
  recurrence_type VARCHAR(40) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  scheduled_time TIME NOT NULL,
  interval_days INTEGER,
  timezone VARCHAR(80) NOT NULL,
  reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  reminder_minutes_before INTEGER,
  notes VARCHAR(2000),
  status VARCHAR(20) NOT NULL,
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  contract_id UUID NOT NULL REFERENCES care_contracts(id),
  responsible_creator_id UUID NOT NULL REFERENCES users(id),
  caregiver_executor_id UUID NOT NULL REFERENCES users(id),
  previous_series_id UUID REFERENCES care_tasks(id),
  medication_name VARCHAR(180),
  medication_dosage VARCHAR(80),
  medication_unit VARCHAR(30),
  medication_custom_unit VARCHAR(80),
  medication_administration_route VARCHAR(30),
  medication_custom_route VARCHAR(120),
  medication_instructions VARCHAR(1000),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_by_user_id UUID NOT NULL REFERENCES users(id),
  updated_by_user_id UUID NOT NULL REFERENCES users(id),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_care_task_dates CHECK (end_date IS NULL OR end_date >= start_date),
  CONSTRAINT ck_care_task_interval CHECK (interval_days IS NULL OR interval_days > 0),
  CONSTRAINT ck_care_task_reminder CHECK (NOT reminder_enabled OR reminder_minutes_before IS NOT NULL AND reminder_minutes_before >= 0)
);

CREATE TABLE care_task_weekdays (
  task_id UUID NOT NULL REFERENCES care_tasks(id),
  weekday VARCHAR(20) NOT NULL,
  PRIMARY KEY (task_id, weekday)
);

CREATE TABLE task_occurrences (
  id UUID PRIMARY KEY,
  task_id UUID NOT NULL REFERENCES care_tasks(id),
  contract_id UUID NOT NULL REFERENCES care_contracts(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  caregiver_user_id UUID NOT NULL REFERENCES users(id),
  scheduled_date DATE NOT NULL,
  scheduled_time TIME NOT NULL,
  scheduled_instant_utc TIMESTAMP WITH TIME ZONE NOT NULL,
  timezone VARCHAR(80) NOT NULL,
  status VARCHAR(25) NOT NULL,
  completed_at TIMESTAMP WITH TIME ZONE,
  executed_by_user_id UUID REFERENCES users(id),
  non_completion_reason VARCHAR(1000),
  execution_note VARCHAR(1000),
  canceled_at TIMESTAMP WITH TIME ZONE,
  exception BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_task_occurrence_schedule UNIQUE (task_id, scheduled_date, scheduled_time)
);

CREATE TABLE care_activity_records (
  id UUID PRIMARY KEY,
  occurrence_id UUID NOT NULL UNIQUE REFERENCES task_occurrences(id),
  contract_id UUID NOT NULL REFERENCES care_contracts(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  responsible_user_id UUID NOT NULL REFERENCES users(id),
  caregiver_user_id UUID NOT NULL REFERENCES users(id),
  activity_type VARCHAR(40) NOT NULL,
  title VARCHAR(180) NOT NULL,
  notes VARCHAR(1000),
  occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE care_task_audit (
  id UUID PRIMARY KEY,
  task_id UUID NOT NULL REFERENCES care_tasks(id),
  occurrence_id UUID REFERENCES task_occurrences(id),
  actor_user_id UUID NOT NULL REFERENCES users(id),
  action VARCHAR(40) NOT NULL,
  details VARCHAR(500),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_care_tasks_responsible_status ON care_tasks(responsible_creator_id, status, updated_at DESC);
CREATE INDEX idx_care_tasks_caregiver_status ON care_tasks(caregiver_executor_id, status, updated_at DESC);
CREATE INDEX idx_care_tasks_assisted ON care_tasks(assisted_person_id);
CREATE INDEX idx_care_tasks_contract ON care_tasks(contract_id);
CREATE INDEX idx_occurrences_task_date ON task_occurrences(task_id, scheduled_date);
CREATE INDEX idx_occurrences_contract_date ON task_occurrences(contract_id, scheduled_date);
CREATE INDEX idx_occurrences_assisted_date ON task_occurrences(assisted_person_id, scheduled_date);
CREATE INDEX idx_occurrences_caregiver_date ON task_occurrences(caregiver_user_id, scheduled_date);
CREATE INDEX idx_occurrences_status_instant ON task_occurrences(status, scheduled_instant_utc);
CREATE INDEX idx_task_audit_task_date ON care_task_audit(task_id, created_at DESC);
