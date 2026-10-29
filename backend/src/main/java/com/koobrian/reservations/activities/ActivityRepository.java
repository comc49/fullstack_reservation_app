package com.koobrian.reservations.activity;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {

    private final JdbcClient jdbcClient;

    public ActivityRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ActivityItem> findOlderThan(long roomId, ActivityCursor cursor, int limit) {
        return jdbcClient
            .sql("""
                SELECT a.id, a.occurred_at, a.action, b.title AS booking_title
                FROM activity_log AS a
                LEFT JOIN bookings AS b ON b.id = a.booking_id
                WHERE a.room_id = :roomId
                    AND (a.occurred_at, a.id) < (:cursorTime, :cursorId)
                ORDER BY a.occurred_at DESC, a.id DESC
                LIMIT :limit
                """)
            .param("roomId", roomId)
            .param("cursorTime", cursor.occurredAt())   // blank 2
            .param("cursorId", cursor.id())     // blank 3
            .param("limit", limit)
            .query((resultSet, rowNumber) -> new ActivityItem(
                resultSet.getLong("id"),
                resultSet.getObject("occurred_at", OffsetDateTime.class),
                resultSet.getString("action"),                    // blank 4: action
                resultSet.getString("booking_title") // blank 5: booking title
            ))
            .list();
    }
}