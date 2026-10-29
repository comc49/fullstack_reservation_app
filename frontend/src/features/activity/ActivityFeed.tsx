import { useGetRoomActivityInfiniteQuery } from '../../api/apiSlice'
import { formatAction, formatOccurredAt } from './formatActivity'

interface ActivityFeedProps {
  roomId: number
}

export function ActivityFeed({ roomId }: ActivityFeedProps) {
  const {
    data,
    isLoading,
    isError,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
  } = useGetRoomActivityInfiniteQuery(roomId)

  if (isLoading) {
    return <p>Loading activity...</p>
  }

  if (isError || data === undefined) {
    return <p role="alert">Couldn't load activity.</p>
  }

  // data.pages holds every page loaded so far: [page1, page2, ...]. Flatten them into one list.
  const items = data.pages.flatMap((page) => page.items)

  if (items.length === 0) {
    return <p>No activity yet.</p>
  }

  return (
    <section aria-label="Room activity">
      <ol className="activity-list">
        {items.map((item) => (
          <li key={item.id} className="activity-item">
            <time dateTime={item.occurredAt}>{formatOccurredAt(item.occurredAt)}</time>
            <span className="activity-action">{formatAction(item.action)}</span>
            {item.bookingTitle !== null && (
              <span className="activity-title">{item.bookingTitle}</span>
            )}
          </li>
        ))}
      </ol>

      {hasNextPage ? (
        <button type="button" onClick={() => fetchNextPage()} disabled={isFetchingNextPage}>
          {isFetchingNextPage ? 'Loading...' : 'Load more'}
        </button>
      ) : (
        <p>That's everything.</p>
      )}
    </section>
  )
}
