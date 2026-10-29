import { useDispatch, useSelector } from 'react-redux'

import type { AppDispatch, RootState } from './store'

// Use these instead of plain useDispatch and useSelector, so the types are always right.
export const useAppDispatch = useDispatch.withTypes<AppDispatch>()
export const useAppSelector = useSelector.withTypes<RootState>()
