package com.koobrian.reservations.activity;

import java.time.OffsetDateTime;

// One row in a room's activity feed.
// bookingTitle is null for rows with no booking (the generated test rows).
public record ActivityItem(
    long id,
    OffsetDateTime occurredAt,
    String action,
    String bookingTitle
) {
}
