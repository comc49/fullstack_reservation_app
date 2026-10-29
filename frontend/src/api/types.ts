// The shapes the Spring Boot API returns.
// They mirror the Java records: Room, ActivityItem, ActivityPage.

export interface Room {
  id: number
  name: string
}

export interface ActivityItem {
  id: number
  occurredAt: string        // ISO 8601 in UTC, for example "2026-10-29T16:33:00.248Z"
  action: string            // "booking_created", "booking_changed", ...
  bookingTitle: string | null
}

export interface ActivityPage {
  items: ActivityItem[]
  nextCursor: string | null // null on the last page
}
