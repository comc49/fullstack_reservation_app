// Display helpers for the activity feed.

const ACTION_LABELS: Record<string, string> = {
  booking_created: 'Booking created',
  booking_changed: 'Booking changed',
  booking_cancelled: 'Booking cancelled',
  room_checked: 'Room checked',
}

export function formatAction(action: string): string {
  return ACTION_LABELS[action] ?? action
}

// The API sends UTC. Staff read times in Irvine time.
const IRVINE_TIME = new Intl.DateTimeFormat('en-US', {
  timeZone: 'America/Los_Angeles',
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  hour: 'numeric',
  minute: '2-digit',
})

export function formatOccurredAt(occurredAt: string): string {
  return IRVINE_TIME.format(new Date(occurredAt))
}
