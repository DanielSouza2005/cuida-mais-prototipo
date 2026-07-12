CREATE TABLE service_requests (
  id UUID PRIMARY KEY,
  responsible_user_id UUID NOT NULL REFERENCES users(id),
  caregiver_user_id UUID NOT NULL REFERENCES users(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  hiring_type VARCHAR(40) NOT NULL,
  status VARCHAR(30) NOT NULL,
  start_date DATE,
  end_date DATE,
  needs_description VARCHAR(2000) NOT NULL,
  activity_other VARCHAR(500),
  additional_notes VARCHAR(2000),
  negotiation_notes VARCHAR(1000),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  canceled_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE service_request_dates (
  service_request_id UUID NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  service_date DATE NOT NULL,
  PRIMARY KEY (service_request_id, service_date)
);

CREATE TABLE service_request_schedule_days (
  service_request_id UUID NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  weekday VARCHAR(20) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  PRIMARY KEY (service_request_id, weekday)
);

CREATE TABLE service_request_activities (
  service_request_id UUID NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  activity VARCHAR(50) NOT NULL,
  PRIMARY KEY (service_request_id, activity)
);

CREATE INDEX idx_service_requests_owner_status ON service_requests(responsible_user_id, status);
CREATE INDEX idx_service_requests_duplicate ON service_requests(responsible_user_id, caregiver_user_id, assisted_person_id, status);
