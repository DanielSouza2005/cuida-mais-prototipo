CREATE TABLE attendance_reports (
  id UUID PRIMARY KEY,
  contract_id UUID NOT NULL REFERENCES care_contracts(id) ON DELETE CASCADE,
  attendance_date DATE NOT NULL,
  attendance_start_record_id UUID NOT NULL REFERENCES service_attendance_records(id),
  attendance_end_record_id UUID NOT NULL REFERENCES service_attendance_records(id),
  caregiver_id UUID NOT NULL REFERENCES users(id),
  responsible_id UUID NOT NULL REFERENCES users(id),
  assisted_person_id UUID NOT NULL REFERENCES assisted_persons(id),
  generated_text TEXT NOT NULL,
  edited_text TEXT,
  final_text TEXT,
  additional_notes VARCHAR(4000),
  nursing_notes TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  email_status VARCHAR(20) NOT NULL DEFAULT 'NOT_SENT',
  email_sent_at TIMESTAMP WITH TIME ZONE,
  email_error_message VARCHAR(500),
  generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  edited_at TIMESTAMP WITH TIME ZONE,
  finalized_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT ck_attendance_report_status CHECK (status IN ('DRAFT', 'FINALIZED')),
  CONSTRAINT ck_attendance_report_email_status CHECK (email_status IN ('NOT_SENT', 'SENT', 'FAILED')),
  CONSTRAINT uk_attendance_report_contract_date UNIQUE (contract_id, attendance_date)
);

CREATE INDEX idx_attendance_reports_participants
  ON attendance_reports(responsible_id, caregiver_id, attendance_date);
