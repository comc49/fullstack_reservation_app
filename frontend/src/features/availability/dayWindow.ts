// The time window for one calendar day in the viewer's time zone (Irvine for church staff).
// Returns ISO strings ready for GET /v1/bookings?from=...&to=...

export interface DayWindow {
  from: string
  to: string
}

export function dayWindow(day: Date): DayWindow {
  // Midnight at the start of this day, local time.
  const start = new Date(day.getFullYear(), day.getMonth(), day.getDate())

  // Midnight at the start of the NEXT day.
  const end = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1)

  return {
    from: start.toISOString(),
    to: end.toISOString(),
  }
}

export function addDays(day: Date, days: number): Date {
  return new Date(day.getFullYear(), day.getMonth(), day.getDate() + days)
}
