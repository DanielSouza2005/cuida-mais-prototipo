CREATE TABLE care_routines (
  id UUID PRIMARY KEY,
  responsible_user_id UUID NOT NULL REFERENCES users(id),
  assisted_person_id UUID REFERENCES assisted_persons(id),
  name VARCHAR(140) NOT NULL,
  description VARCHAR(1000),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE care_routine_items (
  id UUID PRIMARY KEY,
  care_routine_id UUID NOT NULL REFERENCES care_routines(id) ON DELETE CASCADE,
  title VARCHAR(140) NOT NULL,
  description VARCHAR(1000),
  sort_order INTEGER NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE service_requests
  ADD COLUMN care_routine_id UUID REFERENCES care_routines(id),
  ADD COLUMN care_routine_name_snapshot VARCHAR(140);

CREATE TABLE service_request_care_items_snapshot (
  id UUID PRIMARY KEY,
  service_request_id UUID NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
  original_care_routine_id UUID NOT NULL REFERENCES care_routines(id),
  original_care_routine_item_id UUID REFERENCES care_routine_items(id),
  title VARCHAR(140) NOT NULL,
  description VARCHAR(1000),
  sort_order INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_care_routines_owner_updated ON care_routines(responsible_user_id, updated_at DESC);
CREATE INDEX idx_care_routines_assisted_active ON care_routines(assisted_person_id, active);
CREATE INDEX idx_care_routine_items_routine_order ON care_routine_items(care_routine_id, sort_order);
CREATE INDEX idx_request_care_snapshot_request_order ON service_request_care_items_snapshot(service_request_id, sort_order);

