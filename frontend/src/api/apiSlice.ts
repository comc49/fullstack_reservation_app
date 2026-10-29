import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import type { ActivityPage, Room } from './types'

export const apiSlice = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({ baseUrl: '/v1' }),
  endpoints: (build) => ({
    // GET /v1/rooms
    getRooms: build.query<Room[], void>({
      query: () => 'rooms',
    }),

    // GET /v1/rooms/{roomId}/activity?cursor=...
    // Type parameters: <one page, the room id, the cursor>
    getRoomActivity: build.infiniteQuery<ActivityPage, number, string | null>({
      infiniteQueryOptions: {
        // The first request has no cursor.
        initialPageParam: null,                             // blank 1

        // After each page, RTK Query asks: "what's the cursor for the next one?"
        // Returning undefined means "no more pages", which sets hasNextPage to false.
        getNextPageParam: (lastPage) => lastPage.nextCursor,               // blank 2
      },
      query: ({ queryArg: roomId, pageParam: cursor }) => ({
        url: `rooms/${roomId}/activity`,                                          // blank 3
        params: cursor === null ? undefined: {cursor},                                       // blank 4: only send ?cursor= when there is one
      }),
    }),
  }),
})

export const { useGetRoomsQuery, useGetRoomActivityInfiniteQuery } = apiSlice
