CREATE UNIQUE INDEX uq_service_requests_source_caregiver
  ON service_requests(source_opportunity_id, caregiver_user_id)
  WHERE source_opportunity_id IS NOT NULL;
