package backend.collab.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import backend.collab.Room;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface RoomManager {
	void joinRoom(String roomId, WebSocketSession session);

	void leaveRoom(String roomId, WebSocketSession session);

	void sendUpdate(String diagramId, byte[] data, String senderId);
}

@Service
@Slf4j
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

	/**
	 * Broadcasts a raw binary message ONLY to clients in the specified room,
	 * excluding the sender.
	 * @param diagramId The room/diagram ID to broadcast within.
	 * 
	 * @param data     The byte array to send.
	 * @param senderId The session ID of the sender to exclude from the broadcast.
	 */
	public void sendUpdate(String diagramId, byte[] data, String senderId) {
		BinaryMessage message = new BinaryMessage(data);

		Set<WebSocketSession> roomSessions = Optional.ofNullable(activeRooms.get(diagramId))
               												.map(Room::getSessions)
               												.orElse(Collections.emptySet());


		if (roomSessions == null || roomSessions.isEmpty()) {
			log.warn("No sessions found for diagramId: {}", diagramId);
			return;
		}

		updateWriter.submitWriteTask(() -> {
			redisService.addUpdate(diagramId, data);
		});

		// Stream and send to the targeted room sessions
		roomSessions.parallelStream().forEach(session -> {
			if (session.isOpen() && !session.getId().equals(senderId)) {
				try {
					session.sendMessage(message);
				
				} catch (IOException e) {
					log.error("Error sending message to session {} in diagram {}:\n {}",
								 session.getId(), diagramId, e.getMessage());
				}
			}
		});
	}
}