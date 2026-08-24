CREATE TABLE service_attendance_records (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL REFERENCES care_contracts(id) ON DELETE CASCADE,
  caregiver_id UUID NOT NULL REFERENCES users(id),
  responsible_id UUID NOT NULL REFERENCES users(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  attendance_date DATE NOT NULL,
  record_type VARCHAR(10) NOT NULL,
  recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  accuracy DOUBLE PRECISION NOT NULL,
  location_captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
  address_snapshot VARCHAR(500),
  device_timezone VARCHAR(80) NOT NULL,
  scheduled_start_time TIME NOT NULL,
  scheduled_end_time TIME NOT NULL,
  allowed_window_start TIMESTAMP WITH TIME ZONE NOT NULL,
  allowed_window_end TIMESTAMP WITH TIME ZONE NOT NULL,
  within_allowed_window BOOLEAN NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT ck_service_attendance_type CHECK (record_type IN ('START', 'END')),
  CONSTRAINT ck_service_attendance_latitude CHECK (latitude BETWEEN -90 AND 90),
  CONSTRAINT ck_service_attendance_longitude CHECK (longitude BETWEEN -180 AND 180),
  CONSTRAINT ck_service_attendance_accuracy CHECK (accuracy >= 0 AND accuracy <= 1000),
  CONSTRAINT uk_service_attendance_contract_date_type UNIQUE (contract_id, attendance_date, record_type)
);

CREATE INDEX idx_service_attendance_contract_date
  ON service_attendance_records(contract_id, attendance_date, recorded_at);

CREATE INDEX idx_service_attendance_caregiver_date
  ON service_attendance_records(caregiver_id, attendance_date);
