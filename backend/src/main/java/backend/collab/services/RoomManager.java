package backend.collab.services;

import org.springframework.data.redis.connection.DataType;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.Room;
import backend.collab.exceptions.CollabException.RoomNotFoundException;
import backend.collab.snapshot.SnapshotService;
import backend.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public interface RoomManager {
	void joinRoom(String roomId, WebSocketSession session);

	void leaveRoom(String roomId, WebSocketSession session);

	void sendUpdate(String diagramId, byte[] data, String senderId, Role role);
}

@Service
@RequiredArgsConstructor
@Slf4j
class RoomManagerImpl implements RoomManager {

	private final RedisStreamService redisService;

	private final SnapshotService snapshotService;

	private final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

	@Override
	public void joinRoom(String roomId, WebSocketSession session) {
		Room room = activeRooms.computeIfAbsent(roomId, k -> new Room(roomId, snapshotService));
		room.addSession(session);
	}

	@Override
	public void leaveRoom(String roomId, WebSocketSession session) {
		Room room = activeRooms.get(roomId);
		if (room == null) return;

		room.removeSession(session);
		if (room.isEmpty()) {
			activeRooms.remove(roomId);
			room.takeSnapshot();
			room.close();
		}
	}

	/**
	 * Broadcasts a raw binary message ONLY to clients in the specified room,
	 * excluding the sender.
	 * 
	 * @param diagramId The room/diagram ID to broadcast within.
	 * @param data     The byte array to send.
	 * @param senderId The session ID of the sender to exclude from the broadcast.
	 */
	public void sendUpdate(String diagramId, byte[] data, String senderId, Role role) {
		Room room = activeRooms.get(diagramId);
		if (room == null) {
			throw new RoomNotFoundException("Room with diagramId %s is not found".formatted(diagramId));
		}
		
		final boolean cursorUpdate = (data[0] == 1);
		
		// Cursor positions don't need to be stored
		if (!cursorUpdate) {
			log.info("Storing update {} for diagramId {}", HexFormat.of().formatHex(data), diagramId);
			redisService.addUpdate(diagramId, data);
			// snapshotService.takeSnapshot(diagramId);
		}
		
		room.doUpdate(data, senderId, role);
	}
}