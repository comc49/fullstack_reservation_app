import { useGetRoomsQuery } from '../../api/apiSlice'

interface RoomPickerProps {
  selectedRoomId: number | null
  onSelect: (roomId: number) => void
}

export function RoomPicker({ selectedRoomId, onSelect }: RoomPickerProps) {
  const { data: rooms, isLoading, isError } = useGetRoomsQuery()

  if (isLoading) {
    return <p>Loading rooms...</p>
  }

  if (isError || rooms === undefined) {
    return <p role="alert">Couldn't load rooms. Is the backend running?</p>
  }

  return (
    <label className="room-picker">
      Room{' '}
      <select
        value={selectedRoomId ?? ''}
        onChange={(event) => onSelect(Number(event.target.value))}
      >
        <option value="" disabled>
          Choose a room
        </option>
        {rooms.map((room) => (
          <option key={room.id} value={room.id}>
            {room.name}
          </option>
        ))}
      </select>
    </label>
  )
}
