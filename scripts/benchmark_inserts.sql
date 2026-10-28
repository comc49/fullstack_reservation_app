-- Measures how much indexes slow down inserts.
-- Inserts 100,000 rows into a scratch copy of activity_log, first with 0 indexes,
-- then with 4. Read the two "Time:" lines printed after each INSERT.
-- The scratch table is dropped at the end, so nothing is left behind.

\timing on

CREATE TABLE bench_log (LIKE activity_log INCLUDING DEFAULTS);

-- Run 1: 0 indexes
INSERT INTO bench_log (id, occurred_at, action, room_id, details)
SELECT n, now() - random() * interval '3 years', 'booking_created', 1 + floor(random() * 5)::int, 'generated'
FROM generate_series(1, 100000) AS n;

TRUNCATE bench_log;

CREATE INDEX ON bench_log (id);
CREATE INDEX ON bench_log (room_id, occurred_at DESC);
CREATE INDEX ON bench_log (action);
CREATE INDEX ON bench_log (occurred_at);

-- Run 2: 4 indexes
INSERT INTO bench_log (id, occurred_at, action, room_id, details)
SELECT n, now() - random() * interval '3 years', 'booking_created', 1 + floor(random() * 5)::int, 'generated'
FROM generate_series(1, 100000) AS n;

DROP TABLE bench_log;
