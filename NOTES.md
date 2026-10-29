10/27/26
Ran this query
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id = 3
ORDER BY occurred_at DESC
LIMIT 20;

Got
 Planning Time: 1.398 ms
 Execution Time: 111.980 ms
 Pages read	10,503 plus sort

 After adding composite index, 
  Planning Time: 0.732 ms
 Execution Time: 1.801 ms
 Pages read 26, no sort

Why: without the index, Postgres reads every page, keeps about 200,000 rows for the room, sorts them, and returns 20.
With it, Postgres jumps to the room's section of the index, already newest first, and reads 20 entries.

Column order: equality columns first (`room_id`), then the sort or range column (`occurred_at`).
With time first, the room's entries are scattered through the whole timeline.

### 2. When Postgres skips an index (selectivity)

Around 20 of rows

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id = 3
ORDER BY occurred_at DESC, id DESC
LIMIT 20;

->  Index Scan using activity_log_room_time_idx on activity_log  (cost=0.42..48620.26 rows=202415 width=31) (actual time=0.203..0.285 rows=20 loops=1)
 Planning Time: 1.818 ms
 Execution Time: 3.352 ms


About 40% of rows

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id IN (1, 2);

Bitmap Heap Scan on activity_log  (cost=4233.72..19731.66 rows=399595 width=31) (actual time=31.977..114.445 rows=399735 loops=1)
 Planning Time: 1.178 ms
 Execution Time: 154.941 ms

About 80% of rows: four of five rooms

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id IN (1, 2, 3, 4);

Seq Scan on activity_log  (cost=0.00..25504.08 rows=799824 width=31) (actual time=0.063..135.681 rows=800351 loops=1)
 Planning Time: 0.657 ms
 Execution Time: 182.529 ms



Why: when most rows match, nearly every page has to be read anyway.
Reading the whole table in order is then cheaper than jumping between index and table.
The planner compares estimated costs (`random_page_cost` = 4, `seq_page_cost` = 1); there's no fixed cutoff.

### 3. Insert cost of indexes

Run: `docker compose exec -T db psql -U brian -d churchops < scripts/benchmark_inserts.sql`

INSERT INTO bench_log (id, occurred_at, action, room_id, details)
SELECT n, now() - random() * interval '3 years', 'booking_created', 1 + floor(random() * 5)::int, 'generated'
FROM generate_series(1, 100000) AS n;

Time: 179.838 ms


CREATE INDEX ON bench_log (id);
CREATE INDEX ON bench_log (room_id, occurred_at DESC);
CREATE INDEX ON bench_log (action);
CREATE INDEX ON bench_log (occurred_at);

-- Run 2: 4 indexes
INSERT INTO bench_log (id, occurred_at, action, room_id, details)
SELECT n, now() - random() * interval '3 years', 'booking_created', 1 + floor(random() * 5)::int, 'generated'
FROM generate_series(1, 100000) AS n;

Run 2 Time: 687.645 ms

Why: each insert adds one entry to every index, in sorted position, and logs each change.
Rule: add an index for a measured, frequent query, not "just in case."

### 4. Pagination: page 5,001 of one room's feed

```sql
-- OFFSET
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id = 3
ORDER BY occurred_at DESC, id DESC
LIMIT 20 OFFSET 100000;

-- Keyset: replace the cursor with the last row of the previous page.
-- To get a realistic cursor for this test:
--   SELECT occurred_at, id FROM activity_log WHERE room_id = 3
--   ORDER BY occurred_at DESC, id DESC OFFSET 99999 LIMIT 1;
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, occurred_at, action
FROM activity_log
WHERE room_id = 3
  AND (occurred_at, id) < ('<cursor time>', <cursor id>)
ORDER BY occurred_at DESC, id DESC
LIMIT 20;
```

| | Entries read | Pages read | Time |
| --- | --- | --- | --- |
| `OFFSET 100000` | 100,020 | ____ | ____ ms |
| Keyset | 20 | ____ | ____ ms |

Why: `OFFSET` counts rows, so Postgres reads and discards the first 100,000. Cost grows with page depth.
Keyset filters on the sort columns, so the index jumps straight to the cursor. Same cost at any depth.
Tradeoff: keyset only does next and previous, no "jump to page 4,000."

Tie breaker: `id` is part of the cursor and the index (V4).
With only `occurred_at < cursor`, a row sharing the cursor's timestamp would be skipped.

Naming: keyset pagination is the SQL technique; cursor pagination is the API contract
(`nextCursor` in the response, sent back on the next request).
