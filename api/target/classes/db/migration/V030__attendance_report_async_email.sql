ALTER TABLE attendance_reports
  ADD COLUMN email_requested_at TIMESTAMP WITH TIME ZONE,
  ADD COLUMN email_attempts INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN email_next_retry_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE attendance_reports DROP CONSTRAINT ck_attendance_report_email_status;

ALTER TABLE attendance_reports
  ADD CONSTRAINT ck_attendance_report_email_status
  CHECK (email_status IN ('NOT_SENT', 'PENDING', 'SENT', 'FAILED'));

CREATE INDEX idx_attendance_reports_email_delivery
  ON attendance_reports(email_status, email_next_retry_at, email_requested_at);
