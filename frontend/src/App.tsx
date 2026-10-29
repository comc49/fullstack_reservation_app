import { useState } from 'react'

import { ActivityFeed } from './features/activity/ActivityFeed'
import { AvailabilityDay } from './features/availability/AvailabilityDay'
import { RoomPicker } from './features/rooms/RoomPicker'
import './App.css'

type View = 'availability' | 'activity'

function App() {
  const [view, setView] = useState<View>('availability')
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null)

  return (
    <main className="app">
      <nav className="view-tabs" aria-label="Views">
        <button type="button" aria-pressed={view === 'availability'} onClick={() => setView('availability')}>
          Availability
        </button>
        <button type="button" aria-pressed={view === 'activity'} onClick={() => setView('activity')}>
          Activity
        </button>
      </nav>

      {view === 'availability' ? (
        <AvailabilityDay />
      ) : (
        <>
          <h1>Room activity</h1>
          <RoomPicker selectedRoomId={selectedRoomId} onSelect={setSelectedRoomId} />
          {selectedRoomId !== null && <ActivityFeed roomId={selectedRoomId} />}
        </>
      )}
    </main>
  )
}

export default App
