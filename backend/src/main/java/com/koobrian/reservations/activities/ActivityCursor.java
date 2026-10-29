package com.koobrian.reservations.activity;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

// Where the previous page stopped: the last shown row's occurred_at and id.
// The client only ever sees it as an opaque string (see encode and decode).
public record ActivityCursor(OffsetDateTime occurredAt, long id) {

    // First page: a position later than every real row ("the end of time").
    // Java has no 'infinity' timestamp, so a far future date plays that role.
    public static final ActivityCursor FIRST_PAGE = new ActivityCursor(
        OffsetDateTime.parse("9999-12-31T23:59:59Z"),
        Long.MAX_VALUE
    );

    // Builds the cursor that points just after this item.
    public static ActivityCursor after(ActivityItem lastShownItem) {
        return new ActivityCursor(lastShownItem.occurredAt(), lastShownItem.id());
    }

    // "2026-10-26T16:10:00Z|384210" -> URL safe base64, so it fits in ?cursor=...
    public String encode() {
        String plainText = occurredAt.toString() + "|" + id;
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // The reverse of encode(). Throws IllegalArgumentException for anything malformed.
    public static ActivityCursor decode(String encoded) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            String plainText = new String(bytes, StandardCharsets.UTF_8);

            String[] parts = plainText.split("\\|");
            if (parts.length != 2) {
                throw new IllegalArgumentException("cursor must have two parts");
            }

            OffsetDateTime occurredAt = OffsetDateTime.parse(parts[0]);
            long id = Long.parseLong(parts[1]);
            return new ActivityCursor(occurredAt, id);
        } catch (DateTimeParseException | NumberFormatException exception) {
            throw new IllegalArgumentException("invalid cursor", exception);
        }
    }
}
