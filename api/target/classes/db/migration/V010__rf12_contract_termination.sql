ALTER TABLE care_contracts ADD COLUMN termination_type VARCHAR(50);
ALTER TABLE care_contracts ADD COLUMN termination_reason VARCHAR(1000);
ALTER TABLE care_contracts ADD COLUMN termination_notes VARCHAR(1000);
ALTER TABLE care_contracts ADD COLUMN termination_requested_by_user_id UUID REFERENCES users(id);
ALTER TABLE care_contracts ADD COLUMN termination_requested_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE care_contracts ADD COLUMN effective_end_date DATE;
ALTER TABLE care_contracts ADD COLUMN canceled_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE care_contracts ADD COLUMN cancellation_requested_by_user_id UUID REFERENCES users(id);
ALTER TABLE care_contracts ADD COLUMN cancellation_requested_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE status_history ALTER COLUMN changed_by_user_id DROP NOT NULL;

CREATE INDEX idx_care_contracts_scheduled_termination
  ON care_contracts(status, effective_end_date)
  WHERE status = 'ENCERRAMENTO_AGENDADO';
