package com.koobrian.reservations.bookings;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BookingRepository {

    private final JdbcClient jdbcClient;

    public BookingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Every booked room that overlaps [from, to), skipping cancelled bookings.
    public List<RoomBooking> findInWindow(OffsetDateTime from, OffsetDateTime to) {
        return jdbcClient
            .sql("""
                SELECT 
                    r.id AS room_id,
                    r.name AS room_name,
                    b.id AS booking_id,
                    b.title AS title,
                    lower(br.during) AS starts_at,
                    upper(br.during) AS ends_at
                FROM booking_rooms AS br
                JOIN bookings AS b ON b.id = br.booking_id
                JOIN rooms AS r ON r.id = br.room_id
                WHERE br.during && tstzrange(:from, :to, '[)')
                    AND b.cancelled_at IS NULL
                ORDER BY r.name, lower(br.during)
                """)
            .param("from", from)  // blank 2: fill :from and :to
            .param("to", to)  
            .query((resultSet, rowNumber) -> new RoomBooking(
                resultSet.getLong("room_id"),
                resultSet.getString("room_name"),
                resultSet.getLong("booking_id"),
                resultSet.getString("title"),
                resultSet.getObject("starts_at", OffsetDateTime.class),
                resultSet.getObject("ends_at", OffsetDateTime.class) // blank 3: six values, in the record's order
            ))
            .list();
    }
}