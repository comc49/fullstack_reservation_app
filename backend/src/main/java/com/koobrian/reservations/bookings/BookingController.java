package com.koobrian.reservations.bookings;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BookingController {

    // Longest window one request may ask for, so nobody can request ten years at once.
    private static final Duration MAX_WINDOW = Duration.ofDays(31);

    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // GET /v1/bookings?from=2026-10-30T00:00:00-07:00&to=2026-10-31T00:00:00-07:00
    @GetMapping("/v1/bookings")                                        // blank 1
    public List<RoomBooking> listBookingsInWindow(
        @RequestParam OffsetDateTime from,
        @RequestParam OffsetDateTime to
    ) {
        if (!from.isBefore(to)) {                                            // blank 2: window is empty or backwards
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'to' must be after 'from'");
        }

        if (Duration.between(from, to).compareTo(MAX_WINDOW) > 0) {                                            // blank 3: window longer than MAX_WINDOW
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Window can be at most 31 days");
        }

        return this.bookingRepository.findInWindow(from, to);
    }
}