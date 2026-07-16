ALTER TABLE service_requests ADD COLUMN cancellation_reason VARCHAR(1000);
ALTER TABLE care_contracts ADD COLUMN cancellation_reason VARCHAR(1000);
ALTER TABLE care_contracts ADD COLUMN closure_reason VARCHAR(1000);

CREATE TABLE status_history (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(40) NOT NULL,
  entity_id UUID NOT NULL,
  previous_status VARCHAR(30),
  new_status VARCHAR(30) NOT NULL,
  changed_by_user_id UUID NOT NULL REFERENCES users(id),
  reason VARCHAR(1000),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_status_history_entity ON status_history(entity_type, entity_id, created_at);
CREATE INDEX idx_care_contracts_responsible_updated ON care_contracts(responsible_user_id, updated_at DESC);
CREATE INDEX idx_service_requests_responsible_updated ON service_requests(responsible_user_id, updated_at DESC);

INSERT INTO status_history (id, entity_type, entity_id, previous_status, new_status, changed_by_user_id, reason, created_at)
SELECT gen_random_uuid(), 'SERVICE_REQUEST', id, NULL, status, responsible_user_id,
       CASE WHEN status = 'REJEITADA' THEN rejection_reason WHEN status = 'CANCELADA' THEN cancellation_reason ELSE NULL END,
       COALESCE(updated_at, created_at)
FROM service_requests;

INSERT INTO status_history (id, entity_type, entity_id, previous_status, new_status, changed_by_user_id, reason, created_at)
SELECT gen_random_uuid(), 'CARE_CONTRACT', id, NULL, status, responsible_user_id,
       CASE WHEN status = 'CANCELADA' THEN cancellation_reason WHEN status = 'FINALIZADA' THEN closure_reason ELSE NULL END,
       COALESCE(updated_at, created_at)
FROM care_contracts;
