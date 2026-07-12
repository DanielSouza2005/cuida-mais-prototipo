ALTER TABLE caregiver_profiles ADD COLUMN latitude NUMERIC(10, 7);
ALTER TABLE caregiver_profiles ADD COLUMN longitude NUMERIC(10, 7);

ALTER TABLE assisted_persons ADD COLUMN latitude NUMERIC(10, 7);
ALTER TABLE assisted_persons ADD COLUMN longitude NUMERIC(10, 7);
