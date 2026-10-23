-- V1: core tables for room booking.
-- Flyway runs this once, in order, before V2, V3, and so on.

-- Lets a GiST index compare room_id with = inside the exclusion constraint.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE people (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  full_name  text NOT NULL,
  email      text NOT NULL UNIQUE
);

CREATE TABLE rooms (
  id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name  text NOT NULL UNIQUE
);

CREATE TABLE ministries (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name       text NOT NULL UNIQUE,
  parent_id  bigint REFERENCES ministries (id),   -- NULL means a top level ministry
  CONSTRAINT ministries_not_own_parent CHECK (parent_id <> id)
);

CREATE TABLE bookings (
  id                    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  staging_id            bigint UNIQUE,                     -- the sheet row it was imported from
  title                 text NOT NULL,
  requested_by          bigint REFERENCES people (id),     -- filled for bookings made in the app
  legacy_contact_name   text,                              -- only for imported rows
  legacy_contact_phone  text,
  created_at            timestamptz NOT NULL DEFAULT now(),
  cancelled_at          timestamptz
);

CREATE TABLE booking_rooms (
  booking_id  bigint NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
  room_id     bigint NOT NULL REFERENCES rooms (id),
  during      tstzrange NOT NULL,
  PRIMARY KEY (booking_id, room_id),
  CONSTRAINT booking_rooms_has_length
    CHECK (lower(during) < upper(during)),
  -- Same room (=) and overlapping time (&&) is rejected.
  CONSTRAINT booking_rooms_no_overlap
    EXCLUDE USING gist (room_id WITH =, during WITH &&)
);

CREATE TABLE booking_ministries (
  booking_id   bigint NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
  ministry_id  bigint NOT NULL REFERENCES ministries (id),
  PRIMARY KEY (booking_id, ministry_id)
);
