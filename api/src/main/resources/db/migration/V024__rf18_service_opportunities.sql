ALTER TABLE assisted_persons
  ADD COLUMN allow_caregivers_to_find BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE service_requests
  ALTER COLUMN caregiver_user_id DROP NOT NULL;

ALTER TABLE service_requests
  ADD COLUMN initiated_by VARCHAR(20) NOT NULL DEFAULT 'RESPONSIBLE',
  ADD COLUMN requester_user_id UUID REFERENCES users(id),
  ADD COLUMN source_opportunity_id UUID REFERENCES service_requests(id);

UPDATE service_requests
SET requester_user_id = responsible_user_id
WHERE requester_user_id IS NULL;

ALTER TABLE service_requests
  ALTER COLUMN requester_user_id SET NOT NULL;

CREATE INDEX idx_service_requests_opportunities
  ON service_requests(status, initiated_by, created_at DESC);

CREATE INDEX idx_service_requests_source_caregiver
  ON service_requests(source_opportunity_id, caregiver_user_id);
