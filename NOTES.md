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