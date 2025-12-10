package backend.collab.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.Room; // Import the new Room class

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface RoomManager {
	void joinRoom(String roomId, WebSocketSession session);

	void leaveRoom(String roomId, WebSocketSession session);

	Set<WebSocketSession> getSessionsInRoom(String roomId);
}

@Service
class RoomManagerImpl implements RoomManager {

	@Autowired
	private UpdateWriter updateWriter;

	@Autowired
	private SnapshotService snapshotService;

	@Autowired
	private RedisStreamService redisService;

	private final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

	@Override
	public void joinRoom(String roomId, WebSocketSession session) {
		Room room = activeRooms.computeIfAbsent(roomId, k -> new Room(roomId));
		room.addSession(session);
	}

	@Override
	public void leaveRoom(String roomId, WebSocketSession session) {
		Room room = activeRooms.get(roomId);

		if (room != null) {
			room.removeSession(session);
			if (room.isEmpty()) {
				activeRooms.remove(roomId);
			}
		}
	}

	@Override
	public Set<WebSocketSession> getSessionsInRoom(String roomId) {
		Room room = activeRooms.get(roomId);

		if (room != null) {
			return room.getSessions();
		}
		return Collections.emptySet();
	}
}