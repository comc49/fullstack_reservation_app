-- V3: speeds up "latest activity for one room".
-- Sorted by room first, then newest first within each room, so Postgres can
-- jump to one room's entries and read the newest ones in order, with no sort.
--
-- Measured on 1,000,072 rows (latest 20 for one room):
--   before: Seq Scan, about 10,500 pages read, about 70 ms
--   after:  Index Scan, about 26 pages read, about 0.2 ms

CREATE INDEX activity_log_room_time_idx
  ON activity_log (room_id, occurred_at DESC);
