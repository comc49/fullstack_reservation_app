# Database ERD

Generated from `backend/src/main/resources/db/migration/V1__init.sql`. Update this file whenever a migration changes the schema.

```mermaid
erDiagram
    people |o--o{ bookings : "requests"
    bookings ||--o{ booking_rooms : "uses"
    rooms ||--o{ booking_rooms : "is booked in"
    bookings ||--o{ booking_ministries : "is for"
    ministries ||--o{ booking_ministries : "has"
    ministries |o--o{ ministries : "parent of"

    people {
        bigint id PK
        text full_name "NOT NULL"
        text email UK "NOT NULL"
    }

    rooms {
        bigint id PK
        text name UK "NOT NULL"
    }

    ministries {
        bigint id PK
        text name UK "NOT NULL"
        bigint parent_id FK "NULL = top level; CHECK parent_id <> id"
    }

    bookings {
        bigint id PK
        bigint staging_id UK "sheet row it was imported from"
        text title "NOT NULL"
        bigint requested_by FK "NULL for imported rows"
        text legacy_contact_name "imported rows only"
        text legacy_contact_phone "imported rows only"
        timestamptz created_at "NOT NULL, default now()"
        timestamptz cancelled_at "NULL = active"
    }

    booking_rooms {
        bigint booking_id PK, FK "ON DELETE CASCADE"
        bigint room_id PK, FK
        tstzrange during "NOT NULL; CHECK start < end; EXCLUDE same room + overlapping time"
    }

    booking_ministries {
        bigint booking_id PK, FK "ON DELETE CASCADE"
        bigint ministry_id PK, FK
    }
```

## How to read the lines

| Symbol at a line's end | Meaning |
| --- | --- |
| `\|\|` | exactly one |
| `\|o` | zero or one |
| `o{` | zero or more |
| `\|{` | one or more |

Read each line from one table to the other. For example, `rooms ||--o{ booking_rooms`: each `booking_rooms` row points to exactly one room, and each room has zero or more `booking_rooms` rows.

## Rules the diagram can't show

- **No double booking:** `booking_rooms_no_overlap` rejects two rows with the same `room_id` and overlapping `during`. Ranges are half open (`[)`), so back to back bookings are allowed.
- **Cancelled bookings still block their room:** the exclusion constraint doesn't look at `bookings.cancelled_at` yet. Open decision: copy `cancelled_at` onto `booking_rooms`, or delete the room rows on cancel.
- **A booking with no rooms is allowed by the database.** Nothing forces at least one `booking_rooms` row, so the booking service must check it.
- **Short notice** (under 2 weeks) is derived from `created_at` and the start time, not stored.
