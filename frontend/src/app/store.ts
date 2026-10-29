import { configureStore } from '@reduxjs/toolkit'

import { apiSlice } from '../api/apiSlice'

export const store = configureStore({
  reducer: {
    [apiSlice.reducerPath]: apiSlice.reducer,
  },
  // RTK Query's middleware handles caching, refetching, and cleanup.
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(apiSlice.middleware),
})

// Derived from the store itself, so they stay correct as reducers are added.
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
