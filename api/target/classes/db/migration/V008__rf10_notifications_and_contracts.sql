ALTER TABLE service_requests ADD COLUMN rejection_reason VARCHAR(1000);

CREATE TABLE notifications (
  id UUID PRIMARY KEY, recipient_user_id UUID NOT NULL REFERENCES users(id), type VARCHAR(50) NOT NULL,
  title VARCHAR(180) NOT NULL, message VARCHAR(500) NOT NULL, related_entity_type VARCHAR(40) NOT NULL,
  related_entity_id UUID NOT NULL, read_at TIMESTAMP WITH TIME ZONE, cleared_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id, cleared_at, created_at DESC);

CREATE TABLE care_contracts (
  id UUID PRIMARY KEY, service_request_id UUID NOT NULL UNIQUE REFERENCES service_requests(id),
  responsible_user_id UUID NOT NULL REFERENCES users(id), caregiver_user_id UUID NOT NULL REFERENCES users(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id), status VARCHAR(30) NOT NULL,
  start_date DATE NOT NULL, end_date DATE, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_care_contracts_caregiver_status ON care_contracts(caregiver_user_id, status);
