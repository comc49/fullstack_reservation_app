-- Fills activity_log with realistic history plus generated rows, for index practice.
-- Safe to commit: it only uses data already in your tables, no real contact details.
-- Run AFTER the import. Takes about 10 to 30 seconds.
\set ON_ERROR_STOP on
\timing on

BEGIN;

-- 1. Real history: one 'booking_created' row per imported booking room.
INSERT INTO activity_log (occurred_at, action, room_id, booking_id, details)
SELECT
  lower(br.during) - interval '14 days',     -- pretend each was booked 2 weeks ahead
  'booking_created',
  br.room_id,
  br.booking_id,
  'imported from the reservation sheet'
FROM booking_rooms AS br;

-- 2. Generated history: 1,000,000 rows spread over the last 3 years.
--    random() picks a room, an action, and a moment for each row.
--    The room ids are collected once into an array (room_ids), then each row
--    picks a random position in it. A subquery like (SELECT id FROM rooms ...
--    random() ...) would run only once and give every row the same room.
INSERT INTO activity_log (occurred_at, action, room_id, details)
SELECT
  now() - random() * interval '3 years',
  (ARRAY['booking_created', 'booking_created', 'booking_created',
         'booking_changed', 'booking_cancelled', 'room_checked'])[1 + floor(random() * 6)::int],
  r.room_ids[1 + floor(random() * cardinality(r.room_ids))::int],
  'generated'
FROM generate_series(1, 1000000) AS n
CROSS JOIN (SELECT array_agg(id ORDER BY id) AS room_ids FROM rooms) AS r;

COMMIT;

-- 3. Refresh the planner's statistics so EXPLAIN reflects the new data.
ANALYZE activity_log;

SELECT count(*) AS total_rows FROM activity_log;
SELECT r.name, count(*) FROM activity_log AS a JOIN rooms AS r ON r.id = a.room_id GROUP BY r.name ORDER BY r.name;
