-- Everyday queries for room booking. Run one at a time in psql.

-- Q1: What's booked in the MPR during the week of Oct 26?
-- The week's edges are local times; daylight saving ends Nov 1.
SELECT
  b.title,
  lower(br.during) AT TIME ZONE 'America/Los_Angeles' AS starts_local,
  upper(br.during) AT TIME ZONE 'America/Los_Angeles' AS ends_local
FROM booking_rooms AS br
JOIN bookings AS b ON b.id = br.booking_id
JOIN rooms    AS r ON r.id = br.room_id
WHERE r.name = 'MPR'
  AND b.cancelled_at IS NULL
  AND br.during && tstzrange(
        '2026-10-26 00:00'::timestamp AT TIME ZONE 'America/Los_Angeles',
        '2026-11-02 00:00'::timestamp AT TIME ZONE 'America/Los_Angeles',
        '[)')
ORDER BY lower(br.during);

-- Q2: Is the Cafe free Friday Oct 30, 7 to 9 PM? Zero rows means free.
SELECT b.title, br.during
FROM booking_rooms AS br
JOIN bookings AS b ON b.id = br.booking_id
JOIN rooms    AS r ON r.id = br.room_id
WHERE r.name = 'Cafe'
  AND b.cancelled_at IS NULL
  AND br.during && tstzrange(
        '2026-10-30 19:00'::timestamp AT TIME ZONE 'America/Los_Angeles',
        '2026-10-30 21:00'::timestamp AT TIME ZONE 'America/Los_Angeles',
        '[)');

-- Q3: Each booking with its rooms and ministries on one line.
-- Subqueries, not joins: joining both tables multiplies rows (2 rooms x 2 ministries = 4).
SELECT
  b.id,
  b.title,
  (SELECT string_agg(r.name, ', ' ORDER BY r.name)
     FROM booking_rooms AS br
     JOIN rooms AS r ON r.id = br.room_id
    WHERE br.booking_id = b.id) AS rooms,
  (SELECT string_agg(m.name, ', ' ORDER BY m.name)
     FROM booking_ministries AS bm
     JOIN ministries AS m ON m.id = bm.ministry_id
    WHERE bm.booking_id = b.id) AS ministries
FROM bookings AS b
WHERE b.title = 'AYM Monthly'
ORDER BY b.id;

-- Q4: Which rooms are used most?
SELECT
  r.name,
  count(*) AS bookings,
  round(sum(extract(epoch FROM upper(br.during) - lower(br.during))) / 3600, 1) AS hours
FROM booking_rooms AS br
JOIN rooms AS r ON r.id = br.room_id
GROUP BY r.name
ORDER BY hours DESC;

-- Q5: Every College booking, including its subgroups (recursive CTE).
WITH RECURSIVE college_tree AS (
  SELECT id FROM ministries WHERE name = 'College'
  UNION ALL
  SELECT m.id FROM ministries AS m JOIN college_tree AS t ON m.parent_id = t.id
)
SELECT DISTINCT b.id, b.title
FROM bookings AS b
JOIN booking_ministries AS bm ON bm.booking_id = b.id
WHERE bm.ministry_id IN (SELECT id FROM college_tree)
ORDER BY b.id;

-- Q6: Bookings with no ministry.
-- Anti-join: LEFT JOIN keeps every booking, and a booking with no match
-- gets NULL on the right side. Use IS NULL; "= NULL" never matches.
SELECT b.id, b.title
FROM bookings AS b
LEFT JOIN booking_ministries AS bm ON bm.booking_id = b.id
WHERE bm.ministry_id IS NULL;

-- Q7: Busiest day of the week, counting events (not room uses).
-- Convert to Irvine time BEFORE extracting the day, or evening bookings
-- land on the next day in UTC. dow: 0 = Sunday ... 6 = Saturday.
-- Run order: FROM -> GROUP BY -> SELECT -> ORDER BY.
SELECT
  extract(dow FROM lower(br.during) AT TIME ZONE 'America/Los_Angeles') AS day_of_week,
  count(DISTINCT br.booking_id) AS bookings
FROM booking_rooms AS br
GROUP BY day_of_week
ORDER BY bookings DESC;

-- Q8: Ministries with more than 5 bookings.
-- HAVING filters groups after GROUP BY; WHERE filters rows before it.
-- HAVING runs before SELECT, so repeat the aggregate instead of using "bookings".
-- No DISTINCT: the primary key means each booking/ministry pair appears once.
SELECT
  m.name AS ministry,
  count(bm.booking_id) AS bookings
FROM booking_ministries AS bm
JOIN ministries AS m ON m.id = bm.ministry_id
GROUP BY m.name
HAVING count(bm.booking_id) > 5
ORDER BY bookings DESC;

-- Q9: The longest single booking.
-- upper - lower gives an interval. ORDER BY + LIMIT keeps the whole row;
-- max() alone can't tell you which booking it came from.
-- Always JOIN ... ON: a comma with no condition pairs every row with every row.
SELECT
  br.booking_id,
  b.title,
  upper(br.during) - lower(br.during) AS length
FROM booking_rooms AS br
JOIN bookings AS b ON b.id = br.booking_id
ORDER BY length DESC
LIMIT 1;

-- Q10: The next upcoming booking for each room.
-- ORDER BY groups each room's rows together, earliest first;
-- DISTINCT ON then keeps the first row per room.
-- Rooms with no upcoming bookings don't appear (WHERE removes all their rows).
SELECT DISTINCT ON (r.name)
  r.name AS room,
  b.title,
  lower(br.during) AT TIME ZONE 'America/Los_Angeles' AS starts_local
FROM booking_rooms AS br
JOIN bookings AS b ON b.id = br.booking_id
JOIN rooms    AS r ON r.id = br.room_id
WHERE lower(br.during) > now()
  AND b.cancelled_at IS NULL
ORDER BY r.name, lower(br.during);

-- Constraint test, inside a transaction that is rolled back, so nothing is kept.
-- The first insert should fail with "conflicting key value violates exclusion constraint".
BEGIN;
INSERT INTO bookings (title) VALUES ('Overlap test');
INSERT INTO booking_rooms (booking_id, room_id, during)
SELECT currval('bookings_id_seq'), r.id,
       tstzrange('2026-10-30 19:00-07', '2026-10-30 20:00-07', '[)')
FROM rooms AS r WHERE r.name = 'MPR';
ROLLBACK;
