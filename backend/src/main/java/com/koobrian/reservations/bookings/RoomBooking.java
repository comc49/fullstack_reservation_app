package com.koobrian.reservations.bookings;

import java.time.OffsetDateTime;

// One booked room in a time window: one row of the availability screen.
// A booking using two rooms appears as two RoomBookings.
public record RoomBooking(
    long roomId,
    String roomName,
    long bookingId,
    String title,
    OffsetDateTime startsAt,
    OffsetDateTime endsAt
) {
}