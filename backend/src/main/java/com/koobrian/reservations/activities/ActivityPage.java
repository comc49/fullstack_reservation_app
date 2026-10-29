package com.koobrian.reservations.activity;

import java.util.List;

// One page of the feed: up to PAGE_SIZE items, plus the cursor for the next page.
// nextCursor is null on the last page.
public record ActivityPage(
    List<ActivityItem> items,
    String nextCursor
) {
}
