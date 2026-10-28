-- V4: add id as a tie breaker to the feed index.
-- Keyset pagination uses (occurred_at, id) as the cursor, so two rows with the
-- same occurred_at are never skipped or shown twice. V3 is already applied, so
-- the change goes in a new migration instead of editing V3.

DROP INDEX activity_log_room_time_idx;

CREATE INDEX activity_log_room_time_idx
  ON activity_log (room_id, occurred_at DESC, id DESC);
