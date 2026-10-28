-- V2: an append only history of what happened in the app.
-- Every booking, cancellation, and later every maintenance and rotation change,
-- writes one row here. Rows are never updated or deleted.

CREATE TABLE activity_log (
  id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  occurred_at  timestamptz NOT NULL DEFAULT now(),
  action       text NOT NULL,                          -- e.g. 'booking_created'
  room_id      bigint REFERENCES rooms (id),           -- NULL when not about a room
  booking_id   bigint REFERENCES bookings (id),        -- NULL when not about a booking
  actor_id     bigint REFERENCES people (id),          -- NULL for imported or system rows
  details      text,
  CONSTRAINT activity_log_known_action CHECK (
    action IN ('booking_created', 'booking_cancelled', 'booking_changed', 'room_checked')
  )
);

-- No indexes yet on purpose: Week 2 measures the query first, then adds one.
