import { useState } from 'react'

import { useGetBookingsInWindowQuery, useGetRoomsQuery } from '../../api/apiSlice'
import type { RoomBooking } from '../../api/types'
import { addDays, dayWindow } from './dayWindow'

// The visible part of the day. Bookings outside it are clipped to the edges.
const FIRST_HOUR = 6        // 6 AM
const LAST_HOUR = 24        // midnight
const VISIBLE_MINUTES = (LAST_HOUR - FIRST_HOUR) * 60
const HOUR_MARKS = Array.from({ length: (LAST_HOUR - FIRST_HOUR) / 2 + 1 }, (_, i) => FIRST_HOUR + i * 2)

const DAY_LABEL = new Intl.DateTimeFormat('en-US', { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' })
const TIME_LABEL = new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: '2-digit' })

function hourLabel(hour: number): string {
  if (hour === 0 || hour === 24) return '12 AM'
  if (hour === 12) return '12 PM'
  return hour < 12 ? `${hour} AM` : `${hour - 12} PM`
}

// Minutes from FIRST_HOUR on the shown day, clamped to the visible range.
function visibleMinute(time: Date, dayStart: Date): number {
  const minutes = (time.getTime() - dayStart.getTime()) / 60000 - FIRST_HOUR * 60
  return Math.min(Math.max(minutes, 0), VISIBLE_MINUTES)
}

interface BarProps {
  booking: RoomBooking
  dayStart: Date
}

function BookingBar({ booking, dayStart }: BarProps) {
  const startsAt = new Date(booking.startsAt)
  const endsAt = new Date(booking.endsAt)
  const left = visibleMinute(startsAt, dayStart)
  const right = visibleMinute(endsAt, dayStart)

  if (right <= left) {
    return null // entirely outside the visible hours
  }

  const timeText = `${TIME_LABEL.format(startsAt)} to ${TIME_LABEL.format(endsAt)}`

  return (
    <li
      className="booking-bar"
      style={{
        left: `${(left / VISIBLE_MINUTES) * 100}%`,
        width: `${((right - left) / VISIBLE_MINUTES) * 100}%`,
      }}
      title={`${booking.title}, ${timeText}`}
    >
      <span className="booking-title">{booking.title}</span>
      <span className="visually-hidden">, {timeText}</span>
    </li>
  )
}

export function AvailabilityDay() {
  const [day, setDay] = useState(() => new Date())
  const range = dayWindow(day)
  const dayStart = new Date(range.from)

  const rooms = useGetRoomsQuery()
  const bookings = useGetBookingsInWindowQuery(range)

  if (rooms.isLoading) {
    return <p>Loading rooms...</p>
  }
  if (rooms.isError || rooms.data === undefined) {
    return <p role="alert">Couldn't load rooms. Is the backend running?</p>
  }

  // Group the flat list by room, so each row can find its bookings.
  const bookingsByRoom = new Map<number, RoomBooking[]>()
  for (const booking of bookings.data ?? []) {
    const list = bookingsByRoom.get(booking.roomId) ?? []
    list.push(booking)
    bookingsByRoom.set(booking.roomId, list)
  }

  return (
    <section className="availability" aria-labelledby="availability-heading">
      <div className="availability-header">
        <h2 id="availability-heading">{DAY_LABEL.format(day)}</h2>
        <div className="day-nav">
          <button type="button" onClick={() => setDay(addDays(day, -1))} aria-label="Previous day">
            ‹
          </button>
          <button type="button" onClick={() => setDay(new Date())}>
            Today
          </button>
          <button type="button" onClick={() => setDay(addDays(day, 1))} aria-label="Next day">
            ›
          </button>
        </div>
      </div>

      {bookings.isError && <p role="alert">Couldn't load bookings for this day.</p>}

      <div className="timeline" aria-busy={bookings.isFetching}>
        <div className="timeline-row timeline-hours" aria-hidden="true">
          <span />
          <div className="track">
            {HOUR_MARKS.map((hour) => (
              <span
                key={hour}
                className="hour-mark"
                style={{ left: `${((hour - FIRST_HOUR) / (LAST_HOUR - FIRST_HOUR)) * 100}%` }}
              >
                {hourLabel(hour)}
              </span>
            ))}
          </div>
        </div>

        {rooms.data.map((room) => {
          const roomBookings = bookingsByRoom.get(room.id) ?? []
          return (
            <div key={room.id} className="timeline-row">
              <span className="room-name" id={`room-${room.id}`}>
                {room.name}
              </span>
              <ul className="track" aria-labelledby={`room-${room.id}`}>
                {roomBookings.length === 0 && <li className="visually-hidden">Free all day</li>}
                {roomBookings.map((booking) => (
                  <BookingBar key={booking.bookingId} booking={booking} dayStart={dayStart} />
                ))}
              </ul>
            </div>
          )
        })}
      </div>
    </section>
  )
}
