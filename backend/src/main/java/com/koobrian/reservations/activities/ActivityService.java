package com.koobrian.reservations.activity;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    static final int PAGE_SIZE = 20;

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public ActivityPage getRoomActivity(long roomId, ActivityCursor cursor) {
        // 1. Ask the repository for one more row than a page holds.
        List<ActivityItem> rows = activityRepository.findOlderThan(roomId, cursor, PAGE_SIZE+1);   // blank 1

        // 2. Did the extra row come back?
        boolean hasNextPage = rows.size() > PAGE_SIZE;

        if (!hasNextPage) {
            // 3. Last page: return every row, and no cursor.
            return new ActivityPage(rows, null);                                         // blank 3
        }

        // 4. Keep only the first PAGE_SIZE rows. The extra row is never sent.
        List<ActivityItem> pageItems = rows.subList(0, PAGE_SIZE);

        // 5. The next page starts after the last row the user actually sees.
        ActivityItem lastShownItem = pageItems.get(PAGE_SIZE - 1);
        String nextCursor = ActivityCursor.after(lastShownItem).encode();

        return new ActivityPage(List.copyOf(pageItems), nextCursor);
    }
}