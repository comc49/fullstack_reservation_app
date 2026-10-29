package com.koobrian.reservations.activity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // GET /v1/rooms/3/activity                  -> first page
    // GET /v1/rooms/3/activity?cursor=MjAy...   -> next page
    @GetMapping("/v1/rooms/{roomId}/activity")                                         // blank 1: the URL pattern
    public ActivityPage getRoomActivity(
        @PathVariable long roomId,
        @RequestParam(required = false) String cursor
    ) {
        ActivityCursor position;

        if (cursor == null || cursor.isBlank()) {
            position = ActivityCursor.FIRST_PAGE;
        } else {
            try {
                position = ActivityCursor.decode(cursor);
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor");   // blank 4
            }
        }

        return activityService.getRoomActivity(roomId, position);                                            // blank 5
    }
}