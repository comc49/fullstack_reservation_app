import { useState } from 'react'

import { ActivityFeed } from './features/activity/ActivityFeed'
import { RoomPicker } from './features/rooms/RoomPicker'
import './App.css'

function App() {
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null)

  return (
    <main className="app">
      <h1>Room activity</h1>
      <RoomPicker selectedRoomId={selectedRoomId} onSelect={setSelectedRoomId} />
      {selectedRoomId !== null && <ActivityFeed roomId={selectedRoomId} />}
    </main>
  )
}

export default App
